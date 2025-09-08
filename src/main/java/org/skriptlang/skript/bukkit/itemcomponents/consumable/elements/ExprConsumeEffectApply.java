package org.skriptlang.skript.bukkit.itemcomponents.consumable.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.consumable.ConsumeEffectExperimentalSyntax;

import java.util.ArrayList;
import java.util.List;

@Name("Consume Effect - Apply Effects")
@Description("""
	A consume effect that applies the provided potions when the item has been consumed.
	The probability is the chance the potions get applied when the item has been consumed.
	Consume effects have to be added to the consumable component of an item.
	NOTE: Consume Effect elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	set {_effect} to a consume effect to apply (a new potion effect of strength of tier 3 for 1 hour) with a probability of 100
	add {_effect} to the consume effects of {_item}
	""")
@RequiredPlugins("Minecraft 1.21.3+")
@Since("INSERT VERSION")
@SuppressWarnings("UnstableApiUsage")
public class ExprConsumeEffectApply extends SimpleExpression<ConsumeEffect> implements ConsumeEffectExperimentalSyntax {

	static {
		Skript.registerExpression(ExprConsumeEffectApply.class, ConsumeEffect.class, ExpressionType.COMBINED,
			"[a] consume effect to apply %potioneffects% with [a] probability of %number%");
	}

	private Expression<PotionEffect> effects;
	private Expression<Number> probability;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		effects = (Expression<PotionEffect>) exprs[0];
		//noinspection unchecked
		probability = (Expression<Number>) exprs[1];
		return true;
	}

	@Override
	protected ConsumeEffect @Nullable [] get(Event event) {
		List<PotionEffect> potions = new ArrayList<>(effects.stream(event).toList());
		if (potions.isEmpty())
			return null;

		Number number = this.probability.getSingle(event);
		if (number == null)
			return null;
		float probability = Math2.fit(0, number.floatValue(), 100) / 100;

		ConsumeEffect effect = ConsumeEffect.applyStatusEffects(potions, probability);
		return new ConsumeEffect[] {effect};
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
		if (effects instanceof Literal<PotionEffect> && probability instanceof Literal<Number>)
			return SimplifiedLiteral.fromExpression(this);
		return this;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("a consume effect to apply", effects)
			.append("with a probability of", probability)
			.toString();
	}

}
