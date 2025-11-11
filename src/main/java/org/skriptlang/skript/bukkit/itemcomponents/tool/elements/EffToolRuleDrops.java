package org.skriptlang.skript.bukkit.itemcomponents.tool.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolExperimentalSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolRuleWrapper;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Tool Rule - Drops")
@Description("""
	Whether the block types set in the tool rule should drop their respective items, \
	when mined with the item the tool rule is applied to.
	A tool rule consists of:
		- Block types that the rule should be applied to
		- Mining speed for the blocks
		- Whether the blocks should drop their respective items
	NOTE: Tool component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	set {_rule} to a custom tool rule with block types oak log, stone and obsidian
	set the tool rule speed of {_rule} to 10
	enable the tool rule drops of {_rule}
	add {_rule} to the tool rules of {_item}
	""")
@RequiredPlugins("Minecraft 1.21.3+")
@Since("INSERT VERSION")
public class EffToolRuleDrops extends Effect implements ToolExperimentalSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffToolRuleDrops.class)
				.addPatterns(
					"enable [the] tool rule drops (of|for) %toolrules%",
					"disable [the] tool rule drops (of|for) %toolrules%"
				)
				.supplier(EffToolRuleDrops::new)
				.build()
		);
	}

	private Expression<ToolRuleWrapper> toolRules;
	private boolean enable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		toolRules = (Expression<ToolRuleWrapper>) exprs[0];
		enable = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		toolRules.stream(event).forEach(rule -> rule.modify(builder -> builder.correctForDrops(enable)));
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
