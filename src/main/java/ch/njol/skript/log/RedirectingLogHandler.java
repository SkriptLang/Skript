/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
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
		String formattedMessage = prefix + entry.toFormattedString();
		for (CommandSender recipient : recipients) {
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

