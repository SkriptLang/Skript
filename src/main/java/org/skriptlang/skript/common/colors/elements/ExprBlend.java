package org.skriptlang.skript.common.colors.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Color;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.colors.ColorUtils;

import java.util.ArrayList;
import java.util.List;

@Name("Blended Colours")
@Description({
	"Returns the result of blending colours together. Optionally takes an amount to blend the colours by, which is" +
		"a number from 0 to 100.",
	"In that range, a 50 would be an expected equal blend of each colour (the default behaviour)."
})
@Examples({
	"set {_purple} to red blended with blue",
	"set {_goldyPurple} to {_purple} blended with gold",
	"set {_morePurpleThanGold} to {_purple} blended with gold by an amount of 10",
	"set {_aBunch} to red blended with all colours where [input is not red]"
})
@Since("INSERT VERSION")
public class ExprBlend extends SimpleExpression<Color> {

	static {
		Skript.registerExpression(ExprBlend.class, Color.class, ExpressionType.COMBINED,
			"%colors% (blended|mixed) with %colors% [by [(a factor|an amount) of] %-number%]",
			"blend of %colors% (and|with) %colors% [by [(a factor|an amount) of] %-number%]",
			"%colors% and %colors% blen(ded|t) together [by [(a factor|an amount) of] %-number%]");
	}

	private Expression<Color> colours, blendWith;
	private Expression<Number> amount;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.colours = (Expression<Color>) exprs[0];
		this.blendWith = (Expression<Color>) exprs[1];
		this.amount = (Expression<Number>) exprs[2];
		return true;
	}

	@Override
	protected Color @Nullable [] get(Event event) {
		Color[] colours = this.colours.getArray(event);
		Color[] blendWiths = this.blendWith.getArray(event);
		Number amount = this.amount == null ? 50 : this.amount.getOptionalSingle(event).orElse(50);

		List<Color> blendedColours = new ArrayList<>();
		for (Color colour : colours) {
			Color blended = colour;
			for (Color blendWith : blendWiths) {
				blended = ColorUtils.blendColors(blended, blendWith, amount.doubleValue());
			}
			blendedColours.add(blended);
		}

		return blendedColours.toArray(new Color[0]);
	}

	@Override
	public boolean isSingle() {
		return this.colours.isSingle();
	}

	@Override
	public Class<? extends Color> getReturnType() {
		return Color.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
        return new SyntaxStringBuilder(event, debug)
			.append(this.colours)
			.append("blended with")
			.append(this.blendWith)
			.append("by a factor of")
			.append(this.amount)
			.toString();
	}

}
