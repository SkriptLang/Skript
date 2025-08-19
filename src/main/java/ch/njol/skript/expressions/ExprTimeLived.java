package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Time Lived of Entity")
@Description("""
	Returns the total amount of time the entity has lived.
	Note: This does not reset when a player dies.
	""")
@Example("""
	set {_target} to player's target entity
	send "%{_target}% has lived for %time lived of {_target}%"

	spawn zombie at player:
		set {_boss} to zombie
	# start off with 1 tick lived instead of 0 to avoid custom effects running off spawn
	add 1 tick to {_boss}'s time lived
	while {_boss} is alive:
		wait 1 tick
		# update display name every tick with hp
		set display name of {_boss} to "Boss %{_boss}'s health%/%{_boss}'s max health%"
		# heal every second
		if mod(ticks of {_boss}'s time lived, 20) is 0:
			add 1 to {_boss}'s health
		# push forward every 2 seconds
		if mod(ticks of {_boss}'s time lived, 40) is 0:
			push {_boss} forward at speed 1
		# give strength every 10 seconds
		if mod(ticks of {_boss}'s time lived, 200) is 0:
			add potion effect of strength for 3 seconds to {_boss}'s potion effects
	""")
@Since("INSERT VERSION")
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
		if (mode == ChangeMode.REMOVE_ALL || mode == ChangeMode.DELETE)
			return null;
		return CollectionUtils.array(Timespan.class);
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Entity entity = getExpr().getSingle(event);
		if (entity == null) return;

		int newTicks = 1;
		if (delta != null && delta[0] instanceof Timespan timespan) {
			newTicks = (int) timespan.get(Timespan.TimePeriod.TICK);
		}

		int currentTicks = entity.getTicksLived();
		int valueToSet = switch (mode) {
			case ADD -> currentTicks + newTicks;
			case REMOVE -> currentTicks - newTicks;
			case SET, RESET -> newTicks;
			default -> currentTicks;
		};

		entity.setTicksLived(Math.max(1, valueToSet));
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
