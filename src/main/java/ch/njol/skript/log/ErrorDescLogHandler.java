package ch.njol.skript.log;

import java.util.logging.Level;

import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;

/**
 * Does nothing but print messages before the first error encountered and/or a message at the end if no error were encountered.
 * 
 * @author Peter Güttinger
 */
public class ErrorDescLogHandler extends LogHandler {
	@Nullable
	private final String before, after, success;
	
	public ErrorDescLogHandler() {
		this(null, null, null);
	}
	
	public ErrorDescLogHandler(@Nullable String before, @Nullable String after, @Nullable String success) {
		this.before = before;
		this.after = after;
		this.success = success;
	}
	
	private boolean hadError = false;
	
	@Override
	public LogResult log(LogEntry entry) {
		if (!hadError && entry.getLevel() == Level.SEVERE) {
			hadError = true;
			beforeErrors();
		}
		return LogResult.LOG;
	}
	
	@Override
	public ErrorDescLogHandler start() {
		SkriptLogger.startLogHandler(this);
		return this;
	}
	
	protected void beforeErrors() {
		if (before != null)
			Skript.error(before);
	}
	
	protected void afterErrors() {
		if (after != null)
			Skript.error(after);
	}
	
	protected void onSuccess() {
		if (success != null)
			Skript.info(success);
	}
	
	@Override
	protected void onStop() {
		if (!hadError)
			onSuccess();
		else
			afterErrors();
	}
	
}
