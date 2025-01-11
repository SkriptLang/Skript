package ch.njol.skript.timings;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Skript's implementation of {@link Timings}
 */
public class SkriptTimings extends Timings {

	private boolean enabled;
	private final Map<String, ScriptTiming> scriptTimings = new HashMap<>();

	public SkriptTimings(boolean enabled) {
		this.enabled = enabled;
	}

	// == TIMINGS ==

	@Override
	public void reset(String script) {
		this.scriptTimings.remove(script);
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (!enabled)
			this.scriptTimings.clear();
	}

	@Override
	public @Nullable Timing start(Timeable timeable) {
		if (this.enabled) {
			Node node = timeable.getNode();
			if (node != null) {
				return new TimingInstance(timeable);
			}
		}
		return null;
	}

	@Override
	public void stop(@Nullable Timing timing, boolean async) {
		if (!(timing instanceof TimingInstance timingInstance))
			return;

		Timeable timeable = timingInstance.timeable;

		String name = timeable.getNode().getConfig().getFileName();
		if (!this.scriptTimings.containsKey(name)) {
			this.scriptTimings.put(name, new ScriptTiming());
		}
		this.scriptTimings.get(name).stop(timingInstance, async);
	}

	// == COMMAND ==

	@Override
	public boolean hasCommand() {
		return true;
	}

	@Override
	public boolean handleCommand(CommandSender sender, String[] args) {
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("enable")) {
				if (this.isEnabled()) {
					Skript.info(sender, "<light red>Timings are already enabled.");
				} else {
					setEnabled(true);
					Skript.info(sender, "Timings enabled.");
				}
			} else if (args[0].equalsIgnoreCase("disable")) {
				if (!this.isEnabled()) {
					Skript.info(sender, "<light red>Timings are already disabled.");
				} else {
					setEnabled(false);
					Skript.info(sender, "Timings have been disabled.");
				}
			} else if (args[0].equalsIgnoreCase("reset")) {
				if (args.length > 1) {
					File file = ScriptLoader.getScriptFromName(args[1]);
					if (file != null) {
						if (file.isDirectory()) {
							for (Script script : ScriptLoader.getScripts(file)) {
								reset(script.getConfig().getFileName());
							}
							Skript.info(sender, "Timings have been reset for <reset>'<light aqua>" + file.getName() + "/<reset>'");
						} else {
							Script script = ScriptLoader.getScript(file);
							assert script != null;
							String fileName = script.getConfig().getFileName();
							reset(fileName);
							Skript.info(sender, "Timings have been reset for <reset>'<light aqua>" + fileName + "<reset>'");
						}
					} else {
						Skript.info(sender, "<light red>No script found for '" + args[1] + "'.");
					}
				} else {
					this.scriptTimings.clear();
					Skript.info(sender, "Timings for all scripts have been reset.");
				}
			} else if (args.length > 1 && (args[0].equalsIgnoreCase("print") || args[0].equalsIgnoreCase("file")) && this.isEnabled()) {
				boolean print = !args[0].equalsIgnoreCase("file");
				File file = ScriptLoader.getScriptFromName(args[1]);
				if (file != null) {
					if (file.isDirectory()) {
						for (Script script : ScriptLoader.getScripts(file)) {
							outputTimings(sender, script.getConfig().getFileName(), print);
						}
					} else {
						Script script = ScriptLoader.getScript(file);
						assert script != null;
						outputTimings(sender, script.getConfig().getFileName(), print);
					}
				} else {
					Skript.info(sender, "<light red>No script found for '" + args[1] + "'.");
				}
			}
		}
		return true;
	}

	private static final List<String> SUBS = List.of("print", "file", "enable", "disable", "reset");

	@Override
	public @NotNull List<String> handleTabComplete(CommandSender sender, String[] args) {
		if (args.length == 1) {
			return StringUtil.copyPartialMatches(args[0], SUBS, new ArrayList<>());
		} else if (args.length > 1 && (args[0].equalsIgnoreCase("print") ||
			args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("file")) && this.isEnabled()) {
			List<String> list = new ArrayList<>();
			for (Script loadedScript : ScriptLoader.getLoadedScripts()) {
				list.add(loadedScript.getConfig().getFileName());
			}
			return StringUtil.copyPartialMatches(args[1], list, new ArrayList<>());
		}
		return List.of();
	}

	/**
	 * Represents an instance of {@link Timing}
	 * <p>This is used when starting a timing and holding information.</p>
	 */
	private static class TimingInstance implements Timing {
		private final Timeable timeable;
		private final long startTime;

		public TimingInstance(Timeable timeable) {
			this.timeable = timeable;
			this.startTime = System.currentTimeMillis();
		}
	}

	// == SUPPLEMENTAL CLASSES ==

	/**
	 * Represents the timings of a Script.
	 */
	private static class ScriptTiming {

		private final Map<Integer, LineTiming> lineTimings = new TreeMap<>();

		public ScriptTiming() {
		}

		public void stop(TimingInstance timing, boolean async) {
			Timeable timeable = timing.timeable;

			Node node = timeable.getNode();
			if (node != null) {
				int line = node.getLine();
				LineTiming lineTiming;
				if (!lineTimings.containsKey(line)) {
					lineTimings.put(line, new LineTiming(async));
				}
				lineTiming = lineTimings.get(line);

				long endTime = System.currentTimeMillis() - timing.startTime;
				lineTiming.stop(endTime);
			}
		}
	}

	/**
	 * Represents the timings of a line
	 */
	private static class LineTiming {
		private final boolean async;
		private long count = 0;
		private long total = 0;

		public LineTiming(boolean async) {
			this.async = async;
		}

		public boolean isAsync() {
			return this.async;
		}

		private void stop(long time) {
			this.count++;
			this.total += time;
		}

		public TimingResult getResults() {
			long averageTime = 0;
			if (this.count > 0 && this.total > 0) {
				averageTime = this.total / this.count;
			}
			return new TimingResult(this.count, averageTime);
		}
	}

	/**
	 * Represents the returned timings from a {@link LineTiming}
	 *
	 * @param count       How many times the element was timed
	 * @param averageTime The average time it took the element to execute
	 */
	public record TimingResult(long count, long averageTime) {
	}

	// == TIMINGS PRINTER ==
	// TODO Please ignore, this is just for my testing
	@SuppressWarnings({"resource", "DataFlowIssue"})
	public void outputTimings(CommandSender sender, String name, boolean print) {
		File scriptFile = ScriptLoader.getScriptFromName(name);

		StringBuilder builder = new StringBuilder();
		try {
			ScriptTiming scriptTiming = scriptTimings.get(name);

			int line = 0;
			Iterator<String> iterator = Files.lines(scriptFile.toPath()).iterator();
			while (iterator.hasNext()) {
				line++;
				builder.append("<reset>");
				String lineString = iterator.next();
				if (print) {
					lineString = lineString.replace("\t", "    "); // Tabs are huge in console, let's use spaces
					lineString = lineString.replaceAll("<\\w+>|&[a-z0-9]", ""); // Strip out color codes
				}
				LineTiming lineTiming = scriptTiming != null ? scriptTiming.lineTimings.get(line) : null;
				if (lineTiming != null) {
					TimingResult results = lineTiming.getResults();
					long averageTime = results.averageTime();
					String timingColor = "<light green>";
					if (lineTiming.isAsync())
						timingColor = "<light aqua>";
					else if (averageTime > 100)
						timingColor = "<red>";
					else if (averageTime > 50)
						timingColor = "<light red>";
					else if (averageTime > 35)
						timingColor = "<orange>";
					else if (averageTime > 20)
						timingColor = "<yellow>";
					String format = String.format(" <grey>#(%s%s<reset>ms [<light aqua>x%s<reset>]<grey>)",
						timingColor, averageTime, results.count());

					if (print)
						builder.append("<grey>[").append(line).append("] ");

					builder.append(lineString);
					builder.append(format);
					builder.append(System.lineSeparator());
				} else {
					if (lineString.trim().startsWith("#")) {
						builder.append("<black>");
					} else {
						builder.append("<dark grey>");
					}
					if (print)
						builder.append("[").append(line).append("] ");
					builder.append(lineString);
					builder.append(System.lineSeparator());
				}
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		if (print) {
			if (sender instanceof Player)
				Skript.info(sender, "Timings for '" + name + "' have been sent to console.");
			Skript.info(Bukkit.getConsoleSender(), "Timings for '" + name + "':" + System.lineSeparator() + builder);
		} else {
			String path = scriptFile.getAbsolutePath().replace("Skript/scripts", "Skript/timings");
			File file = new File(path);
			file.getParentFile().mkdirs();
			String toPrint = builder.toString().replaceAll("<[\\w ]+>", "");
			try (PrintWriter out = new PrintWriter(file)) {
				out.println(toPrint);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
			Skript.info(sender, "Timing results saved to: " + scriptFile.getPath());
		}
	}

}
