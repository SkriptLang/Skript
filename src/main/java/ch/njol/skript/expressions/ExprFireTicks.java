package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Entity Fire Burn Duration")
@Description("How much time an entity will be burning for.")
@Examples({
	"send \"You will stop burning in %fire time of player%\"",
	"send the max burn time of target"
})
@Since("2.7, 2.10 (maximum)")
public class ExprFireTicks extends SimplePropertyExpression<Entity, Timespan> {

	static {
		register(ExprFireTicks.class, Timespan.class, "[:max[imum]] (burn[ing]|fire) (time|duration)", "entities");
	}

	private boolean max;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		max = (parseResult.hasTag("max"));
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Timespan convert(Entity entity) {
		return new Timespan(TimePeriod.TICK, (max ? entity.getMaxFireTicks() : Math.max(entity.getFireTicks(), 0)));
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (max)
			return null;
		return switch (mode) {
			case ADD, SET, RESET, DELETE, REMOVE -> CollectionUtils.array(Timespan.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		Entity[] entities = getExpr().getArray(event);
		int change = delta == null ? 0 : (int) ((Timespan) delta[0]).getAs(Timespan.TimePeriod.TICK);
		switch (mode) {
			case REMOVE:
				change = -change;
			case ADD:
				for (Entity entity : entities)
					entity.setFireTicks(entity.getFireTicks() + change);
				break;
			case DELETE:
			case RESET:
			case SET:
				for (Entity entity : entities)
					entity.setFireTicks(change);
				break;
			default:
				assert false;
		}
	}

	@Override
	public Class<Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "fire time";
	}

}
