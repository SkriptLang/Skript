package org.skriptlang.skript.bukkit.itemcomponents.tool.elements;

import ch.njol.skript.Skript;
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
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolWrapper;

@Name("Tool Component - Destroy Blocks In Creative")
@Description("""
	Whether an item should destroy blocks when used by a player in creative mode.
	NOTE: Tool component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	if {_item} can destroy blocks in creative:
		prevent {_item} from destroying blocks in creative
	""")
@RequiredPlugins("Minecraft 1.21.5+")
@Since("INSERT VERSION")
public class EffToolCompCreative extends Effect implements ToolExperimentalSyntax {

	static {
		if (ToolWrapper.HAS_CAN_DESTROY_BLOCKS_IN_CREATIVE)
			Skript.registerEffect(EffToolCompCreative.class,
				"allow %toolcomponents% to destroy blocks in creative",
				"prevent %toolcomponents% from destroying blocks in creative");
	}

	private Expression<ToolWrapper> tools;
	private boolean enable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		tools = (Expression<ToolWrapper>) exprs[0];
		enable = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		tools.stream(event).forEach(wrapper ->
			wrapper.editBuilder(builder -> builder.canDestroyBlocksInCreative(enable)));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (enable) {
			return new SyntaxStringBuilder(event, debug)
				.append("allow", tools, "to destroy blocks in creative")
				.toString();
		}
		return new SyntaxStringBuilder(event, debug)
			.append("prevent", tools, "from destroying blocks in creative")
			.toString();
	}

}
