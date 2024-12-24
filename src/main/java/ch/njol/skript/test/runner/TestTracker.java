package ch.njol.skript.test.runner;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

import ch.njol.skript.test.utils.TestResults;

/**
 * Tracks failed and succeeded tests.
 */
public class TestTracker {

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

	public static void testStarted(String name) {
		startedTests.add(name);
		currentTest = name;
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
