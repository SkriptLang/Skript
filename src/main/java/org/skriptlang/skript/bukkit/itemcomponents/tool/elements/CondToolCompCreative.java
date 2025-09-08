package org.skriptlang.skript.bukkit.itemcomponents.tool.elements;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolExperimentalSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolWrapper;

@Name("Tool Component - Can Destroy Blocks In Creative")
@Description("""
	Whether an item can destroy blocks when used by a player in creative mode.
	NOTE: Tool component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	if {_item} can destroy blocks in creative:
		prevent {_item} from destroying blocks in creative
	""")
@RequiredPlugins("Minecraft 1.21.5+")
@Since("INSERT VERSION")
public class CondToolCompCreative extends PropertyCondition<ToolWrapper> implements ToolExperimentalSyntax {

	static {
		if (ToolWrapper.HAS_CAN_DESTROY_BLOCKS_IN_CREATIVE)
			register(CondToolCompCreative.class, PropertyType.CAN, "destroy blocks in creative", "toolcomponents");
	}

	@Override
	public boolean check(ToolWrapper wrapper) {
		return wrapper.getComponent().canDestroyBlocksInCreative();
	}

	@Override
	protected String getPropertyName() {
		return "destroy blocks in creative";
	}

}
