package ch.njol.skript.log;

/**
 * Blocks any messages from being logged.
 * 
 * @author Peter Güttinger
 */
public class BlockingLogHandler extends LogHandler {
	
	@Override
	public LogResult log(LogEntry entry) {
		return LogResult.DO_NOT_LOG;
	}
	
	@Override
	public BlockingLogHandler start() {
		SkriptLogger.startLogHandler(this);
		return this;
	}
	
}
