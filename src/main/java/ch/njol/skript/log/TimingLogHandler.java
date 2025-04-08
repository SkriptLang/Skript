
/**
 * A {@link LogHandler} that records the time since its creation.
 */
package ch.njol.skript.log;

/**
 * A log handler that records the time since its creation.
 */
public class TimingLogHandler extends LogHandler {

	private final long start = System.currentTimeMillis();

	@Override
	public LogResult log(LogEntry entry) {
		return LogResult.LOG;
	}

	@Override
	public TimingLogHandler start() {
		return SkriptLogger.startLogHandler(this);
	}

	/**
	 * @return the time in milliseconds of when this log handler was created.
	 */
	public long getStart() {
		return start;
	}

	/**
	 * @return the time in milliseconds between now and this log handler's creation.
	 */
	public long getTimeTaken() {
		return System.currentTimeMillis() - start;
	}

}
