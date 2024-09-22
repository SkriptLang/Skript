package org.skriptlang.skript.bukkit.spark.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.lucko.spark.api.statistic.StatisticWindow.MillisPerTick;
import me.lucko.spark.api.statistic.misc.DoubleAverageInfo;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spark.MsptStatistic;
import org.skriptlang.skript.bukkit.spark.SparkUtils;

import java.util.Arrays;

@Name("MSPT Usage")
@RequiredPlugins("Paper 1.21+ or Spark")
@Description({
    "Returns the MSPT (milliseconds per tick) readings from the last 10 seconds, 1 minute and 5 minutes. " +
	"The returned values will be numbers which represent milliseconds, rather than timespans.",
	"The average, minimum, maximum, and median values are available, as well as any percentile between 0 and 100.",
    "This expression can only be used if the server has Spark or you have Spark installed as a plugin."
})
@Examples({
	"broadcast the average mspt over the last 10 seconds",
	"broadcast the minimum and maximum mspt measurements from the last minute",
	"broadcast the 66.6th percentile and 95th percentile milliseconds per tick values from the last 5 minutes"
})
public class ExprMSPT extends SimpleExpression<Double> {

	static {
		Skript.registerExpression(ExprMSPT.class, Double.class, ExpressionType.SIMPLE,
			"[the] %*msptstatistics% (milliseconds per tick|mspt) [measurement[s]|value[s]] (over|from|of) the last 10 seconds",
			"[the] %*msptstatistics% (milliseconds per tick|mspt) [measurement[s]|value[s]] (over|from|of) the last [1|one] minute",
			"[the] %*msptstatistics% (milliseconds per tick|mspt) [measurement[s]|value[s]] (over|from|of) the last (5|five) minutes");
	}

	private int matchedPattern;
	private MillisPerTick window;
	private Literal<MsptStatistic> stats;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!SparkUtils.supportsMspt()) {
			Skript.error("Your platform does not support measuring MSPT values!");
			return false;
		}
		this.matchedPattern = matchedPattern;
		//noinspection unchecked
		this.stats = ((Literal<MsptStatistic>) expressions[0]);
		this.window = MillisPerTick.values()[matchedPattern];
		return true;
	}

	@Override
	protected Double @Nullable [] get(Event event) {
		DoubleAverageInfo info = SparkUtils.mspt(window);
		return Arrays.stream(stats.getArray()).map(stat-> stat.get(info)).toArray(Double[]::new);
	}

	@Override
	public boolean isSingle() {
		return stats.isSingle();
	}

	@Override
	public Class<? extends Double> getReturnType() {
		return Double.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + stats.toString(event, debug) + " milliseconds per tick measurements from the last " +
				switch (matchedPattern) {
					case 0 -> "10 seconds";
					case 1 -> "minute";
					case 2 -> "5 minutes";
					default -> throw new IllegalStateException("invalid matched pattern");
				};
	}

}
