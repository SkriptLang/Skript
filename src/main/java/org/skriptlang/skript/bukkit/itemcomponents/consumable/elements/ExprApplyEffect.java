package org.skriptlang.skript.bukkit.itemcomponents.consumable.elements;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.consumable.ConsumableExperimentSyntax;

public class ExprApplyEffect extends SimpleExpression<ConsumeEffect> implements ConsumableExperimentSyntax {

	static {

	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return false;
	}

	@Override
	protected ConsumeEffect @Nullable [] get(Event event) {
		return new ConsumeEffect[0];
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends ConsumeEffect> getReturnType() {
		return ConsumeEffect.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "";
	}

}
