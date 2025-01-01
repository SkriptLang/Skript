package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;

@Name("Active Item Use Time")
@Description({
	"Returns the time that the entities have either spent using an item, " +
		"or the time left for them to finish using an item.",
	"If an entity is not using any item, this will return 0 seconds."
})
@Examples({
	"on right click:",
		"\tbroadcast player's remaining item use time",
		"\twait 1 second",
		"\tbroadcast player's item use time"
})
@Since("2.8.0")
@RequiredPlugins("Paper")
public class ExprEntityItemUseTime extends SimplePropertyExpression<LivingEntity, Timespan> {

	static {
		if (Skript.methodExists(LivingEntity.class, "getItemUseRemainingTime"))
			register(ExprEntityItemUseTime.class, Timespan.class, "[elapsed|:remaining] (item|tool) us[ag]e time", "livingentities");
	}

	private boolean remaining;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		remaining = parseResult.hasTag("remaining");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public Timespan convert(LivingEntity livingEntity) {
		if (remaining)
			return new Timespan(Timespan.TimePeriod.TICK, livingEntity.getItemUseRemainingTime());
		return new Timespan(Timespan.TimePeriod.TICK, livingEntity.getHandRaisedTime());
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return (remaining ? "remaining" : "elapsed") + " item usage time";
	}

}
