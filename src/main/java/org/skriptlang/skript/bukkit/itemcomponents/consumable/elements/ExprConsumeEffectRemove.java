package org.skriptlang.skript.bukkit.itemcomponents.consumable.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.consumable.ConsumableExperimentSyntax;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class ExprConsumeEffectRemove extends SimpleExpression<ConsumeEffect> implements ConsumableExperimentSyntax {

	static {
		Skript.registerExpression(ExprConsumeEffectRemove.class, ConsumeEffect.class, ExpressionType.PROPERTY,
			"[a] consume effect to remove %potioneffecttypes%");
	}

	private Expression<PotionEffectType> effectTypes;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		effectTypes = (Expression<PotionEffectType>) exprs[0];
		return true;
	}

	@Override
	protected ConsumeEffect @Nullable [] get(Event event) {
		List<PotionEffectType> types = new ArrayList<>(effectTypes.stream(event).toList());
		if (types.isEmpty())
			return null;


		return new ConsumeEffect[0] ;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<ConsumeEffect> getReturnType() {
		return ConsumeEffect.class;
	}

	@Override
	public Expression<? extends ConsumeEffect> simplify() {
		if (effectTypes instanceof Literal<PotionEffectType>)
			return SimplifiedLiteral.fromExpression(this);
		return this;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("a consume effect to remove", effectTypes)
			.toString();
	}

}
