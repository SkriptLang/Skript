package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;

@Name("Time Spent Wielding an Item")
@Description({
	"Returneth the time that the entities have either spent employing an item, " +
		"or the time remaining for them to finish employing said item.",
	"If an entity doth not wield any item, this shall return 0 seconds."
})
@Example("""
	on right click:
		broadcast player's remaining item use time
		wait 1 second
		broadcast player's item use time
	""")
@Since("2.8.0")
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
