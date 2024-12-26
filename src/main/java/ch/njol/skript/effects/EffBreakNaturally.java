package ch.njol.skript.effects;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;

@Name("Break Block")
@Description({"Breaks the block and spawns items as if a player had mined it",
		"\nYou can add a tool, which will spawn items based on how that tool would break the block ",
		"(ie: When using a hand to break stone, it drops nothing, whereas with a pickaxe it drops cobblestone)"})
@Examples({"on right click:", "\tbreak clicked block naturally",
		"loop blocks in radius 10 around player:", "\tbreak loop-block using player's tool",
		"loop blocks in radius 10 around player:", "\tbreak loop-block naturally using diamond pickaxe"})
@Since("2.4")
public class EffBreakNaturally extends Effect {
	
	static {
		Skript.registerEffect(EffBreakNaturally.class, "break %blocks% [naturally] [using %-itemtype%]");
	}
	
	@SuppressWarnings("null")
	private Expression<Block> blocks;
	@Nullable
	private Expression<ItemType> tool;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parser) {
		blocks = (Expression<Block>) exprs[0];
		tool = (Expression<ItemType>) exprs[1];
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
		ItemType tool = this.tool != null ? this.tool.getSingle(e) : null;
		for (Block block : this.blocks.getArray(e)) {
			if (tool != null) {
				ItemStack is = tool.getRandom();
				if (is != null)
					block.breakNaturally(is);
				else
					block.breakNaturally();
			} else {
				block.breakNaturally();
			}
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "break " + blocks.toString(e, debug) + " naturally" + (tool != null ? " using " + tool.toString(e, debug) : "");
	}
}
