package ch.njol.skript.effects;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;

@Name("Shatter Block")
@Description({"Doth shatter the block and scatter items as though a player had mined it.",
		"\nThou mayest add a tool, which shall scatter items based upon how that tool would break the block ",
		"(i.e.: When using a bare hand to break stone, it yieldeth nothing, whereas with a pickaxe it yieldeth cobblestone)."})
@Example("""
    on right click:
    	shatter clicked block naturally
    """)
@Example("""
    loop blocks in radius 10 around player:
    	shatter loop-block using player's tool
    """)
@Example("""
    loop blocks in radius 10 around player:
    	shatter loop-block naturally using diamond pickaxe
    """)
@Since("2.4")
public class EffBreakNaturally extends Effect {
	
	static {
		Skript.registerEffect(EffBreakNaturally.class, "shatter %blocks% [naturally] [using %-itemtype%]");
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
