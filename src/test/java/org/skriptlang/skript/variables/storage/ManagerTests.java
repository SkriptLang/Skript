package org.skriptlang.skript.variables.storage;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.log.BlockingLogHandler;
import ch.njol.skript.variables.SerializedVariable;
import org.skriptlang.skript.variables.VariableMap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * What the {@link VariableStorageManager} tests share: a manager that saves every value as its text,
 * {@link RecordingStorage}s set up for it, and loading and closing without logging the expected errors.
 */
public final class ManagerTests {

	/**
	 * The type every value is saved as. Values of any other type can't be read.
	 */
	public static final String READABLE = "string";

	private ManagerTests() {
	}

	/**
	 * @param globals    the map of global variables
	 * @param mainThread runs the changes storages report
	 * @return a manager that saves every value as its text, and reads back values of type {@link #READABLE}
	 */
	public static VariableStorageManager manager(VariableMap globals, Executor mainThread) {
		return new VariableStorageManager(globals,
			value -> value == null ? null : value(String.valueOf(value)),
			(type, data) -> type.equals(READABLE) ? new String(data, StandardCharsets.UTF_8) : null,
			mainThread);
	}

	/**
	 * @param text the text
	 * @return the text saved as a value the manager can read back
	 */
	public static SerializedVariable.Value value(String text) {
		return new SerializedVariable.Value(READABLE, text.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * @param name     the storage's name
	 * @param prefixes the prefixes it accepts, taken as written; none to accept every name
	 * @return a storage whose saving thread waits an hour between saves, so it only saves when a test flushes
	 * or stops it, and never drains the queue before a test reads it
	 */
	static RecordingStorage storage(String name, String... prefixes) {
		RecordingStorage storage = new RecordingStorage().setPrefixesForTest(prefixes.length == 0 ? null : prefixes);
		storage.setNameForTest(name);
		storage.intervalMillis = 3_600_000;
		return storage;
	}

	/**
	 * Loads storages into a manager, keeping the errors that tests expect out of the log.
	 *
	 * @return what {@link VariableStorageManager#loadStorages(List)} returned
	 */
	static boolean loadStorages(VariableStorageManager manager, VariableStorage... storages) {
		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
			return manager.loadStorages(List.of(storages));
		}
	}

	/**
	 * Closes a manager, keeping the errors that tests expect out of the log.
	 */
	public static void close(VariableStorageManager manager) {
		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
			manager.close();
		}
	}

	/**
	 * @param databases the entries of the {@code databases} section, each line starting with a tab
	 * @return a config holding only that {@code databases} section
	 */
	public static Config config(String databases) throws IOException {
		return new Config(new ByteArrayInputStream(("databases:\n" + databases).getBytes(StandardCharsets.UTF_8)),
			"test.sk", false, true, ":");
	}

	/**
	 * @param databases the entries of the {@code databases} section, each line starting with a tab
	 * @return the parsed {@code databases} section
	 */
	public static SectionNode databases(String databases) throws IOException {
		return (SectionNode) config(databases).getMainNode().get("databases");
	}

	/**
	 * @return what the storage holds right now, with values read back as text
	 */
	static Map<String, String> stored(RecordingStorage storage) {
		Map<String, String> stored = new HashMap<>();
		synchronized (storage.saved) {
			storage.saved.forEach((name, value) -> stored.put(name, new String(value.data, StandardCharsets.UTF_8)));
		}
		return stored;
	}

}
