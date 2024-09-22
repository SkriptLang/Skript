package org.skriptlang.skript.bukkit.spark.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.lucko.spark.api.statistic.StatisticWindow.CpuUsage;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spark.SparkUtils;

@Name("CPU Usage")
@RequiredPlugins("Paper 1.21+ or Spark")
@Description({
	"Returns the CPU usage readings for either the process (the server) or the system " +
	"(the machine the server is running on). The expression defaults to returning the process readings.",
	"The readings are the usage percentage of the system's or process's CPU resources " +
	"(1.0 means 100%). Readings can be made of the last 10 seconds, last minute, and last 15 minutes.",
	"This expression can only be used if the server has the Spark plugin installed or is running Paper 1.21+, " +
	"which includes Spark."
})
@Examples({
	"broadcast cpu usage",
	"if system cpu usage over the last minute > 90%:",
		"\tsend \"The server is low on CPU power!\" to console"
})
@Since("INSERT VERSION")
public class ExprCPUUsage extends SimpleExpression<Double> {

	private String expr;
	private CpuUsage[] windows;
	private boolean useProcess;
	private int matchedPattern;

	static {
		Skript.registerExpression(ExprCPUUsage.class, Double.class, ExpressionType.SIMPLE,
				"[the] [:system|process] cpu usage [measurement|value] (over|from|of) the last 10 seconds",
				"[the] [:system|process] cpu usage [measurement|value] (over|from|of) the last [1|one] minute",
				"[the] [:system|process] cpu usage [measurement|value] (over|from|of) the last (15|fifteen) minutes",
				"[the] [:system|process] cpu usage [measurements|values]");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		expr = parseResult.expr;
		useProcess = !parseResult.hasTag("system");
		this.matchedPattern = matchedPattern;
		windows = switch (matchedPattern) {
			case 0 -> new CpuUsage[]{CpuUsage.SECONDS_10};
			case 1 -> new CpuUsage[]{CpuUsage.MINUTES_1};
			case 2 -> new CpuUsage[]{CpuUsage.MINUTES_15};
			case 3 -> CpuUsage.values();
			default -> throw new IllegalStateException("matched invalid pattern: " + matchedPattern);
		};
		return true;
	}

	@Override
	protected Double @Nullable [] get(Event event) {
		return useProcess ? SparkUtils.cpuProcess(windows) : SparkUtils.cpuSystem(windows);
	}

	@Override
	public boolean isSingle() {
		return matchedPattern != 3;
	}

	@Override
	public Class<? extends Double> getReturnType() {
		return Double.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return expr;
	}

}
