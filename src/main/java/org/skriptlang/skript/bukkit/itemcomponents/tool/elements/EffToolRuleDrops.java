package org.skriptlang.skript.bukkit.itemcomponents.tool.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.components.ToolComponent.ToolRule;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolExperiment;

@Name("Tool Rule - Drops")
@Description("If the tool with this tool rule should drop items of a block defined from the blocks of this tool rule.")
@Examples({
	"set {_rule} to a new tool rule with block types oak log, stone and obsidian",
	"set the tool rule speed of {_rule} to 10",
	"enable the tool rule drops of {_rule}",
	"add {_rule} to the tool rules of {_item}"
})
@RequiredPlugins("Minecraft 1.20.6+")
@Since("INSERT VERSION")

@SuppressWarnings("UnstableApiUsage")
public class EffToolRuleDrops extends Effect implements ToolExperiment {

	static {
		Skript.registerEffect(EffToolRuleDrops.class,
			"enable [the] tool rule drops (of|for) %toolrules%",
			"disable [the] tool rule drops (of|for) %toolrules%");
	}

	private Expression<? extends ToolRule> toolRules;
	private boolean enable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		toolRules = (Expression<? extends ToolRule>) exprs[0];
		enable = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		toolRules.stream(event).forEach(toolRule -> toolRule.setCorrectForDrops(enable));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (enable) {
			builder.append("enable");
		} else {
			builder.append("disable");
		}
		builder.append("the tool rule drops for", toolRules);
		return builder.toString();
	}

}
