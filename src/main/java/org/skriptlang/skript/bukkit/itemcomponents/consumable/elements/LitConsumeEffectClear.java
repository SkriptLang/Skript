package org.skriptlang.skript.bukkit.itemcomponents.consumable.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.consumable.ConsumeEffectExperimentalSyntax;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Consume Effect - Clear Effects")
@Description("""
	A consume effect that clears all potions when the item has been consumed.
	Consume effects have to be added to the consumable component of an item.
	NOTE: Consume Effect elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	set {_effect} to a consume effect to clear all potion effects
	add {_effect} to the consume effects of {_item}
	""")
@RequiredPlugins("Minecraft 1.21.3+")
@Since("INSERT VERSION")
@SuppressWarnings("UnstableApiUsage")
public class LitConsumeEffectClear extends SimpleLiteral<ConsumeEffect> implements ConsumeEffectExperimentalSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.builder(LitConsumeEffectClear.class, ConsumeEffect.class)
				.addPatterns("[a] consume effect to clear all (potion|status) effects")
				.supplier(LitConsumeEffectClear::new)
				.build()
		);
	}

	public LitConsumeEffectClear() {
		super(ConsumeEffect.clearAllStatusEffects(), false);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "a consume effect to clear all potion effects";
	}

}
