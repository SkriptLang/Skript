package org.skriptlang.skript.bukkit.potion.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Potion Effect - Amplifier")
@Description("An expression to obtain the amplifier of a potion effect.")
@Example("set the amplifier of {_potion} to 10")
@Example("add 10 to the amplifier of the player's speed effect")
@Since("2.7, INSERT VERSION (support for potion effect objects, changing)")
public class ExprPotionAmplifier extends SimplePropertyExpression<SkriptPotionEffect, Integer> {

	public static void register(SyntaxRegistry registry) {
		registerDefault(registry, ExprPotionAmplifier.class, Integer.class,
				"[potion] (amplifier|tier|level)[s]", "skriptpotioneffects");
	}

	@Override
	public Integer convert(SkriptPotionEffect potionEffect) {
		return potionEffect.amplifier() + 1;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, SET, REMOVE -> CollectionUtils.array(Integer.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		int change = (int) delta[0];
		for (SkriptPotionEffect potionEffect : getExpr().getArray(event)) {
			// need to subtract 1 for setting
			int base = mode == ChangeMode.SET ? -1 : potionEffect.amplifier();
			if (mode == ChangeMode.REMOVE) {
				change = -change;
			}
			potionEffect.amplifier(Math2.fit(Integer.MIN_VALUE, change + base, Integer.MAX_VALUE));
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "amplifier";
	}

}
