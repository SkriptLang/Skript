package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Name("Update Block")
@Description({
	"Updates the blocks by setting them to a selected block",
	"Using 'without physics' will not send updates to the surrounding blocks of the blocks being set.",
	"Example: Updating a block next to a sand block in the air 'without physics' will not cause the sand block to fall."
})
@Examples({
	"update {_blocks::*} as gravel",
	"update {_blocks::*} to be sand without physics updates",
	"update {_blocks::*} as stone without neighbouring updates"
})
@Since("INSERT VERSION")
// Originally sourced from SkBee by ShaneBee (https://github.com/ShaneBeee/SkBee/blob/master/src/main/java/com/shanebeestudios/skbee/elements/other/effects/EffBlockstateUpdate.java)
public class EffBlockUpdate extends Effect {

	static {
		Skript.registerEffect(EffBlockUpdate.class,
			"update %blocks% (as|to be) %blockdata% [physics:without [neighbo[u]r[ing]|adjacent] [physic[s]] update[s]]");
	}

	private boolean physics;
	private Expression<Block> blocks;
	private Expression<BlockData> blockData;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.physics = !parseResult.hasTag("physics");
		this.blocks = (Expression<Block>) exprs[0];
		this.blockData = (Expression<BlockData>) exprs[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		BlockData data = this.blockData.getSingle(event);
		if (data == null)
			return;
		for (Block block : this.blocks.getArray(event)) {
			block.setBlockData(data, this.physics);
		}
	}

	@Override
	public @NotNull String toString(@Nullable Event event, boolean debug) {
		return "update " + this.blocks.toString(event, debug) + " as "
			+ this.blockData.toString(event, debug) + (this.physics ? "without neighbour updates" : "");
	}

}
