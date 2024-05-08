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
package ch.njol.skript.effects;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;

import org.skriptlang.skript.lang.script.Script;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.util.Closeable;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Log")
@Description({"Writes text into a .log file. Skript will write these files to /plugins/Skript/logs.",
		"NB: Using 'server.log' as the log file will write to the default server log. Omitting the log file altogether will log the message as '[Skript] [&lt;script&gt;.sk] &lt;message&gt;' in the server log."})
@Examples({"on place of TNT:",
	    "	log \"%player% placed TNT in %world% at %location of block%\" to file \"tnt/placement.log\"",
	    "	log \"%player% placed TNT in %world% at %location of block%\" to file \"tnt/placement.log\"\"with severity of info\"",
		"	log \"%player% placed TNT in %world% at %location of block%\" to file \"tnt/placement.log\"\"with severity of warning\"",
        "   log \"%player% placed TNT in %world% at %location of block%\" to file \"tnt/placement.log\"\"with severity of error\""})


@Since("2.0, 2.9 (Adds severity to logs)")
public class EffLog extends Effect {
	static {
		Skript.registerEffect(EffLog.class, "log %strings% [(to|in) [file[s]] %-strings%] [with severity (of) (1:warning|2:severe)]");
	}
	
	private final static File logsFolder = new File(Skript.getInstance().getDataFolder(), "logs");
	
	final static HashMap<String, PrintWriter> writers = new HashMap<>();
	static {
		Skript.closeOnDisable(new Closeable() {
			@Override
			public void close() {
				for (final PrintWriter pw : writers.values())
					pw.close();
			}
		});
	}
	
	@SuppressWarnings("null")
	private Expression<String> messages;
	@Nullable
	private Expression<String> files;

	private int loglevel;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		messages = (Expression<String>) exprs[0];
		files = (Expression<String>) exprs[1];
		loglevel = parser.mark;
		return true;
	}
	
	@SuppressWarnings("resource")
	@Override
	protected void execute(final Event e) {
		for (final String message : messages.getArray(e)) {
			if (files != null) {
				for (String logFile : files.getArray(e)) {
					logFile = logFile.toLowerCase(Locale.ENGLISH);
					if (!logFile.endsWith(".log"))
						logFile += ".log";
					if (logFile.equals("server.log")) {
						switch (loglevel) {
							case 1: SkriptLogger.LOGGER.log(Level.WARNING, message);
								break;
							case 2: SkriptLogger.LOGGER.log(Level.SEVERE, message);
								break;
							default:
								SkriptLogger.LOGGER.log(Level.INFO, message);

						}
					}
					PrintWriter logWriter = writers.get(logFile);
					if (logWriter == null) {
						final File logFolder = new File(logsFolder, logFile); // REMIND what if logFile contains '..'?
						try {
							logFolder.getParentFile().mkdirs();
							logWriter = new PrintWriter(new BufferedWriter(new FileWriter(logFolder, true)));
							writers.put(logFile, logWriter);
						} catch (final IOException ex) {
							Skript.error("Cannot write to log file '" + logFile + "' (" + logFolder.getPath() + "): " + ExceptionUtils.toString(ex));
							return;
						}
					}
					logWriter.println("[" + SkriptConfig.formatDate(System.currentTimeMillis()) + "] " + message);
					logWriter.flush();
				}
			} else {
				Trigger t = getTrigger();
				String scriptName = "---";
				if (t != null) {
					Script script = t.getScript();
					if (script != null)
						scriptName = script.getConfig().getFileName();
				}
				switch (loglevel) {
					case 1: Skript.warning("[" + scriptName + "] " + message);
						break;
					case 2: Skript.error("[" + scriptName + "] " + message);
						break;
					default:
						Skript.info("[" + scriptName + "] " + message);
				}
			}
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "log " + messages.toString(e, debug) + (files != null ? " to " + files.toString(e, debug) : "");
	}
}
