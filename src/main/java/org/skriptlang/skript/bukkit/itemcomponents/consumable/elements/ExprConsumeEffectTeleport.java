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
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.consumable.ConsumeEffectExperimentalSyntax;

@Name("Consume Effect - Teleport Randomly")
@Description("""
	A consume effect that teleports randomly in the provided radius or diameter when the item has been consumed.
	Consume effects have to be added to the consumable component of an item.
	NOTE: Consume Effect elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	set {_effect} to a consume effect to teleport randomly in a radius of 5
	add {_effect} to the consume effects of {_item}
	""")
@RequiredPlugins("Minecraft 1.21.3+")
@Since("INSERT VERSION")
@SuppressWarnings("UnstableApiUsage")
public class ExprConsumeEffectTeleport extends SimpleExpression<ConsumeEffect> implements ConsumeEffectExperimentalSyntax {

	static {
		Skript.registerExpression(ExprConsumeEffectTeleport.class, ConsumeEffect.class, ExpressionType.PROPERTY,
			"[a] consume effect to teleport randomly in [a] (radius|:diameter) of %number%");
	}

	private Expression<Number> number;
	private boolean isDiameter;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		number = (Expression<Number>) exprs[0];
		isDiameter = parseResult.hasTag("diameter");
		return true;
	}

	@Override
	protected ConsumeEffect @Nullable [] get(Event event) {
		Number number = this.number.getSingle(event);
		if (number == null)
			return null;

		float diameter = Math.abs(number.floatValue());
		if (!isDiameter)
			diameter *= 2;

		ConsumeEffect effect = ConsumeEffect.teleportRandomlyEffect(diameter);
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
		if (number instanceof Literal<Number>)
			return SimplifiedLiteral.fromExpression(this);
		return this;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("a consume effect to teleport randomly in a");
		if (isDiameter) {
			builder.append("diameter");
		} else {
			builder.append("radius");
		}
		builder.append("of", number);
		return builder.toString();
	}


}
