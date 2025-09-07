package org.skriptlang.skript.bukkit.itemcomponents.consumable.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.consumable.ConsumableExperimentSyntax;

@SuppressWarnings("UnstableApiUsage")
public class LitConsumeEffectClear extends SimpleLiteral<ConsumeEffect> implements ConsumableExperimentSyntax {

	static {
		Skript.registerExpression(LitConsumeEffectClear.class, ConsumeEffect.class, ExpressionType.SIMPLE,
			"[a] consume effect to clear all potion effects");
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
