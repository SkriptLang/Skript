package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Is Preferred Tool")
@Description(
		"Checks whether an item is the preferred tool for a block. A preferred tool is one that will drop the block's item " +
		"when used. For example, a wooden pickaxe is a preferred tool for grass and stone blocks, but not for iron ore."
)
@Examples({
	"on left click:",
		"\tevent-block is set",
		"\tif player's tool is the preferred tool for event-block:",
			"\t\tbreak event-block naturally using player's tool",
		"\telse:",
			"\t\tcancel event"
})
@Since("2.7")
@RequiredPlugins("1.16.5+, Paper 1.19.2+ (blockdata)")
public class CondIsPreferredTool extends Condition {

	static {
		String types = "blocks";
		if (Skript.methodExists(BlockData.class, "isPreferredTool", ItemStack.class))
			types += "/blockdatas";

		Skript.registerCondition(CondIsPreferredTool.class,
				"%itemtypes% (is|are) %" + types + "%'s preferred tool[s]",
				"%itemtypes% (is|are) [the|a] preferred tool[s] (for|of) %" + types + "%",
				"%itemtypes% (is|are)(n't| not) %" + types + "%'s preferred tool[s]",
				"%itemtypes% (is|are)(n't| not) [the|a] preferred tool[s] (for|of) %" + types + "%"
		);
	}

	private Expression<ItemType> items;
	private Expression<?> blocks;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(matchedPattern >= 2);
		items = (Expression<ItemType>) exprs[0];
		blocks = exprs[1];
		return true;
	}

	@Override
	public boolean check(Event event) {
		return blocks.check(event, block ->
			items.check(event, item -> {
				ItemStack stack = item.getRandom();
				if (stack != null) {
					if (block instanceof Block)
						return ((Block) block).isPreferredTool(stack);
					if (block instanceof BlockData)
						return ((BlockData) block).isPreferredTool(stack);
				}
				return false;
			}), isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return items.toString(event, debug) + " is the preferred tool for " + blocks.toString(event, debug);
	}
}
