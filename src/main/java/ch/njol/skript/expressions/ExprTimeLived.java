package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Time Lived of Entity")
@Description("""
	Returns the total amount of time the entity has lived.
	Note: This does not reset when a player dies.
	""")
@Example("clear all entities where [input's time lived > 1 hour]")
@Example("""
	on right click on entity:
		send "%entity% has lived for %time lived of entity%" to player
	""")
@Since("2.13")
public class ExprTimeLived extends SimplePropertyExpression<Entity, Timespan> {

	static {
		register(ExprTimeLived.class, Timespan.class, "time (alive|lived)", "entities");
	}

	@Override
	public @Nullable Timespan convert(Entity entity) {
		return new Timespan(TimePeriod.TICK, entity.getTicksLived());
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, REMOVE, SET, RESET -> CollectionUtils.array(Timespan.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		long newTicks = 1;
		if (delta != null && delta[0] instanceof Timespan timespan) {
			newTicks = (int) timespan.get(Timespan.TimePeriod.TICK);
		}

		for (Entity entity : getExpr().getArray(event)) {
			int currentTicks = entity.getTicksLived();
			long valueToSet = switch (mode) {
				case ADD -> currentTicks + newTicks;
				case REMOVE -> currentTicks - newTicks;
				case SET, RESET -> newTicks;
				default -> currentTicks;
			};

			entity.setTicksLived((int) Math2.fit(1, valueToSet, Integer.MAX_VALUE));
		}
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "time lived";
	}

}
