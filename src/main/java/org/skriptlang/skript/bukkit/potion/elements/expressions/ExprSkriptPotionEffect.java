package org.skriptlang.skript.bukkit.potion.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.EventRestrictedSyntax;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.elements.expressions.ExprSecPotionEffect.PotionEffectSectionEvent;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Created Potion Effect")
@Description("An expression to obtain the potion effect being made in a potion effect creation section.")
@Example("""
	set {_potion} to a potion effect of speed 2 for 10 minutes:
		hide the effect's icon
		hide the effect's particles
	""")
@Since("2.14")
public class ExprSkriptPotionEffect extends EventValueExpression<SkriptPotionEffect> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry registry, Origin origin) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprSkriptPotionEffect.class, SkriptPotionEffect.class,
			"[created] [potion] effect")
				.supplier(ExprSkriptPotionEffect::new)
				.origin(origin)
				.build());
	}

	public ExprSkriptPotionEffect() {
		super(SkriptPotionEffect.class);
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		//noinspection unchecked
		return new Class[]{PotionEffectSectionEvent.class};
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the created potion effect";
	}

}
