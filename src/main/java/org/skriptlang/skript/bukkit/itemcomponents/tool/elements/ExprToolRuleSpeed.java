package org.skriptlang.skript.bukkit.itemcomponents.tool.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.components.ToolComponent.ToolRule;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolExperiment;

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

@SuppressWarnings("UnstableApiUsage")
public class ExprToolRuleSpeed extends SimplePropertyExpression<ToolRule, Float> implements ToolExperiment {

	static {
		register(ExprToolRuleSpeed.class, Float.class, "tool rule speed", "toolrules");
	}

	@Override
	public @Nullable Float convert(ToolRule toolRule) {
		return toolRule.getSpeed();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Number.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		float speed = ((Number) delta[0]).floatValue();
		getExpr().stream(event).forEach(toolRule -> toolRule.setSpeed(speed));
	}

	@Override
	public Class<Float> getReturnType() {
		return Float.class;
	}

	@Override
	protected String getPropertyName() {
		return "tool rule speed";
	}

}
