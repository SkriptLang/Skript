package org.skriptlang.skript.bukkit.entity.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Pickup Delay")
@Description("The amount of time before a dropped item can be picked up by an entity.")
@Example("drop diamond sword at {_location} without velocity")
@Example("set pickup delay of last dropped item to 5 seconds")
@Since("2.7")
public class ExprPickupDelay extends SimplePropertyExpression<Entity, Timespan> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprPickupDelay.class, Timespan.class, "pick[ ]up delay", "entities", false)
				.supplier(ExprPickupDelay::new)
				.build()
		);
	}

	@Override
	public @Nullable Timespan convert(Entity entity) {
		if (!(entity instanceof Item item))
			return null;
		return new Timespan(Timespan.TimePeriod.TICK, item.getPickupDelay());
	}


	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, RESET, DELETE, REMOVE -> CollectionUtils.array(Timespan.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Entity[] entities = getExpr().getArray(event);
		int change = delta == null ? 0 : (int) ((Timespan) delta[0]).getAs(Timespan.TimePeriod.TICK);
		if (mode == ChangeMode.REMOVE)
			change = -change;

		for (Entity entity : entities)  {
			if (!(entity instanceof Item item))
				continue;
			switch (mode) {
				case SET, DELETE, RESET -> item.setPickupDelay(change);
				case ADD, REMOVE -> item.setPickupDelay(item.getPickupDelay() + change);
			}
		}
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "pickup delay";
	}

}
