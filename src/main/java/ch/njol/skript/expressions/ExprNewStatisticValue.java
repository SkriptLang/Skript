package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.jetbrains.annotations.Nullable;

@Name("New/Previous Statistic Value")
@Description("Get the new or previous value of a statistic after it has been incremented.")
@Examples({
	"on player statistic change:",
		"\tset {_new} to new statistic value",
		"\tset {_old} to previous statistic value"
})
@Since("INSERT VERSION")
@Events("Player Statistic Change")
public class ExprNewStatisticValue extends SimpleExpression<Number> implements EventRestrictedSyntax {

	static {
		Skript.registerExpression(ExprNewStatisticValue.class, Number.class, ExpressionType.SIMPLE,
			"(future|new) statistic value", "(past|previous) statistic value");
	}

	private boolean future;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		future = matchedPattern == 0;
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(PlayerStatisticIncrementEvent.class);
	}

	@Override
	protected Number @Nullable [] get(Event event) {
		PlayerStatisticIncrementEvent statisticEvent =  ((PlayerStatisticIncrementEvent) event);
		if (future)
			return new Number[] {statisticEvent.getNewValue()};
		return new Number[] {statisticEvent.getPreviousValue()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (future ? "future " : "past ") + "statistic value";
	}

}
