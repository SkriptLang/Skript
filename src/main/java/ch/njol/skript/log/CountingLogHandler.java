package ch.njol.skript.log;

import java.util.logging.Level;

/**
 * Counts logged messages of a certain type
 * 
 * @author Peter Güttinger
 */
public class CountingLogHandler extends LogHandler {
	
	private final int minimum;
	
	private int count;
	
	public CountingLogHandler(Level minimum) {
		this.minimum = minimum.intValue();
	}
	
	@Override
	public LogResult log(LogEntry entry) {
		if (entry.level.intValue() >= minimum)
			count++;
		return LogResult.LOG;
	}
	
	@Override
	public CountingLogHandler start() {
		SkriptLogger.startLogHandler(this);
		return this;
	}
	
	public int getCount() {
		return count;
	}
	
}
