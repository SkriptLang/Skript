package org.skriptlang.skript.bukkit.itemcomponents.tool.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.components.ToolComponent.ToolRule;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolExperiment;

@Name("Tool Rule - Drops Enabled")
@Description("If the drops of a tool rule are enabled.")
@Examples({
	"set {_rules::*} to the tool rules of {_item}",
	"loop {_rules::*}:",
		"\tif the tool rule drops of loop-value is enabled:",
			"\tremove loop-value from the tool rules of {_item}"
})
@RequiredPlugins("Minecraft 1.20.6")
@Since("INSERT VERSION")

@SuppressWarnings("UnstableApiUsage")
public class CondToolRuleDrops extends PropertyCondition<ToolRule> implements ToolExperiment {

	static {
		Skript.registerCondition(CondToolRuleDrops.class, ConditionType.PROPERTY,
			"[the] tool rule drops (of|for) %toolrules% (is|are) enabled",
			"[the] tool rule drops (of|for) %toolrules% (is|are) disabled"
		);
	}

	private Expression<ToolRule> toolRules;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		toolRules = (Expression<ToolRule>) exprs[0];
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(ToolRule toolRule) {
		Boolean correct = toolRule.isCorrectForDrops();
		if (correct != null)
			return correct;
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "tool rule drops";
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("the tool rule drops of", toolRules, "are");
		if (!isNegated()) {
			builder.append("enabled");
		} else {
			builder.append("disabled");
		}
		return builder.toString();
	}

}
