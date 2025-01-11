package ch.njol.skript.timings;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

	// TODO Please ignore, this is just for my testing
	@SuppressWarnings({"resource", "DataFlowIssue"})
	public void printTimings(CommandSender sender, String name) {
		File scriptFile = ScriptLoader.getScriptFromName(name);

		StringBuilder builder = new StringBuilder();
		try {
			ScriptTiming scriptTiming = scriptTimings.get(name);

			int line = 0;
			Iterator<String> iterator = Files.lines(scriptFile.toPath()).iterator();
			while (iterator.hasNext()) {
				line++;
				String lineString = iterator.next().replace("\t", "    ");
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
					String format = String.format(" <grey>(%s%s<reset>ms [<light aqua>x%s<reset>]<grey>)",
						timingColor, averageTime, results.count());
					builder
						.append(lineString)
						.append(Utils.replaceEnglishChatStyles(format))
						.append(System.lineSeparator());
				} else {
					if (lineString.trim().startsWith("#")) {
						builder.append(Utils.replaceEnglishChatStyles("<dark grey>"));
					} else {
						builder.append(Utils.replaceEnglishChatStyles("<light grey>"));
					}
					builder.append(lineString).append(System.lineSeparator());
				}
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		if (sender instanceof Player)
			Skript.info(sender, "Timings for '" + name + "' have been sent to console.");
		Skript.info(Bukkit.getConsoleSender(), "Timings for '" + name + "':" + System.lineSeparator() + builder);
	}

	// TODO javadocs
	private static class TimingInstance implements Timing {
		private final Timeable timeable;
		private final long startTime;

		public TimingInstance(Timeable timeable) {
			this.timeable = timeable;
			this.startTime = System.currentTimeMillis();
		}
	}

	@Override
	public boolean handleCommand(CommandSender sender, String[] args) {
		if (args.length > 1) {
			if (args[0].equalsIgnoreCase("enable")) {
				setEnabled(true);
			} else if (args[0].equalsIgnoreCase("disable")) {
				setEnabled(false);
			} else if (args[0].equalsIgnoreCase("print") && this.isEnabled()) {
				File file = ScriptLoader.getScriptFromName(args[1]);
				if (file != null) {
					if (file.isDirectory()) {
						Set<Script> scripts = ScriptLoader.getScripts(file);
						for (Script script : scripts) {
							printTimings(sender, script.getConfig().getFileName());
						}
					} else {
						Script script = ScriptLoader.getScript(file);
						printTimings(sender, script.getConfig().getFileName());
					}
				}
			}
		}
		return true;
	}

	@Override
	public @NotNull List<String> handleTabComplete(CommandSender sender, String[] args) {
		if (args.length == 1) {
			List<String> subs = List.of("print", "enable", "disable");
			return StringUtil.copyPartialMatches(args[0], subs, new ArrayList<>());
		} else if (args.length > 1 && args[0].equalsIgnoreCase("print") && this.isEnabled()) {
			List<String> list = new ArrayList<>();
			for (Script loadedScript : ScriptLoader.getLoadedScripts()) {
				list.add(loadedScript.getConfig().getFileName());
			}
			return StringUtil.copyPartialMatches(args[1], list, new ArrayList<>());
		}
		return List.of();
	}

	// TODO javadocs
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

	// TODO javadocs
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

	public record TimingResult(long count, long averageTime) {
	}

}
