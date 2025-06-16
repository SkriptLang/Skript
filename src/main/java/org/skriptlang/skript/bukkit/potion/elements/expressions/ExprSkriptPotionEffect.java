package org.skriptlang.skript.bukkit.potion.elements.expressions;

import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.expressions.base.EventValueExpression;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.registration.SyntaxRegistry;

@NoDoc
public class ExprSkriptPotionEffect extends EventValueExpression<SkriptPotionEffect> {

	public static void register(SyntaxRegistry registry) {
		register(registry, ExprSkriptPotionEffect.class, SkriptPotionEffect.class, "[created] [potion] effect");
	}

	public ExprSkriptPotionEffect() {
		super(SkriptPotionEffect.class);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the created potion effect";
	}

}
