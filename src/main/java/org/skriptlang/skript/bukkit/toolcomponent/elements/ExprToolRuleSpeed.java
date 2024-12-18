package org.skriptlang.skript.bukkit.toolcomponent.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.components.ToolComponent.ToolRule;
import org.jetbrains.annotations.Nullable;

@Name("Tool Rule - Speed")
@Description({
	"The speed of a tool rule determining the mining speed of the blocks of the tool rule for an item.",
	"NOTE: 1.0 is equivalent to the default mining speed of the mined block."
})
@Examples({
	"set {_rule} to a new tool rule with block types oak log, stone and obsidian",
	"set the tool rule speed of {_rule} to 10",
	"enable the tool rule drops of {_rule}",
	"add {_rule} to the tool rules of {_item}"
})
@RequiredPlugins("Minecraft 1.20.6+")
@Since("INSERT VERSION")
public class ExprToolRuleSpeed extends PropertyExpression<ToolRule, Float> {

	static {
		Skript.registerExpression(ExprToolRuleSpeed.class, Float.class, ExpressionType.PROPERTY,
			"[the] tool rule speed of %toolrules%");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<? extends ToolRule>) exprs[0]);
		return true;
	}

	@Override
	protected Float @Nullable [] get(Event event, ToolRule[] source) {
		return get(source, toolRule -> {
			return toolRule.getSpeed();
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Number.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Number providedNumber = (Number) delta[0];
		for (ToolRule rule : getExpr().getArray(event)) {
			rule.setSpeed(providedNumber.floatValue());
		}
	}

	@Override
	public boolean isSingle() {
		return getExpr().isSingle();
	}

	@Override
	public Class<Float> getReturnType() {
		return Float.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the tool rule speed of " + getExpr().toString(event, debug);
	}

}
