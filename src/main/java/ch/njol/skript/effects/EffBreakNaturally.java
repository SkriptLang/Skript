package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

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
public class EffBreakNaturally extends Effect implements SyntaxRuntimeErrorProducer {
	
	static {
		Skript.registerEffect(EffBreakNaturally.class, "break %blocks% [naturally] [using %-itemtype%]");
	}

	private Node node;
	private Expression<Block> blocks;
	private @Nullable Expression<ItemType> tool;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		node = getParser().getNode();
		blocks = (Expression<Block>) exprs[0];
		tool = (Expression<ItemType>) exprs[1];
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		ItemType itemType = null;
		if (this.tool != null) {
			ItemType tool = this.tool.getSingle(event);
			if (tool == null) {
				warning("The provided tool was not set, so defaulted to nothing.");
			} else {
				itemType = tool;
			}
		}

		for (Block block : this.blocks.getArray(event)) {
			if (itemType != null) {
				ItemStack itemStack = itemType.getRandom();
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
	public Node getNode() {
		return node;
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("break", blocks, "naturally");
		if (tool != null)
			builder.append("using", tool);
		return builder.toString();
	}

}
