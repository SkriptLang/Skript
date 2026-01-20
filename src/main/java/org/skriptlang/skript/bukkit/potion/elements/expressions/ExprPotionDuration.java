package org.skriptlang.skript.bukkit.potion.elements.expressions;

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
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.PotionUtils;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Potion Effect - Duration")
@Description("An expression to obtain the duration of a potion effect.")
@Example("set the duration of {_potion} to 10 seconds")
@Example("add 10 seconds to the duration of the player's speed effect")
@Since("2.14")
public class ExprPotionDuration extends SimplePropertyExpression<SkriptPotionEffect, Timespan> {

	public static void register(SyntaxRegistry registry, Origin origin) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprPotionDuration.class, Timespan.class,
			"([potion] duration|potion length)[s]", "skriptpotioneffects", true)
				.supplier(ExprPotionDuration::new)
				.origin(origin)
				.build());
	}

	@Override
	public Timespan convert(SkriptPotionEffect potionEffect) {
		if (potionEffect.infinite()) {
			return Timespan.infinite();
		}
		return new Timespan(TimePeriod.TICK, potionEffect.duration());
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (!SkriptPotionEffect.isChangeable(getExpr())) {
			return null;
		}
		return switch (mode) {
			case ADD, SET, REMOVE, RESET -> CollectionUtils.array(Timespan.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Timespan change = delta != null ? (Timespan) delta[0] : new Timespan(TimePeriod.TICK, PotionUtils.DEFAULT_DURATION_TICKS);
		for (SkriptPotionEffect potionEffect : getExpr().getArray(event)) {
			changeSafe(potionEffect, change, mode);
		}
	}

	/**
	 * Changes the duration of a potion effect while accounting for bounds and properties such as infinite durations.
	 * @param potionEffect The potion effect to change.
	 * @param change The timespan delta.
	 * @param mode The mode of change to perform.
	 */
	static void changeSafe(SkriptPotionEffect potionEffect, Timespan change, ChangeMode mode) {
		Timespan duration;
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET) {
			duration = change;
		} else {
			if (potionEffect.infinite()) { // add/remove would have no effect
				return;
			}
			duration = new Timespan(TimePeriod.TICK, potionEffect.duration());
			if (mode == ChangeMode.ADD) {
				duration = duration.add(change);
			} else {
				duration = duration.subtract(change);
			}
		}
		if (duration.isInfinite()) {
			potionEffect.infinite(true);
		} else {
			potionEffect.duration((int) Math2.fit(0, duration.getAs(TimePeriod.TICK), Integer.MAX_VALUE));
		}
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "duration";
	}

}
