package org.skriptlang.skript.bukkit.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.CopperGolem;
import org.bukkit.entity.CopperGolem.Oxidizing;
import org.bukkit.entity.CopperGolem.Oxidizing.AtTime;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Time Until Oxidation")
@Description("""
	The time until a copper golem oxidizes to its next state. (Normal -> Exposed -> Weathered -> Oxidized)
	Copper golems that are waxed do not go through oxidation.
	Setting or resetting the time until oxidation on a waxed copper golem will remove the waxed state.
	Resetting the time until oxidation uses vanilla behavior of generating a random time between 7 hours and 7 hours 40 minutes.
	""")
@Example("set {_time} to the time until oxidation of last spawned copper golem")
@Example("set the time until oxidation of last spawned copper golem to 10 seconds")
@Example("clear the time until oxidation of last spawned copper golem")
@RequiredPlugins("Minecraft 1.21.9+")
@Since("INSERT VERSION")
public class ExprCopperGolemOxidationTime extends SimplePropertyExpression<Entity, Timespan> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprCopperGolemOxidationTime.class,
				Timespan.class,
				"time until oxidation",
				"entities",
				false
			).supplier(ExprCopperGolemOxidationTime::new)
				.build()
		);
	}

	@Override
	public @Nullable Timespan convert(Entity entity) {
		if (entity instanceof CopperGolem golem) {
			if (!(golem.getOxidizing() instanceof AtTime atTime))
				return null;
			long worldTime = golem.getWorld().getGameTime();
			long oxidationTime = atTime.time();
			if (worldTime > oxidationTime)
				return null;
			return new Timespan(TimePeriod.TICK, oxidationTime - worldTime);
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET)
			return CollectionUtils.array(Timespan.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			assert delta != null;
			long ticks = ((Timespan) delta[0]).getAs(TimePeriod.TICK);
			for (Entity entity : getExpr().getArray(event)) {
				if (!(entity instanceof CopperGolem golem))
					continue;
				long worldTime = golem.getWorld().getGameTime();
				golem.setOxidizing(Oxidizing.atTime(worldTime + ticks));
			}
		} else if (mode == ChangeMode.RESET) {
			for (Entity entity : getExpr().getArray(event)) {
				if (!(entity instanceof CopperGolem golem))
					continue;
				golem.setOxidizing(Oxidizing.unset());
			}
		}
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "time until oxidation";
	}

}
