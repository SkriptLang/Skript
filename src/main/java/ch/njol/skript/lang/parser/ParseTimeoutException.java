package ch.njol.skript.lang.parser;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.util.Timespan;

import java.util.concurrent.TimeUnit;

/**
 * An exception noting that parsing a single line took longer than allowed.
 * Thrown by {@link ParserInstance#checkParseDeadline()} and caught by the script loader,
 * which reports it as a parse error for the line.
 */
public class ParseTimeoutException extends RuntimeException {

	private static final ArgsMessage m_parse_timeout = new ArgsMessage("log.parse timeout");

	/**
	 * Creates a deadline for parsing a single line that starts now,
	 * based on {@link SkriptConfig#longParseTimeErrorThreshold}.
	 * @return A {@link System#nanoTime()} value to pass to {@link ParserInstance#setParseDeadline(long)},
	 * 	or 0 if parse timeouts are disabled.
	 */
	public static long createDeadline() {
		long timeout = SkriptConfig.longParseTimeErrorThreshold.value().getAs(Timespan.TimePeriod.MILLISECOND);
		if (timeout <= 0)
			return 0;
		return System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeout);
	}

	/**
	 * Prints the error for a line that exceeded {@link SkriptConfig#longParseTimeErrorThreshold}.
	 */
	public static void printError() {
		Skript.error(m_parse_timeout.toString(SkriptConfig.longParseTimeErrorThreshold.value()));
	}

	public ParseTimeoutException() {
		super("Parsing took too long");
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

}
