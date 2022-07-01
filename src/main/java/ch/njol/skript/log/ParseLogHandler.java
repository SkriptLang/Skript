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

import ch.njol.skript.Skript;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ParseLogHandler extends LogHandler {
	
	@Nullable
	private LogEntry error = null;
	
	private final List<LogEntry> log = new ArrayList<>();
	
	@Override
	public LogResult log(LogEntry entry) {
		if (entry.getLevel().intValue() >= Level.SEVERE.intValue()
				&& (error == null || entry.getQuality() > error.getQuality())) {
			error = entry;
		}

		log.add(entry);
		return LogResult.CACHED;
	}

	boolean printedErrorOrLog = false;

	@Override
	public ParseLogHandler start() {
		SkriptLogger.startLogHandler(this);
		return this;
	}
	
	public void error(String error, ErrorQuality quality) {
		log(new LogEntry(SkriptLogger.SEVERE, quality, error));
	}
	
	/**
	 * Clears all log messages except for the error
	 */
	public void clear() {
		for (LogEntry e : log)
			e.discarded("cleared");
		log.clear();
	}

	public void clearError() {
		if (error != null)
			error.discarded("cleared");
		error = null;
	}

	/**
	 * Prints the retained log
	 */
	public void printLog() {
		printLog(true);
	}

	public void printLog(boolean includeErrors) {
		printedErrorOrLog = true;
		stop();
		for (LogEntry logEntry : log)
			if (includeErrors || logEntry.getLevel().intValue() < Level.SEVERE.intValue())
				SkriptLogger.log(logEntry);
		if (error != null)
			error.discarded("not printed");
	}

	public void printError() {
		printError(null);
	}
	
	/**
	 * Prints the best error or the given error if no error has been logged.
	 * 
	 * @param def Error to log if no error has been logged so far, can be null
	 */
	public void printError(@Nullable String def) {
		printedErrorOrLog = true;
		stop();
		LogEntry error = this.error;
		if (error != null)
			SkriptLogger.log(error);
		else if (def != null)
			SkriptLogger.log(new LogEntry(SkriptLogger.SEVERE, ErrorQuality.SEMANTIC_ERROR, def));
		for (LogEntry e : log)
			e.discarded("not printed");
	}
	
	public void printError(String def, ErrorQuality quality) {
		printedErrorOrLog = true;
		stop();
		LogEntry error = this.error;
		if (error != null && error.quality >= quality.quality())
			SkriptLogger.log(error);
		else
			SkriptLogger.log(new LogEntry(SkriptLogger.SEVERE, quality, def));
		for (LogEntry e : log)
			e.discarded("not printed");
	}
	
	public int getNumErrors() {
		return error == null ? 0 : 1;
	}
	
	public boolean hasError() {
		return error != null;
	}
	
	@Nullable
	public LogEntry getError() {
		return error;
	}
	
}
