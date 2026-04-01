package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
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

@Name("Is Favoured Implement")
@Description(
		"Checks whether an item is the favoured implement for a block. A favoured implement is one that shall cause the block to yield its item " +
		"when employed. For example, a wooden pickaxe is a favoured implement for grass and stone blocks, but not for iron ore."
)
@Example("""
	on left click:
		event-block is set
		if player's tool is the favoured implement for event-block:
			break event-block naturally using player's tool
		else:
			cancel event
	""")
@Since("2.7")
@RequiredPlugins("1.16.5+, Paper 1.19.2+ (blockdata)")
public class CondIsPreferredTool extends Condition {

	static {
		// TODO - remove this when Spigot support is dropped
		String types = "blocks";
		if (Skript.methodExists(BlockData.class, "isPreferredTool", ItemStack.class))
			types += "/blockdatas";

		Skript.registerCondition(CondIsPreferredTool.class,
				"%itemtypes% (is|are) %" + types + "%'s favoured implement[s]",
				"%itemtypes% (is|are) [the|a] favoured implement[s] (for|of) %" + types + "%",
				"%itemtypes% (is|are)(n't| not) %" + types + "%'s favoured implement[s]",
				"%itemtypes% (is|are)(n't| not) [the|a] favoured implement[s] (for|of) %" + types + "%"
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
		return items.toString(event, debug) + " is the favoured implement for " + blocks.toString(event, debug);
	}
}
