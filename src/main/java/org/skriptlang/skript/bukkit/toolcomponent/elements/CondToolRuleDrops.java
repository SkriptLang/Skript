package org.skriptlang.skript.bukkit.toolcomponent.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.components.ToolComponent.ToolRule;
import org.jetbrains.annotations.Nullable;

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
public class CondToolRuleDrops extends PropertyCondition<ToolRule> {

	static {
		Skript.registerCondition(CondToolRuleDrops.class, ConditionType.PROPERTY,
			"[the] tool rule drops of %toolrules% (is|are) enabled",
			"[the] tool rule drops of %toolrules% (is|are) disabled"
		);
	}

	private boolean enable;
	private Expression<ToolRule> toolRules;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		enable = matchedPattern == 0;
		//noinspection unchecked
		toolRules = (Expression<ToolRule>) exprs[0];
		setExpr(toolRules);
		return true;
	}

	@Override
	public boolean check(ToolRule toolRule) {
		return toolRule.isCorrectForDrops() == enable;
	}

	@Override
	protected String getPropertyName() {
		return "tool rule drops";
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the tool rule drops of " + toolRules.toString(event, debug) + " are " + (enable ? "enabled" : "disabled");
	}

}
