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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.Version;
import ch.njol.util.NotifyingReference;

/**
 * TODO use a database (SQLite) instead and only load a limited amount of variables into RAM - e.g. 2 GB (configurable). If more variables are available they will be loaded when
 * accessed. (rem: print a warning when Skript starts)
 * rem: store null variables (in memory) to prevent looking up the same variables over and over again
 * 
 * @author Peter Güttinger
 */
public class FlatFileStorage extends VariablesStorage {
	
	@SuppressWarnings("null")
	public final static Charset UTF_8 = Charset.forName("UTF-8");
	
	/**
	 * A Lock on this object must be acquired after connectionLock (if that lock is used) (and thus also after {@link Variables#getReadLock()}).
	 */
	private final NotifyingReference<PrintWriter> changesWriter = new NotifyingReference<>();
	
	private volatile boolean loaded = false;
	
	final AtomicInteger changes = new AtomicInteger(0);
	private final int REQUIRED_CHANGES_FOR_RESAVE = 1000;
	
	@Nullable
	private Task saveTask;
	
	private boolean loadError = false;
	
	protected FlatFileStorage(final String name) {
		super(name);
	}
	
	/**
	 * Doesn'ts lock the connection as required by {@link Variables#variableLoaded(String, Object, VariablesStorage)}.
	 */
	@SuppressWarnings({"deprecation"})
	@Override
	protected boolean load_i(final SectionNode n) {
		SkriptLogger.setNode(null);
		
		IOException ioEx = null;
		int unsuccessful = 0;
		final StringBuilder invalid = new StringBuilder();
		
		Version varVersion = Skript.getVersion(); // will be set later
		
		final Version v2_0_beta3 = new Version(2, 0, "beta 3");
		boolean update2_0_beta3 = false;
		final Version v2_1 = new Version(2, 1);
		boolean update2_1 = false;
		
		BufferedReader r = null;
		try {
			r = new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF_8));
			String line = null;
			int lineNum = 0;
			while ((line = r.readLine()) != null) {
				lineNum++;
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) {
					if (line.startsWith("# version:")) {
						try {
							varVersion = new Version("" + line.substring("# version:".length()).trim());
							update2_0_beta3 = varVersion.isSmallerThan(v2_0_beta3);
							update2_1 = varVersion.isSmallerThan(v2_1);
						} catch (final IllegalArgumentException e) {}
					}
					continue;
				}
				final String[] split = splitCSV(line);
				if (split == null || split.length != 3) {
					Skript.error("invalid amount of commas in line " + lineNum + " ('" + line + "')");
					if (invalid.length() != 0)
						invalid.append(", ");
					invalid.append(split == null ? "<unknown>" : split[0]);
					unsuccessful++;
					continue;
				}
				if (split[1].equals("null")) {
					Variables.variableLoaded("" + split[0], null, this);
				} else {
					Object d;
					if (update2_1)
						d = Classes.deserialize("" + split[1], "" + split[2]);
					else
						d = Classes.deserialize("" + split[1], decode("" + split[2]));
					if (d == null) {
						if (invalid.length() != 0)
							invalid.append(", ");
						invalid.append(split[0]);
						unsuccessful++;
						continue;
					}
					if (d instanceof String && update2_0_beta3) {
						d = Utils.replaceChatStyles((String) d);
					}
					Variables.variableLoaded("" + split[0], d, this);
				}
			}
		} catch (final IOException e) {
			loadError = true;
			ioEx = e;
		} finally {
			if (r != null) {
				try {
					r.close();
				} catch (final IOException e) {}
			}
		}
		
		final File file = this.file;
		if (file == null) {
			assert false : this;
			return false;
		}
		
		if (ioEx != null || unsuccessful > 0 || update2_1) {
			if (unsuccessful > 0) {
				Skript.error(unsuccessful + " variable" + (unsuccessful == 1 ? "" : "s") + " could not be loaded!");
				Skript.error("Affected variables: " + invalid.toString());
			}
			if (ioEx != null) {
				Skript.error("An I/O error occurred while loading the variables: " + ExceptionUtils.toString(ioEx));
				Skript.error("This means that some to all variables could not be loaded!");
			}
			try {
				if (update2_1) {
					Skript.info("[2.1] updating " + file.getName() + " to the new format...");
				}
				final File bu = FileUtils.backup(file);
				Skript.info("Created a backup of " + file.getName() + " as " + bu.getName());
				loadError = false;
			} catch (final IOException ex) {
				Skript.error("Could not backup " + file.getName() + ": " + ex.getMessage());
			}
		}
		
		if (update2_1) {
			saveVariables(false);
			Skript.info(file.getName() + " successfully updated.");
		}
		
		connect();
		
		saveTask = new Task(Skript.getInstance(), 5 * 60 * 20, 5 * 60 * 20, true) {
			@Override
			public void run() {
				if (changes.get() >= REQUIRED_CHANGES_FOR_RESAVE) {
					saveVariables(false);
					changes.set(0);
				}
			}
		};
		
		return ioEx == null;
	}
	
	@Override
	protected void allLoaded() {
		// no transaction support
	}
	
	@Override
	protected boolean requiresFile() {
		return true;
	}
	
	@Override
	protected File getFile(final String file) {
		return new File(file);
	}
	
	static String encode(final byte[] data) {
		final char[] r = new char[data.length * 2];
		for (int i = 0; i < data.length; i++) {
			r[2 * i] = Character.toUpperCase(Character.forDigit((data[i] & 0xF0) >>> 4, 16));
			r[2 * i + 1] = Character.toUpperCase(Character.forDigit(data[i] & 0xF, 16));
		}
		return new String(r);
	}
	
	static byte[] decode(final String hex) {
		final byte[] r = new byte[hex.length() / 2];
		for (int i = 0; i < r.length; i++) {
			r[i] = (byte) ((Character.digit(hex.charAt(2 * i), 16) << 4) + Character.digit(hex.charAt(2 * i + 1), 16));
		}
		return r;
	}
	
	@SuppressWarnings("null")
	private final static Pattern csv = Pattern.compile("(?<=^|,)\\s*([^\",]*|\"([^\"]|\"\")*\")\\s*(,|$)");
	
	@Nullable
	static String[] splitCSV(final String line) {
		final Matcher m = csv.matcher(line);
		int lastEnd = 0;
		final ArrayList<String> r = new ArrayList<>();
		while (m.find()) {
			if (lastEnd != m.start())
				return null;
			final String v = m.group(1);
			if (v.startsWith("\""))
				r.add(v.substring(1, v.length() - 1).replace("\"\"", "\""));
			else
				r.add(v.trim());
			lastEnd = m.end();
		}
		if (lastEnd != line.length())
			return null;
		return r.toArray(new String[r.size()]);
	}
	
	@Override
	protected boolean save(final String name, final @Nullable String type, final @Nullable byte[] value) {
		synchronized (connectionLock) {
			synchronized (changesWriter) {
				if (!loaded && type == null)
					return true; // deleting variables is not really required for this kind of storage, as it will be completely rewritten every once in a while, and at least once when the server stops.
				PrintWriter cw;
				while ((cw = changesWriter.get()) == null) {
					try {
						changesWriter.wait();
					} catch (final InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				writeCSV(cw, name, type, value == null ? "" : encode(value));
				cw.flush();
				changes.incrementAndGet();
			}
		}
		return true;
	}
	
	/**
	 * Use with find()
	 */
	@SuppressWarnings("null")
	private final static Pattern containsWhitespace = Pattern.compile("\\s");
	
	private static void writeCSV(final PrintWriter pw, final String... values) {
		assert values.length == 3; // name, type, value
		for (int i = 0; i < values.length; i++) {
			if (i != 0)
				pw.print(", ");
			String v = values[i];
			if (v != null && (v.contains(",") || v.contains("\"") || v.contains("#") || containsWhitespace.matcher(v).find()))
				v = '"' + v.replace("\"", "\"\"") + '"';
			pw.print(v);
		}
		pw.println();
	}
	
	@Override
	protected final void disconnect() {
		synchronized (connectionLock) {
			clearChangesQueue();
			synchronized (changesWriter) {
				final PrintWriter cw = changesWriter.get();
				if (cw != null) {
					cw.close();
					changesWriter.set(null);
				}
			}
		}
	}
	
	@Override
	protected final boolean connect() {
		synchronized (connectionLock) {
			synchronized (changesWriter) {
				if (changesWriter.get() != null)
					return true;
				try (FileOutputStream fos = new FileOutputStream(file, true)){
					changesWriter.set(new PrintWriter(new OutputStreamWriter(fos, UTF_8)));
					loaded = true;
					return true;
				} catch (IOException e) { // close() might throw ANY IOException
					Skript.exception(e);
					return false;
				}
			}
		}
	}
	
	@Override
	public void close() {
		clearChangesQueue();
		super.close();
		saveVariables(true); // also closes the writer
	}
	
	/**
	 * Completely rewrites the while file
	 * 
	 * @param finalSave whether this is the last save in this session or not.
	 */
	public final void saveVariables(final boolean finalSave) {
		if (finalSave) {
			final Task st = saveTask;
			if (st != null)
				st.cancel();
			final Task bt = backupTask;
			if (bt != null)
				bt.cancel();
		}
		try {
			Variables.getReadLock().lock();
			synchronized (connectionLock) {
				try {
					final File f = file;
					if (f == null) {
						assert false : this;
						return;
					}
					disconnect();
					if (loadError) {
						try {
							final File backup = FileUtils.backup(f);
							Skript.info("Created a backup of the old " + f.getName() + " as " + backup.getName());
							loadError = false;
						} catch (final IOException e) {
							Skript.error("Could not backup the old " + f.getName() + ": " + ExceptionUtils.toString(e));
							Skript.error("No variables are saved!");
							return;
						}
					}
					File tempFile = new File(file.getParentFile(), file.getName() + ".temp");
					PrintWriter pw = null;
					try {
						pw = new PrintWriter(tempFile, "UTF-8");
						pw.println("# === Skript's variable storage ===");
						pw.println("# Please do not modify this file manually!");
						pw.println("#");
						pw.println("# version: " + Skript.getVersion());
						pw.println();
						save(pw, "", Variables.getVariables());
						pw.println();
						pw.flush();
						pw.close();
						FileUtils.move(tempFile, f, true);
					} catch (final IOException e) {
						Skript.error("Unable to make a final save of the database '" + databaseName + "' (no variables are lost): " + ExceptionUtils.toString(e)); // FIXME happens at random - check locks/threads
					} finally {
						if (pw != null)
							pw.close();
					}
				} finally {
					if (!finalSave) {
						connect();
					}
				}
			}
		} finally {
			Variables.getReadLock().unlock();
			boolean gotLock = Variables.variablesLock.writeLock().tryLock();
			if (gotLock) { // Only process queue now if it doesn't require us to wait
				try {
					Variables.processChangeQueue();
				} finally {
					Variables.variablesLock.writeLock().unlock();
				}
			}
		}
	}
	
	/**
	 * Saves the variables.
	 * <p>
	 * This method uses the sorted variables map to save the variables in order.
	 * 
	 * @param pw
	 * @param parent The parent's name with {@link Variable#SEPARATOR} at the end
	 * @param map
	 */
	@SuppressWarnings("unchecked")
	private void save(PrintWriter pw, String parent, TreeMap<String, Object> map) {
		outer: for (Entry<String, Object> e : map.entrySet()) {
			Object val = e.getValue();
			if (val == null)
				continue;
			if (val instanceof TreeMap) {
				save(pw, parent + e.getKey() + Variable.SEPARATOR, (TreeMap<String, Object>) val);
			} else {
				String name = e.getKey() == null ? parent.substring(0, parent.length() - Variable.SEPARATOR.length()) : parent + e.getKey();

				try {
					for (VariablesStorage s : Variables.storages) {
						if (s.accept(name)) {
							if (s == this) {
								SerializedVariable.Value value = Classes.serialize(val);
								if (value != null)
									writeCSV(pw, name, value.type, encode(value.data));
							}
							continue outer;
						}
					}
				} catch (Exception ex) {
					Skript.exception(ex, "Error saving variable named " + name);
				}
			}
		}
	}
	
}
