package org.skriptlang.skript.variables.storage.flatfile;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.variables.SerializedVariable;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.variables.storage.CsvFormat;
import org.skriptlang.skript.variables.storage.LoadListener;
import org.skriptlang.skript.variables.storage.VariableStorage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.concurrent.TimeUnit;

/**
 * Saves variables to a CSV file, in {@link CsvFormat}.
 * <p>
 * The file works like a log. Each save appends a row for every change, and a later row for a name wins over any
 * earlier one. Deleting a list is just one row for the whole list. We don't keep any variables in memory here.
 * Instead, every rewrite writes out the current variables sorted by name, so the file is always a sorted,
 * compact <i>base</i> followed by whatever was appended since.
 * <p>
 * The base ends with a {@link CsvFormat#COMPACTED_END} line, and the header records the byte offset where that line
 * ends, so we know where the base stops without reading it. Compacting the file only needs the appended rows
 * in memory: we read those into {@link Changes}, then stream the base through and merge them in. This happens once
 * the appended rows outnumber the base, and on shutdown. Loading works the same way, passing each variable on
 * as it's merged.
 * <p>
 * Since the file is all we save from, we have to notice when something else changes it while we're running, like
 * someone editing it or restoring a backup over it. Before every save we check that it's still the size we left it at,
 * and that its compacted rows still end where we left them. Compacting also checks that every row we read back is one
 * we could have written. If the file was changed, we say so, read it again as it is now and carry on from there,
 * and ask for every variable to be saved again from memory, which puts back whatever the change lost.
 * Since that's only noticed when we next write, we also check once on shutdown, before the last save.
 * <p>
 * This class decides when to do what and interfaces with the storage api.<br>
 * {@link VariableFile} handles the file itself, like IO and file system details.<br>
 * {@link CsvFormat} is responsible for the format of the file / its text.<br>
 * {@link Changes} handles interpreting and merging the rows, with {@link Row} being the representation of each row.<br>
 * {@link LoadReport} is a nice helper for storing and printing the problems found while loading.
 */
public class FlatFileStorage extends VariableStorage {

	/**
	 * We only rewrite the file once at least this many rows were appended, and at least as many as the base holds.
	 */
	private static final int MIN_APPENDED_ROWS = 1000;

	/**
	 * The shortest time between rewrites, so a small file isn't rewritten over and over.
	 */
	private static final long MIN_REWRITE_INTERVAL_NANOS = TimeUnit.MINUTES.toNanos(1);

	/**
	 * Once this many times as many rows as the base holds were appended, we rewrite the file
	 * without waiting for {@link #MIN_REWRITE_INTERVAL_NANOS}.
	 */
	private static final int FORCE_REWRITE_FACTOR = 10;

	/**
	 * Once this many rows were appended, we rewrite the file no matter what. Compacting holds what the appended rows
	 * change in memory, and so does loading after a crash, so this keeps that bounded for really large files.
	 */
	private static final int MAX_APPENDED_ROWS = 250_000;

	private @Nullable VariableFile file;
	// details about the base compacted section
	private long baseRows;
	private long baseEnd;
	// how many rows were appended after the base
	private long appendedRows;
	// how long the file is, in bytes, up to the end of the last complete save. a failed write may have left more
	private long fileLength;
	// whether a write may have left the file incomplete, so the next save rewrites it
	private boolean rewriteRequired;
	private long lastRewrite = System.nanoTime() - MIN_REWRITE_INTERVAL_NANOS;
	private boolean loaded;

	/**
	 * Creates a CSV storage. Skript does this when the config picks the CSV type.
	 *
	 * @param addon The addon that registered the CSV storage type, which is Skript
	 * @param type  The type name the config picked this storage with, such as {@code csv}
	 */
	public FlatFileStorage(SkriptAddon addon, String type) {
		super(addon, type);
	}

	/**
	 * Configures the storage. The CSV storage has no options of its own beyond the {@code file} every file storage has,
	 * so there's nothing to do here.
	 *
	 * @param node The storage's config section
	 * @return Always true
	 */
	@Override
	protected boolean configure(SectionNode node) {
		return true;
	}

	/**
	 * @return True, since the variables are saved to a file
	 */
	@Override
	protected boolean requiresFile() {
		return true;
	}

	// ---------------------------- LOADING ----------------------------

	/**
	 * Reads the file and passes each variable on as soon as it's known.
	 * <p>
	 * If this storage wrote the file, the header says where its compacted rows end, and we check that against the
	 * {@link CsvFormat#COMPACTED_END} line. If that holds, we read the rows after it first, then stream the compacted rows
	 * through and merge them in. Any other file, like one from an older version or one with rows deleted by hand before
	 * that line, is read as if it had no compacted rows at all: every row is read first, then the file is rewritten.
	 * This takes more memory/cpu, but is more reliable for files we aren't sure about.
	 * <p>
	 * Rows that can't be read, and a last row cut off by a crash, are skipped and reported in one summary.
	 * If there are any, the file is backed up and then rewritten without them.
	 * Otherwise, a file with valid compacted rows is just opened for appending as it is.
	 *
	 * @param listener Where to pass each variable
	 * @throws IOException If the file can't be read, is from before Skript 2.1, or can't be backed up before
	 *                     it's rewritten
	 */
	@Override
	protected synchronized void load(LoadListener listener) throws IOException {
		open(row -> listener.loaded(row.name(), row.value()));
		loaded = true;
	}

	/**
	 * Reads the file, passing each variable to the sink, and gets it ready for appending, as {@link #load} describes.
	 *
	 * @param sink Where to pass each variable
	 * @throws IOException If the file can't be read, is from before Skript 2.1, or can't be backed up before
	 *                     it's rewritten
	 */
	private void open(Row.Sink sink) throws IOException {
		VariableFile file = file();
		VariableFile.Layout layout = file.readLayout();
		long changesFrom = layout.changesFrom();

		// read the appended lines (tail) first, then merge with the base compacted section
		LoadReport report = new LoadReport();
		Changes changes = new Changes();
		try (BufferedReader tail = file.read(changesFrom, layout.size())) {
			changes.read(tail, layout.tornTail(), report);
		}
		// merge tail with base and report loaded variables so they go into memory
		long compactedRows = merge(changesFrom, changes, report, sink);

		if (layout.compacted() && report.isEmpty()) {
			// situation normal, store data + allow appending
			file.openAppend();
			baseRows = compactedRows;
			appendedRows = changes.count();
			baseEnd = changesFrom;
			fileLength = layout.size();
			rewriteRequired = false;
		} else {
			// issue detected, or no valid compacted section! report and rewrite
			if (!report.isEmpty()) {
				// report issues
				report.print(file.name());
				file.backUp();
			}
			// rewrite the file in a proper manner. the problematic rows are left out the same way again
			rewrite(changesFrom, changes, false);
		}
	}

	// ---------------------------- SAVING ----------------------------

	/**
	 * Appends a row for each of the batch's clears and changes, in one write that's synced to disk.
	 * If enough rows were appended already, or an earlier write failed, the whole file is compacted instead,
	 * with the batch merged in.
	 *
	 * <p>
	 * First, if something else changed the file since we last wrote it, it's read again as it is now.
	 *
	 * @param batch The changes to save
	 * @throws IOException If the file couldn't be written. The next save then compacts it, with this batch again
	 */
	@Override
	protected synchronized void save(Batch batch) throws IOException {
		// handle possible grimy fingers touching our precious files
		if (changedOutside())
			reopenChanged();
		StringBuilder rows = new StringBuilder();
		int added = 0;
		// append clears to buffer
		for (String prefix : batch.clears()) {
			CsvFormat.appendRow(rows, prefix + "*", null);
			added++;
		}
		// append changes to buffer
		for (SerializedVariable change : batch.changes()) {
			CsvFormat.appendRow(rows, change.name, change.value);
			added++;
		}

		// compact from buffer if needed, otherwise append buffer.
		if (rewriteRequired || !file().isOpen() || shouldRewrite(added)) {
			compact(rows); // the batch is merged in as if it had been appended
			return;
		}
		if (added == 0)
			return;
		try {
			fileLength = file().append(rows);
		} catch (IOException e) {
			// anything written past fileLength is ignored by the rewrite, which gets this batch again
			rewriteRequired = true;
			throw e;
		}
		appendedRows += added;
	}

	/**
	 * Checks whether the file should be compacted. That's always the case once {@link #MAX_APPENDED_ROWS} rows
	 * would have been appended. Otherwise, it's when at least {@link #MIN_APPENDED_ROWS} rows would have been
	 * appended, and at least as many as the base holds, and either the last rewrite was long enough ago,
	 * or {@link #FORCE_REWRITE_FACTOR} times as many rows were appended as the base holds.
	 *
	 * @param appending How many rows are about to be appended
	 * @return Whether to compact the file instead of appending them
	 */
	private boolean shouldRewrite(int appending) {
		long appended = appendedRows + appending;
		// keeps the memory compacting needs bounded, however large the base is
		if (appended >= MAX_APPENDED_ROWS)
			return true;
		if (appended < Math.max(MIN_APPENDED_ROWS, baseRows))
			return false;
		if (System.nanoTime() - lastRewrite >= MIN_REWRITE_INTERVAL_NANOS)
			return true;
		return appended >= FORCE_REWRITE_FACTOR * baseRows + MIN_APPENDED_ROWS;
	}

	/**
	 * Compacts the file, with the given rows merged in as if they had been appended after it.
	 * If it turns out something else changed the file, it's read again as it is now, and then compacted.
	 *
	 * @param extraRows Rows in the file's format, each ending with a line break
	 * @throws IOException If reading or writing fails. The old file is only replaced once the new one is complete
	 */
	private void compact(CharSequence extraRows) throws IOException {
		try {
			compactOnce(extraRows);
		} catch (ChangedOutsideException e) {
			reopenChanged();
			compactOnce(extraRows);
		}
	}

	/**
	 * Compacts the file, with the given rows merged in as if they had been appended after it.
	 *
	 * @param extraRows Rows in the file's format, each ending with a line break
	 * @throws ChangedOutsideException If a row we wrote can't be read back, so something else changed the file
	 * @throws IOException             If reading or writing fails. The old file is only replaced once the new one is complete
	 */
	private void compactOnce(CharSequence extraRows) throws IOException {
		Changes changes = appendedChanges();
		changes.read(new BufferedReader(new StringReader(extraRows.toString())), false, new LoadReport());
		rewrite(baseEnd, changes, true);
	}

	/**
	 * Reads the rows appended after the base.
	 *
	 * @return What they change
	 * @throws ChangedOutsideException If one of them can't be read
	 * @throws IOException             If the file can't be read
	 */
	private Changes appendedChanges() throws IOException {
		Changes changes = new Changes();
		LoadReport report = new LoadReport();
		try (BufferedReader appended = file().read(baseEnd, fileLength)) {
			changes.read(appended, false, report);
		}
		// we wrote these rows ourselves, after loading checked the file, so a problem means something else changed it
		if (!report.isEmpty())
			throw new ChangedOutsideException();
		return changes;
	}

	/**
	 * Rewrites the file as only compacted rows: the ones it has now, with the changes merged in.
	 *
	 * @param compactedEnd Where the file's compacted rows end, or 0 if it has none
	 * @param changes      The changes to merge in
	 * @param ours         Whether we wrote the compacted rows since loading, so they must all be readable.
	 *                     Otherwise, rows that can't be read are left out, since loading reported them already
	 * @throws ChangedOutsideException If the compacted rows are ours, but one of them can't be read.
	 *                                 The file isn't replaced then
	 * @throws IOException             If reading or writing fails. The old file is only replaced once the new one is complete
	 */
	private void rewrite(long compactedEnd, Changes changes, boolean ours) throws IOException {
		rewriteRequired = true; // until it's done
		VariableFile file = file();
		VariableFile.Written written = file.rewrite(sink -> {
			if (ours) {
				mergeOurs(compactedEnd, changes, sink);
			} else {
				merge(compactedEnd, changes, new LoadReport(), sink);
			}
		});
		// the file is replaced now, so this has to describe it even if opening it fails, which the next save retries
		lastRewrite = System.nanoTime();
		baseRows = written.rows();
		appendedRows = 0;
		baseEnd = fileLength = written.size();
		rewriteRequired = false;
		file.openAppend();
	}

	/**
	 * Streams the file's compacted rows with the changes merged in.
	 *
	 * @param compactedEnd Where the file's compacted rows end, or 0 if it has none
	 * @param changes      The changes to merge in
	 * @param report       Where to report problems with the compacted rows
	 * @param sink         Where to give the resulting rows, in the order of their names
	 * @return How many rows were read from the compacted rows
	 * @throws IOException If the file can't be read, or the sink fails
	 */
	private long merge(long compactedEnd, Changes changes, LoadReport report, Row.Sink sink) throws IOException {
		// a file with no compacted rows may not even exist yet
		try (BufferedReader compacted = compactedEnd == 0 ? new BufferedReader(Reader.nullReader()) : file().read(0, compactedEnd)) {
			return changes.applyTo(compacted, report, sink);
		}
	}

	/**
	 * Streams the compacted rows we wrote since loading, with the changes merged in.
	 *
	 * @param compactedEnd Where the file's compacted rows end
	 * @param changes      The changes to merge in
	 * @param sink         Where to give the resulting rows, in the order of their names
	 * @throws ChangedOutsideException If one of the compacted rows can't be read, so something else changed the file.
	 *                                 That's only known once every row was given to the sink
	 * @throws IOException             If the file can't be read, or the sink fails
	 */
	private void mergeOurs(long compactedEnd, Changes changes, Row.Sink sink) throws IOException {
		LoadReport report = new LoadReport();
		merge(compactedEnd, changes, report, sink);
		if (!report.isEmpty())
			throw new ChangedOutsideException();
	}

	/**
	 * @return True, since backups are compacted from the file
	 */
	@Override
	protected boolean supportsBackups() {
		return true;
	}

	/**
	 * Writes a backup in the same format as the file, with only the current variables.
	 *
	 * If something else changed the file since we last wrote it, it's read again first.
	 *
	 * @param out Where to write the backup. It isn't closed
	 * @throws IOException If the file can't be read, or the backup can't be written. Also if it turns out something
	 *                     else changed the file while it was backed up, in which case it's read again for the next backup
	 */
	@Override
	protected synchronized void writeBackup(OutputStream out) throws IOException {
		if (changedOutside())
			reopenChanged();
		try {
			Changes changes = appendedChanges();
			VariableFile.writeBackup(out, sink -> mergeOurs(baseEnd, changes, sink));
		} catch (ChangedOutsideException e) {
			// some of the backup was written already, so it can't just be written again
			reopenChanged();
			throw new IOException("Some rows of " + file().name() + " couldn't be read back, so it wasn't backed up", e);
		}
	}

	// ------------------------ OUTSIDE CHANGES ------------------------

	/**
	 * Checks whether something else changed the file since we last wrote it: it was replaced or deleted, isn't the size
	 * we left it at, or its compacted rows don't end where we left them. A failed append may have left more at the end.
	 *
	 * @return Whether the file was changed
	 * @throws IOException If the file can't be read
	 */
	private boolean changedOutside() throws IOException {
		return !file().isAsWritten(baseEnd, fileLength, rewriteRequired);
	}

	/**
	 * Carries on after something else changed the file while we're running, which isn't supported.
	 * <p>
	 * We say so, then read the file again as it is now, just like loading it, which also backs it up if any of its rows
	 * have to be left out. If it was deleted, we start a new one. Then we {@link #requestResave() request a resave},
	 * so every variable is saved again from memory: rows the other change removed or wrote over are put back, and
	 * rows it changed are set back to what's in memory. Rows it added for variables that aren't in memory stay,
	 * and are loaded on the next start.
	 *
	 * @throws IOException If the file can't be read again or backed up, or a new one can't be written
	 */
	private void reopenChanged() throws IOException {
		VariableFile file = file();
		file.closeAppend(); // it may still be open on a file that was replaced
		Skript.error("Something changed " + file.name() + " while the server was running, which isn't supported."
			+ " All variables will be saved to it again from memory, which undoes any changes made to it by hand."
			+ " Please stop the server before editing it.");
		if (file.exists()) {
			open(row -> {}); // the variables in memory are what we save from now on, so these aren't passed on
		} else {
			rewrite(0, new Changes(), false);
		}
		requestResave();
	}

	/**
	 * Checks whether something else changed the file since we last wrote it, and if so, reads it again and requests
	 * a resave, like a save would. Otherwise a change made while the server is idle would only be noticed while closing,
	 * after the variables were saved for the last time.
	 *
	 * @throws IOException If the file can't be read again or backed up, or a new one can't be written
	 */
	@Override
	protected synchronized void checkIntegrity() throws IOException {
		if (loaded && changedOutside())
			reopenChanged();
	}

	/**
	 * Thrown when a row we wrote can't be read back, so something else must have changed the file.
	 */
	private static final class ChangedOutsideException extends IOException {

		ChangedOutsideException() {
			super("A row that was saved can't be read back");
		}

	}

	// ---------------------------- CLOSING ----------------------------

	/**
	 * Compacts the file if anything was appended since its last rewrite, so the next start reads a compact file,
	 * then closes it. If something else changed the file since we last wrote it, it's read again first.
	 * If the rewrite fails we only report it, since the file is still complete.
	 * Nothing is rewritten if loading failed.
	 */
	@Override
	public synchronized void close() {
		VariableFile file = this.file;
		if (file == null)
			return; // never loaded
		if (loaded) {
			try {
				if (changedOutside())
					reopenChanged();
				if (rewriteRequired || appendedRows > 0)
					compact("");
			} catch (IOException e) {
				Skript.error("Could not compact " + file.name() + " while shutting down: " + e.getMessage()
					+ ". The file still holds what it held before, and is compacted on a later start instead.");
			}
		}
		loaded = false;
		try {
			file.closeAppend();
		} catch (IOException e) {
			Skript.warning("Could not close " + file.name() + ": " + e.getMessage());
		}
	}

	/**
	 * @return The file this storage saves to
	 * @throws IllegalStateException If the storage hasn't been configured with a file
	 */
	private VariableFile file() {
		VariableFile file = this.file;
		if (file == null) {
			File configured = getStorageFile();
			if (configured == null)
				throw new IllegalStateException("The database '" + getName() + "' has no file");
			this.file = file = new VariableFile(configured);
		}
		return file;
	}

}
