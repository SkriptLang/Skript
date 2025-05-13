package ch.njol.skript.log;

import java.io.Closeable;

import ch.njol.util.OpenCloseable;

/**
 * A log handler is used to handle Skripts logging.
 * A log handler is local to the thread it is started on.
 * <br>
 * Log handlers should always be stopped
 * when they are no longer needed.
 *
 * @see SkriptLogger#startLogHandler(LogHandler)
 * @author Peter Güttinger
 */
public abstract class LogHandler implements Closeable, OpenCloseable {
	
	public enum LogResult {
		LOG, CACHED, DO_NOT_LOG
	}
	
	/**
	 * @param entry entry to log
	 * @return Whether to print the specified entry or not.
	 */
	public abstract LogResult log(LogEntry entry);
	
	/**
	 * Called just after the handler is removed from the active handlers stack.
	 */
	protected void onStop() {}
	
	public final void stop() {
		SkriptLogger.removeHandler(this);
		onStop();
	}
	
	public boolean isStopped() {
		return SkriptLogger.isStopped(this);
	}
	
	/**
	 * A convenience method for {@link SkriptLogger#startLogHandler(LogHandler)}.
	 * <br>
	 * Implementations should override this to set the return type
	 * to the implementing class.
	 */
	public LogHandler start() {
		SkriptLogger.startLogHandler(this);
		return this;
	}
	
	@Override
	public void open() {
		start();
	}
	
	@Override
	public void close() {
		stop();
	}
	
}
