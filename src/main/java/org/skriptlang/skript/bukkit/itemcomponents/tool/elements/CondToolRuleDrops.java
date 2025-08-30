package org.skriptlang.skript.bukkit.itemcomponents.tool.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolExperiment;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolRuleWrapper;

@Name("Tool Rule - Drops Enabled")
@Description("If the block types set in the tool rule should drop their respective items, "
	+ "when mined with the tool/item the tool rule is applied to.")
@Example("""
	set {_rules::*} to the tool rules of {_item}
	loop {_rules::*}:
		if the tool rule drops of loop-value is enabled:
			remove loop-value from the tool rules of {_item}
	""")
@RequiredPlugins("Minecraft 1.21.3+")
@Since("INSERT VERSION")

@SuppressWarnings("UnstableApiUsage")
public class CondToolRuleDrops extends PropertyCondition<ToolRuleWrapper> implements ToolExperiment {

	static {
		Skript.registerCondition(CondToolRuleDrops.class, ConditionType.PROPERTY,
			"[the] tool rule drops (of|for) %toolrules% (is|are) enabled",
			"[the] tool rule drops (of|for) %toolrules% (is|are) disabled"
		);
	}

	@Override
	public boolean check(ToolRuleWrapper wrapper) {
		Boolean correct = wrapper.getRule().correctForDrops().toBoolean();
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
		builder.append("the tool rule drops of", getExpr(), "are");
		if (!isNegated()) {
			builder.append("enabled");
		} else {
			builder.append("disabled");
		}
		return builder.toString();
	}

}
