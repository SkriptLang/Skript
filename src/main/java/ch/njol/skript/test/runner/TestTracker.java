package ch.njol.skript.test.runner;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import ch.njol.skript.Skript;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

import ch.njol.skript.test.utils.TestResults;

/**
 * Tracks failed and succeeded tests.
 */
public class TestTracker {

	/**
	 * Registered tests for eventual group execution.
	 */
	private static final Map<String, Runnable> registeredTests = new LinkedHashMap<>(200);

	/**
	 * Started tests.
	 */
	private static final Set<String> startedTests = new HashSet<>();

	/**
	 * Failed tests to failure assert messages.
	 */
	private static final Map<String, String> failedTests = new HashMap<>();

	@Nullable
	private static String currentTest;

	/**
	 * How many callers currently expect {@link Skript#exception} to be called, so it doesn't fail the current test.
	 */
	private static int expectingExceptions;

	public static void registerTest(String test, Runnable runnable) {
		if (registeredTests.containsKey(test)) {
			throw new IllegalArgumentException("Test already registered: " + test);
		}
		registeredTests.put(test, runnable);
	}

	public static void unregisterTest(String test) {
		registeredTests.remove(test);
	}

	public static void runRegisteredTests() {
		for (var entry : new LinkedHashMap<>(registeredTests).entrySet()) {
			Skript.info("Running test case " + entry.getKey());
			testStarted(entry.getKey());
			try {
				entry.getValue().run();
			} catch (Throwable t) {
				// Skript.exception will mark the test as failed
				Skript.exception(t, "A severe error occurred while running test case: " + entry.getKey());
			}
		}
	}

	public static void testStarted(String name) {
		startedTests.add(name);
		currentTest = name;
	}

	public static void parsingStarted(String name) {
		currentTest = name + " (parsing)";
	}

	/**
	 * Sets whether {@link Skript#exception} is expected to be called, for tests of code that reports exceptions on purpose.
	 * While expected, it doesn't fail the current test. Each {@code true} must be matched by a {@code false}.
	 *
	 * @param expected whether exceptions are expected from now on
	 */
	public static void expectExceptions(boolean expected) {
		expectingExceptions += expected ? 1 : -1;
	}

	/**
	 * @return whether {@link Skript#exception} is currently expected to be called
	 * @see #expectExceptions(boolean)
	 */
	public static boolean exceptionsExpected() {
		return expectingExceptions > 0;
	}

	public static void JUnitTestFailed(String currentTest, String msg) {
		failedTests.put(currentTest, msg);
	}

	public static void testFailed(String msg) {
		failedTests.put(currentTest, msg);
	}

	public static void testFailed(String msg, Script script) {
		String file = getFileName(script);
		failedTests.put(currentTest, msg + " [" + file + "]");
	}

	public static void testFailed(String msg, Script script, int line) {
		String file = getFileName(script);
		failedTests.put(currentTest, msg + " [" + file + ", line " + line + "]");
	}

	private static String getFileName(Script script) {
		String file = script.getConfig().getFileName();
		return file.substring(file.lastIndexOf(File.separator) + 1);
	}

	public static void junitTestFailed(String junit, String msg) {
		failedTests.put(junit, msg);
	}

	public static Map<String, String> getFailedTests() {
		return new HashMap<>(failedTests);
	}

	public static Set<String> getSucceededTests() {
		Set<String> tests = new HashSet<>(startedTests);
		tests.removeAll(failedTests.keySet());
		return tests;
	}

	public static TestResults collectResults() {
		TestResults results = new TestResults(getSucceededTests(), getFailedTests(), TestMode.docsFailed);
		startedTests.clear();
		failedTests.clear();
		return results;
	}

}
