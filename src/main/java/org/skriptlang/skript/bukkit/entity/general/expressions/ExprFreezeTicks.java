package org.skriptlang.skript.bukkit.entity.general.expressions;

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
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Freeze Time")
@Description("How much time an entity has been in powdered snow for.")
@Example("""
	player's freeze time is less than 3 seconds:
		send "you're about to freeze!" to the player
	""")
@Since("2.7")
public class ExprFreezeTicks extends SimplePropertyExpression<Entity, Timespan> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprFreezeTicks.class, Timespan.class, "freeze time", "entities", false)
				.supplier(ExprFreezeTicks::new)
				.build()
		);
	}

	@Override
	public @Nullable Timespan convert(Entity entity) {
		return new Timespan(Timespan.TimePeriod.TICK, entity.getFreezeTicks());
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, ADD, REMOVE, RESET -> CollectionUtils.array(Timespan.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int time = delta == null ? 0 : (int) ((Timespan) delta[0]).getAs(TimePeriod.TICK);
		int newTime;
		for (Entity entity : getExpr().getArray(event)) {
			switch (mode) {
				case SET, DELETE, RESET -> setFreezeTicks(entity, time);
				case ADD -> setFreezeTicks(entity, entity.getFreezeTicks() + time);
				case REMOVE -> setFreezeTicks(entity, entity.getFreezeTicks() - time);
			}
		}
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "freeze time";
	}

	private void setFreezeTicks(Entity entity, int ticks) {
		//Limit time to between 0 and max
		if (ticks < 0)
			ticks = 0;
		// Set new time
		entity.setFreezeTicks(ticks);
	}

}
