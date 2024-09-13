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

import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.util.Utils;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;

public class LogEntry {

	public final Level level;
	public final int quality;

	public final String message;

	@Nullable
	public final Node node;

	private final String from;
	private final boolean tracked;

	private static final String CONFIG_NODE = "skript command.reload";
	private static final ArgsMessage WARNING_LINE_INFO = new ArgsMessage(CONFIG_NODE + ".warning line info");
	private static final ArgsMessage ERROR_LINE_INFO = new ArgsMessage(CONFIG_NODE + ".error line info");
	private static final ArgsMessage WARNING_DETAILS = new ArgsMessage(CONFIG_NODE + ".warning details");
	private static final ArgsMessage ERROR_DETAILS = new ArgsMessage(CONFIG_NODE + ".error details");
	private static final ArgsMessage OTHER_DETAILS = new ArgsMessage(CONFIG_NODE + ".other details");
	private static final ArgsMessage LINE_DETAILS = new ArgsMessage(CONFIG_NODE + ".line details");

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
		return message;
	}

	private boolean used;

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

	public String toFormattedString() {
		if (level.intValue() < Level.WARNING.intValue())
			return message;

		ArgsMessage details;
		ArgsMessage lineInfo = WARNING_LINE_INFO;
		if (level.intValue() == Level.WARNING.intValue()) { // warnings
			details = WARNING_DETAILS;
		} else if (level.intValue() == Level.SEVERE.intValue()) { // errors
			details = ERROR_DETAILS;
			lineInfo = ERROR_LINE_INFO;
		} else { // anything else
			details = OTHER_DETAILS;
		}

		// Replace configured messages chat styles without user variables
		String lineInfoMsg = replaceNewline(Utils.replaceEnglishChatStyles(lineInfo.getValue() == null ? lineInfo.key : lineInfo.getValue()));
		String detailsMsg = replaceNewline(Utils.replaceEnglishChatStyles(details.getValue() == null ? details.key : details.getValue()));
		String lineDetailsMsg = replaceNewline(Utils.replaceEnglishChatStyles(LINE_DETAILS.getValue() == null ? LINE_DETAILS.key : LINE_DETAILS.getValue()));

		if (node == null)
			return String.format(detailsMsg.replaceAll("^\\s+", ""), message); // Remove line beginning spaces

		Config c = node.getConfig();
		String from = this.from;

		if (!from.isEmpty())
			from = ChatColor.GRAY + "   " + from + "\n";

		return
			String.format(lineInfoMsg, String.valueOf(node.getLine()), c.getFileName()) + // String.valueOf is to convert the line number (int) to a String
			String.format(detailsMsg, message.replaceAll("§", "&")) + from +
			String.format(lineDetailsMsg, node.save().trim().replaceAll("§", "&"));
	}

	private String replaceNewline(String s) {
		return s.replaceAll("\\\\n", "\n");
	}

}
