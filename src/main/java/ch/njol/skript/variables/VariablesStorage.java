/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.variables;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.variables.SerializedVariable.Value;
import ch.njol.util.Closeable;

// FIXME ! large databases (>25 MB) cause the server to be unresponsive instead of loading slowly

/**
 * @author Peter Güttinger
 */
public abstract class VariablesStorage implements Closeable {
	
	private final static int QUEUE_SIZE = 1000, FIRST_WARNING = 300;
	
	final LinkedBlockingQueue<SerializedVariable> changesQueue = new LinkedBlockingQueue<>(QUEUE_SIZE);
	
	protected volatile boolean closed = false;
	
	protected final String databaseName;
	
	@Nullable
	protected File file;
	
	/**
	 * null for '.*' or '.+'
	 */
	@Nullable
	private Pattern variablePattern;
	
	// created in the constructor, started in load()
	private final Thread writeThread;
	
	protected VariablesStorage(final String name) {
		databaseName = name;
		writeThread = Skript.newThread(new Runnable() {
			@Override
			public void run() {
				while (!closed) {
					try {
						final SerializedVariable var = changesQueue.take();
						final Value d = var.value;
						if (d != null)
							save(var.name, d.type, d.data);
						else
							save(var.name, null, null);
					} catch (final InterruptedException e) {}
				}
			}
		}, "Skript variable save thread for database '" + name + "'");
	}
	
	@Nullable
	protected String getValue(final SectionNode n, final String key) {
		return getValue(n, key, String.class);
	}
	
	@Nullable
	protected <T> T getValue(final SectionNode n, final String key, final Class<T> type) {
		final String v = n.getValue(key);
		if (v == null) {
			Skript.error("The config is missing the entry for '" + key + "' in the database '" + databaseName + "'");
			return null;
		}
		final ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			final T r = Classes.parse(v, type, ParseContext.CONFIG);
			if (r == null)
				log.printError("The entry for '" + key + "' in the database '" + databaseName + "' must be " + Classes.getSuperClassInfo(type).getName().withIndefiniteArticle());
			else
				log.printLog();
			return r;
		} finally {
			log.stop();
		}
	}
	
	public final boolean load(final SectionNode n) {
		final String pattern = getValue(n, "pattern");
		if (pattern == null)
			return false;
		try {
			variablePattern = pattern.equals(".*") || pattern.equals(".+") ? null : Pattern.compile(pattern);
		} catch (final PatternSyntaxException e) {
			Skript.error("Invalid pattern '" + pattern + "': " + e.getLocalizedMessage());
			return false;
		}
		
		if (requiresFile()) {
			final String f = getValue(n, "file");
			if (f == null)
				return false;
			final File file = getFile(f).getAbsoluteFile();
			this.file = file;
			if (file.exists() && !file.isFile()) {
				Skript.error("The database file '" + file.getName() + "' must be an actual file, not a directory.");
				return false;
			} else {
				try {
					file.createNewFile();
				} catch (final IOException e) {
					Skript.error("Cannot create the database file '" + file.getName() + "': " + e.getLocalizedMessage());
					return false;
				}
			}
			if (!file.canWrite()) {
				Skript.error("Cannot write to the database file '" + file.getName() + "'!");
				return false;
			}
			if (!file.canRead()) {
				Skript.error("Cannot read from the database file '" + file.getName() + "'!");
//				Skript.error("This means that no variables will be available and can also prevent new variables from being saved!");
//				try {
//					final File backup = FileUtils.backup(file);
//					Skript.error("A backup of your variables.csv was created as " + backup.getName());
//				} catch (final IOException e) {
//					Skript.error("Failed to create a backup of your variables.csv: " + e.getLocalizedMessage());
//					loadError = true;
//				}
				return false;
			}
			
			if (!"0".equals(getValue(n, "backup interval"))) {
				final Timespan backupInterval = getValue(n, "backup interval", Timespan.class);
				if (backupInterval != null)
					startBackupTask(backupInterval);
			}
		}
		
		if (!load_i(n))
			return false;
		
		writeThread.start();
		Skript.closeOnDisable(this);
		
		return true;
	}
	
	/**
	 * Loads variables stored here.
	 * 
	 * @return Whether the database could be loaded successfully, i.e. whether the config is correct and all variables could be loaded
	 */
	protected abstract boolean load_i(SectionNode n);
	
	/**
	 * Called after all storages have been loaded, and variables have been redistributed if settings have changed. This should commit the first transaction (which is not empty if
	 * variables have been moved from another database to this one or vice versa), and start repeating transactions if applicable.
	 */
	protected abstract void allLoaded();
	
	protected abstract boolean requiresFile();
	
	protected abstract File getFile(String file);
	
	/**
	 * Must be locked after {@link Variables#getReadLock()} (if that lock is used at all)
	 */
	protected final Object connectionLock = new Object();
	
	/**
	 * (Re)connects to the database (not called on the first connect - do this in {@link #load_i(SectionNode)}).
	 * 
	 * @return Whether the connection could be re-established. An error should be printed by this method prior to returning false.
	 */
	protected abstract boolean connect();
	
	/**
	 * Disconnects from the database.
	 */
	protected abstract void disconnect();
	
	@Nullable
	protected Task backupTask = null;
	
	public void startBackupTask(final Timespan t) {
		final File file = this.file;
		if (file == null || t.getTicks_i() == 0)
			return;
		backupTask = new Task(Skript.getInstance(), t.getTicks_i(), t.getTicks_i(), true) {
			@Override
			public void run() {
				synchronized (connectionLock) {
					disconnect();
					try {
						FileUtils.backup(file);
					} catch (final IOException e) {
						Skript.error("Automatic variables backup failed: " + e.getLocalizedMessage());
					} finally {
						connect();
					}
				}
			}
		};
	}
	
	boolean accept(final @Nullable String var) {
		if (var == null)
			return false;
		return variablePattern != null ? variablePattern.matcher(var).matches() : true;
	}
	
	private long lastWarning = Long.MIN_VALUE;
	private final static int WARNING_INTERVAL = 10;
	private long lastError = Long.MIN_VALUE;
	private final static int ERROR_INTERVAL = 10;
	
	/**
	 * May be called from a different thread than Bukkit's main thread.
	 */
	final void save(final SerializedVariable var) {
		if (changesQueue.size() > FIRST_WARNING && lastWarning < System.currentTimeMillis() - WARNING_INTERVAL * 1000) {
			Skript.warning("Cannot write variables to the database '" + databaseName + "' at sufficient speed; server performance may suffer and many variables will be lost if the server crashes. (this warning will be repeated at most once every " + WARNING_INTERVAL + " seconds)");
			lastWarning = System.currentTimeMillis();
		}
		if (!changesQueue.offer(var)) {
			if (lastError < System.currentTimeMillis() - ERROR_INTERVAL * 1000) {
				Skript.error("Skript cannot save any variables to the database '" + databaseName + "'. The server will hang and may crash if no more variables can be saved.");
				lastError = System.currentTimeMillis();
			}
			while (true) {
				try {
					// REMIND add repetitive error and/or stop saving variables altogether?
					changesQueue.put(var);
					break;
				} catch (final InterruptedException e) {}
			}
		}
	}
	
	/**
	 * Called when Skript gets disabled. The default implementation will wait for all variables to be saved before setting {@link #closed} to true and stopping the write thread,
	 * thus <tt>super.close()</tt> must be called if this method is overridden!
	 */
	@Override
	public void close() {
		while (changesQueue.size() > 0) {
			try {
				Thread.sleep(10);
			} catch (final InterruptedException e) {}
		}
		closed = true;
		writeThread.interrupt();
	}
	
	/**
	 * Clears the saveQueue of unsaved variables. Only used if all variables are saved immediately after calling this method.
	 */
	protected void clearChangesQueue() {
		changesQueue.clear();
	}
	
	/**
	 * Saves a variable. This is called from the main thread while variables are transferred between databases, and from the {@link #writeThread} afterwards.
	 * 
	 * @param name
	 * @param type
	 * @param value
	 * @return Whether the variable was saved
	 */
	protected abstract boolean save(String name, @Nullable String type, byte @Nullable [] value);
	
}
