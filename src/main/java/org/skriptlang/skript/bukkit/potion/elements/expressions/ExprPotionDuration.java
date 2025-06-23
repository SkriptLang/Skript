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
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.PotionUtils;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Potion Effect - Duration")
@Description("An expression to obtain the duration of a potion effect.")
@Example("set the duration of {_potion} to 10 seconds")
@Example("add 10 seconds to the duration of the player's speed effect")
@Since("INSERT VERSION")
public class ExprPotionDuration extends SimplePropertyExpression<SkriptPotionEffect, Timespan> {

	public static void register(SyntaxRegistry registry) {
		registerDefault(registry, ExprPotionDuration.class, Timespan.class,
			"[potion] (duration|length)[s]", "skriptpotioneffects");
	}

	@Override
	public Timespan convert(SkriptPotionEffect potionEffect) {
		return new Timespan(TimePeriod.TICK, potionEffect.infinite() ? Long.MAX_VALUE : potionEffect.duration());
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
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

	static void changeSafe(SkriptPotionEffect potionEffect, Timespan change, ChangeMode mode) {
		Timespan duration;
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET) {
			duration = change;
		} else {
			if (potionEffect.infinite()) { // add/remove should not affect infinite potions
				return;
			}
			duration = new Timespan(TimePeriod.TICK, potionEffect.duration());
			if (mode == ChangeMode.ADD) {
				duration = duration.add(change);
			} else {
				duration = duration.subtract(change);
			}
		}
		potionEffect.duration((int) Math2.fit(0, duration.getAs(TimePeriod.TICK), Integer.MAX_VALUE));
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
