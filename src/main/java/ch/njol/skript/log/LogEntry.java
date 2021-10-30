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

import java.util.logging.Level;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;

/**
 * @author Peter Güttinger
 */
public class LogEntry {
	
	public final Level level;
	public final int quality;
	
	public final String message;
	
	@Nullable
	public final Node node;
	
	@Nullable
	private final String from;
	private final boolean tracked;
	
	public LogEntry(Level level, String message) {
		this(level, ErrorQuality.SEMANTIC_ERROR.quality(), message, SkriptLogger.getNode());
	}
	
	public LogEntry(Level level, int quality, String message) {
		this(level, quality, message, SkriptLogger.getNode());
	}
	
	public LogEntry(Level level, ErrorQuality quality, String message) {
		this(level, quality.quality(), message, SkriptLogger.getNode());
	}
	
	public LogEntry(Level level, String message, @Nullable Node node) {
		this(level, ErrorQuality.SEMANTIC_ERROR.quality(), message, node);
	}
	
	public LogEntry(Level level, ErrorQuality quality, String message, Node node) {
		this(level, quality.quality(), message, node);
	}
	
	public LogEntry(Level level, int quality, String message, @Nullable Node node) {
		this(level, quality, message, node, false);
	}
	
	public LogEntry(Level level, int quality, String message, @Nullable Node node, boolean tracked) {
		this.level = level;
		this.quality = quality;
		this.message = message;
		this.node = node;
		this.tracked = tracked;
		from = tracked || Skript.debug() ? findCaller() : "";
	}
	
	private static final String skriptLogPackageName = "" + SkriptLogger.class.getPackage().getName();
	
	static String findCaller() {
		StackTraceElement[] es = new Exception().getStackTrace();
		for (int i = 0; i < es.length; i++) {
			if (!es[i].getClassName().startsWith(skriptLogPackageName))
				continue;
			i++;
			while (i < es.length - 1 && (es[i].getClassName().startsWith(skriptLogPackageName) || es[i].getClassName().equals(Skript.class.getName())))
				i++;
			if (i >= es.length)
				i = es.length - 1;
			return " (from " + es[i] + ")";
		}
		return " (from an unknown source)";
	}
	
	public Level getLevel() {
		return level;
	}
	
	public int getQuality() {
		return quality;
	}
	
	public String getMessage() {
		return toString();
	}
	
	private boolean used = false;
	
	void discarded(String info) {
		used = true;
		if (tracked)
			SkriptLogger.LOGGER.warning(" # LogEntry '" + message + "'" + from + " discarded" + findCaller() + "; " + (new Exception()).getStackTrace()[1] + "; " + info);
	}
	
	void logged() {
		used = true;
		if (tracked)
			SkriptLogger.LOGGER.warning(" # LogEntry '" + message + "'" + from + " logged" + findCaller());
	}
	
	@Override
	protected void finalize() {
		assert used : message + from;
	}
	
	@Override
	public String toString() {
		if (node == null || level.intValue() < Level.WARNING.intValue())
			return message;
		Config c = node.getConfig();
		return message + from + " (" + c.getFileName() + ", line " + node.getLine() + ": " + node.save().trim() + "')";
	}
	
}
