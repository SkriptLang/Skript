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
