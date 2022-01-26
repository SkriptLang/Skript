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
