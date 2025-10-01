package org.skriptlang.skript.bukkit.itemcomponents.consumable.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.consumable.ConsumableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.consumable.ConsumableWrapper;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.time.temporal.ChronoUnit;

@Name("Consumable Component - Consume Time")
@Description("""
	The time it takes for an item to be consumed.
	NOTE: Consumable component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("set the consumption time of {_item} to 5 seconds")
@RequiredPlugins("Minecraft 1.21.3+")
@Since("INSERT VERSION")
public class ExprConsCompTime extends SimplePropertyExpression<ConsumableWrapper, Timespan> implements ConsumableExperimentSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprConsCompTime.class, Timespan.class, "consum(e|ption) time", "consumablecomponents", true)
				.supplier(ExprConsCompTime::new)
				.build()
		);
	}

	@Override
	public @Nullable Timespan convert(ConsumableWrapper wrapper) {
		float seconds = wrapper.getComponent().consumeSeconds();
		return new Timespan(TimePeriod.SECOND, (long) seconds);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, REMOVE, ADD -> CollectionUtils.array(Timespan.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		float seconds = delta == null ? 0f : ((Timespan) delta[0]).get(ChronoUnit.SECONDS);
		float finalSeconds = Math2.fit(0, seconds, Float.MAX_VALUE);
		getExpr().stream(event).forEach(wrapper -> {
			float current = wrapper.getComponent().consumeSeconds();
			switch (mode) {
				case SET, DELETE -> current = finalSeconds;
				case ADD -> current = Math2.fit(0, current + finalSeconds, Float.MAX_VALUE);
				case REMOVE -> current = Math2.fit(0, current - finalSeconds, Float.MAX_VALUE);
			}
			float finalCurrent = current;
			wrapper.editBuilder(builder -> builder.consumeSeconds(finalCurrent));
		});
	}

	@Override
	public Class<Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "consume time";
	}

}
