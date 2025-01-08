package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Break Block")
@Description({
	"Breaks the block and spawns items as if a player had mined it",
	"You can add a tool, which will spawn items based on how that tool would break the block " +
	"(i.e. using a hand to break stone drops nothing, whereas using a pickaxe drops cobblestone)"})
@Examples({
	"on right click:",
		"\tbreak clicked block naturally",
	"loop blocks in radius 10 around player:",
		"\tbreak loop-block using player's tool",
	"loop blocks in radius 10 around player:",
		"\tbreak loop-block naturally using diamond pickaxe"
})
@Since("2.4")
public class EffBreakNaturally extends Effect {
	
	static {
		Skript.registerEffect(EffBreakNaturally.class, "break %blocks% [naturally] [using %-itemtype%]");
	}

	private Expression<Block> blocks;
	private @Nullable Expression<ItemType> tool;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		blocks = (Expression<Block>) exprs[0];
		tool = (Expression<ItemType>) exprs[1];
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		ItemType tool = this.tool != null ? this.tool.getSingle(event) : null;
		for (Block block : this.blocks.getArray(event)) {
			if (tool != null) {
				ItemStack itemStack = tool.getRandom();
				if (itemStack != null) {
					block.breakNaturally(itemStack);
				} else {
					block.breakNaturally();
				}
			} else {
				block.breakNaturally();
			}
		}
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "break " + blocks.toString(event, debug) + " naturally" + (tool != null ? " using " + tool.toString(event, debug) : "");
	}
}
