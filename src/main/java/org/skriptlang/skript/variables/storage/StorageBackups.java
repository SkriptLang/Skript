package org.skriptlang.skript.variables.storage;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * Makes the backups of one {@link VariableStorage}: decides when one is due, writes it safely, and deletes old ones.
 * <p>
 * The storage provides the backup's contents through a {@link Writer}. Backups are written to a temporary file
 * that is only renamed to its real name once complete, so an interrupted backup never looks like a real one,
 * and are named {@code <name>_yyyy-MM-dd_HH-mm-ss-SSS.csv}, plus {@code .gz} when compressed.
 * Only files named like this storage's backups are ever deleted.
 * <p>
 * Not thread safe: the storage only uses it on its saving thread, while holding its save lock,
 * so that nothing is saved while a backup is written.
 */
@NotThreadSafe
final class StorageBackups {

	/**
	 * Writes a full copy of the stored variables.
	 */
	@FunctionalInterface
	interface Writer {

		/**
		 * @param out where to write the backup; must not be closed
		 * @throws IOException if the backup could not be written
		 */
		void write(OutputStream out) throws IOException;

	}

	/**
	 * Reports a failure. Must never throw.
	 */
	@FunctionalInterface
	interface Reporter {

		void report(Throwable t, String message);

	}

	private static final String BACKUP_EXTENSION = ".csv";
	private static final String TEMP_EXTENSION = ".tmp";
	// fixed width, so sorting backups by name sorts them by age
	private static final DateTimeFormatter BACKUP_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");

	private final String storageName;
	private final Path folder;
	private final String prefix;
	private final boolean compress;
	private final long intervalNanos; // 0 means backups are off
	private final int toKeep;         // -1 keeps every backup
	private final Clock clock;        // for file names
	private final Writer writer;
	private final Reporter reporter;

	private long nextBackupAt;     // System.nanoTime() of the next backup
	private boolean changed = true; // whether anything was saved since the last backup; true so the first one happens

	/**
	 * @param storageName   the storage's name, for file names and messages
	 * @param folder        the folder to put backups in, created when the first backup is written
	 * @param compress      whether to gzip backups
	 * @param intervalNanos the time between backups, or 0 for no backups
	 * @param toKeep        how many backups to keep, -1 to keep all, or 0 for none (backups are then off)
	 * @param now           the current {@link System#nanoTime()}; the first backup is due one interval later
	 * @param clock         the clock for the time in file names
	 * @param writer        writes a backup's contents
	 * @param reporter      reports failures
	 */
	StorageBackups(String storageName, Path folder, boolean compress, long intervalNanos, int toKeep, long now,
				   Clock clock, Writer writer, Reporter reporter) {
		this.storageName = storageName;
		this.folder = folder;
		this.prefix = safeName(storageName) + "_";
		this.compress = compress;
		this.intervalNanos = toKeep == 0 ? 0 : intervalNanos;
		this.toKeep = toKeep;
		this.clock = clock;
		this.writer = writer;
		this.reporter = reporter;
		this.nextBackupAt = now + this.intervalNanos;
	}

	/**
	 * @return a storage's name, with anything that might not be allowed in a file name replaced
	 */
	static String safeName(String storageName) {
		return storageName.replaceAll("[^A-Za-z0-9._-]", "_");
	}

	/**
	 * @return whether backups are made at all
	 */
	boolean isOn() {
		return intervalNanos != 0;
	}

	/**
	 * Called when saving starts. If no backups are to be kept, deletes this storage's old ones.
	 * Never throws: failures are reported.
	 */
	void start() {
		if (toKeep == 0)
			prune(0);
	}

	/**
	 * Notes that a batch was saved, so the next backup has something new in it.
	 */
	void changed() {
		changed = true;
	}

	/**
	 * Writes a backup if backups are on, the interval has passed and something was saved since the last one.
	 * Never throws: a failed backup is reported and tried again after the next interval.
	 *
	 * @param now the current {@link System#nanoTime()}
	 */
	void backupIfDue(long now) {
		if (intervalNanos == 0 || !changed || now - nextBackupAt < 0)
			return;
		nextBackupAt = now + intervalNanos; // set first, so a failing backup waits a full interval
		try {
			write();
		} catch (IOException | RuntimeException e) {
			reporter.report(e, "Could not back up the database '" + storageName + "'");
			return;
		}
		changed = false;
		prune(toKeep);
	}

	/**
	 * Writes a new backup through a temporary file, which is only renamed to its real name once it is complete.
	 */
	private void write() throws IOException {
		Files.createDirectories(folder);
		String fileName = prefix + LocalDateTime.now(clock).format(BACKUP_TIME)
			+ BACKUP_EXTENSION + (compress ? ".gz" : "");
		Path target = folder.resolve(fileName);
		Path temp = folder.resolve(fileName + TEMP_EXTENSION);
		try {
			try (OutputStream file = Files.newOutputStream(temp);
				 OutputStream out = compress
					 ? new GZIPOutputStream(new BufferedOutputStream(file))
					 : new BufferedOutputStream(file)) {
				writer.write(out);
			}
			Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE);
		} finally {
			Files.deleteIfExists(temp); // only still there if writing or renaming failed
		}
	}

	/**
	 * Deletes this storage's oldest backups until only {@code keep} are left, and any temporary files
	 * left behind by an interrupted backup. Only touches files named like this storage's backups.
	 * Never throws: failures are reported.
	 *
	 * @param keep how many backups to keep, or -1 to keep all of them
	 */
	private void prune(int keep) {
		if (keep < 0 || !Files.isDirectory(folder))
			return;
		List<Path> backups = new ArrayList<>();
		try {
			try (DirectoryStream<Path> files = Files.newDirectoryStream(folder)) {
				for (Path path : files) {
					String fileName = path.getFileName().toString();
					if (!fileName.startsWith(prefix))
						continue;
					if (fileName.endsWith(TEMP_EXTENSION)) {
						Files.deleteIfExists(path); // left behind by an interrupted backup
					} else if (fileName.endsWith(BACKUP_EXTENSION) || fileName.endsWith(BACKUP_EXTENSION + ".gz")) {
						backups.add(path);
					}
				}
			}
			backups.sort(Comparator.comparing(path -> path.getFileName().toString()));
			for (int i = 0; i < backups.size() - keep; i++)
				Files.deleteIfExists(backups.get(i));
		} catch (IOException | RuntimeException e) {
			reporter.report(e, "Could not delete old backups of the database '" + storageName + "'");
		}
	}

}
