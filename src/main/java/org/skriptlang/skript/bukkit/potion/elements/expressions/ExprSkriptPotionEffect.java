package org.skriptlang.skript.bukkit.potion.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.elements.expressions.ExprSecPotionEffect.PotionEffectSectionEvent;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Created Potion Effect")
@Description("An expression to obtain the potion effect being made in a potion effect creation section.")
@Example("""
	set {_potion} to a potion effect of speed 2 for 10 minutes:
		hide the effect's icon
		hide the effect's particles
	""")
@Since("INSERT VERSION")
public class ExprSkriptPotionEffect extends EventValueExpression<SkriptPotionEffect> {

	public static void register(SyntaxRegistry registry) {
		register(registry, ExprSkriptPotionEffect.class, SkriptPotionEffect.class, "[created] [potion] effect");
	}

	public ExprSkriptPotionEffect() {
		super(SkriptPotionEffect.class);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PotionEffectSectionEvent.class)) {
			Skript.error("The 'created potion effect' is only usable in a potion effect creation section.");
			return false;
		}
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the created potion effect";
	}

}
