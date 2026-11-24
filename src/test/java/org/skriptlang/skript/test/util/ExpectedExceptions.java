package org.skriptlang.skript.test.util;

import ch.njol.skript.log.BlockingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;

/**
 * Keeps the reports that {@link ch.njol.skript.Skript#exception} prints out of the log.
 * <p>
 * {@code Skript.exception} logs straight to {@link SkriptLogger#LOGGER}, so a {@link BlockingLogHandler} doesn't stop it.
 * This filters that logger instead, for every thread, until closed. Only the lines of such a report are held back,
 * so other log messages still show. Use it around just the code that's expected to fail, so an unexpected report
 * from somewhere else is less likely to be hidden.
 * <pre>{@code
 * try (ExpectedExceptions exceptions = ExpectedExceptions.start()) {
 *     manager.close();
 *     exceptions.assertReported("Failed to close the database 'kills'");
 * }
 * }</pre>
 */
public final class ExpectedExceptions implements AutoCloseable {

	// what Skript.exception starts every line of its report with
	private static final String REPORT_PREFIX = "#!#! ";

	private final Logger logger = SkriptLogger.LOGGER;
	private final @Nullable Filter previous;
	private final List<String> lines = new ArrayList<>();

	private ExpectedExceptions() {
		previous = logger.getFilter();
		logger.setFilter(this::isLoggable);
	}

	/**
	 * Starts holding back exception reports, until {@link #close()} is called.
	 *
	 * @return the started filter
	 */
	public static ExpectedExceptions start() {
		return new ExpectedExceptions();
	}

	// classifies a record as part of an exception or not, via prefix.
	// if it is, it's filtered and stored. Otherwise it's set free.
	private boolean isLoggable(LogRecord record) {
		String message = record.getMessage();
		if (message != null && message.startsWith(REPORT_PREFIX)) {
			synchronized (lines) {
				lines.add(message.substring(REPORT_PREFIX.length()));
			}
			return false;
		}
		return previous == null || previous.isLoggable(record);
	}

	/**
	 * @return the lines of the reports held back so far, without their prefix
	 */
	public List<String> lines() {
		synchronized (lines) {
			return List.copyOf(lines);
		}
	}

	/**
	 * Checks that a report with a line containing the given text was held back.
	 *
	 * @param text part of the report's message, such as its first line
	 */
	public void assertReported(String text) {
		List<String> lines = lines();
		assertTrue("expected an exception report containing '" + text + "', got " + lines,
			lines.stream().anyMatch(line -> line.contains(text)));
	}

	/**
	 * Stops holding back reports, and puts back the logger's filter from before.
	 */
	@Override
	public void close() {
		logger.setFilter(previous);
	}

}
