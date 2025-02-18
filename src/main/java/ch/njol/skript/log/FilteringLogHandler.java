package ch.njol.skript.log;

import java.util.logging.Level;

/**
 * @author Peter Güttinger
 */
public class FilteringLogHandler extends LogHandler {
	
	private final int minimum;
	
	public FilteringLogHandler(Level minimum) {
		this.minimum = minimum.intValue();
	}
	
	@Override
	public LogResult log(LogEntry entry) {
		return entry.level.intValue() >= minimum ? LogResult.LOG : LogResult.DO_NOT_LOG;
	}
	
	@Override
	public FilteringLogHandler start() {
		SkriptLogger.startLogHandler(this);
		return this;
	}
	
}
