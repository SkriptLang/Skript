package ch.njol.skript.log;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;

/**
 * Redirects the log to one or more {@link CommandSender}s.
 */
public class RedirectingLogHandler extends LogHandler {

	private final Collection<CommandSender> recipients;
	private int numErrors = 0;
	private final String prefix;

	public RedirectingLogHandler(CommandSender recipient, @Nullable String prefix) {
		this(Collections.singletonList(recipient), prefix);
	}

	public RedirectingLogHandler(Collection<CommandSender> recipients, @Nullable String prefix) {
		this.recipients = new ArrayList<>(recipients);
		this.prefix = prefix == null ? "" : prefix;
	}

	@Override
	public LogResult log(LogEntry entry) {
		return log(entry, null);
	}

	public LogResult log(LogEntry entry, @Nullable CommandSender ignore) {
		String formattedMessage = prefix + entry.toFormattedString();
		for (CommandSender recipient : recipients) {
			if (recipient == ignore)
				continue;
			SkriptLogger.sendFormatted(recipient, formattedMessage);
		}
		if (entry.level == Level.SEVERE) {
			numErrors++;
		}
		return LogResult.DO_NOT_LOG;
	}

	@Override
	public RedirectingLogHandler start() {
		return SkriptLogger.startLogHandler(this);
	}

	public int numErrors() {
		return numErrors;
	}
}

