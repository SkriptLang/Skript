package org.skriptlang.skript.bukkit.itemcomponents.blocking.elements;

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
import org.skriptlang.skript.bukkit.itemcomponents.blocking.BlockingExperimentalSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.BlockingWrapper;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Blocking Component - Delay Time")
@Description("""
	The amount of time before an item can block an attack after starting to block.
	NOTE: Blocking component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("set {_time} to the blocking delay time of {_item}")
@Example("set the blocking delay time of {_item} to 5 ticks")
@RequiredPlugins("Minecraft 1.21.5+")
@Since("INSERT VERSION")
public class ExprBlockCompDelay extends SimplePropertyExpression<BlockingWrapper, Timespan> implements BlockingExperimentalSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprBlockCompDelay.class, Timespan.class, "blocking delay time[s]", "blockingcomponents", true)
				.supplier(ExprBlockCompDelay::new)
				.build()
		);
	}
	
	@Override
	public @Nullable Timespan convert(BlockingWrapper wrapper) {
		return new Timespan(TimePeriod.SECOND, (long) wrapper.getComponent().blockDelaySeconds());
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
		float provided = delta == null ? 0f : ((Timespan) delta[0]).getAs(TimePeriod.SECOND);

		getExpr().stream(event).forEach(wrapper -> {
			float current = wrapper.getComponent().blockDelaySeconds();
			switch (mode) {
				case SET, DELETE -> current = provided;
				case ADD -> current += provided;
				case REMOVE -> current -= provided;
			}
			float newDelay = Math2.fit(0, current, Float.MAX_VALUE);
			wrapper.editBuilder(builder -> builder.blockDelaySeconds(newDelay));
		});
	}

	@Override
	public Class<Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "blocking delay time";
	}
	
}
