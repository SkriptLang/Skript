package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
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

@Name("Transmute Block")
@Description({
	"Doth transmute blocks by setting them to a chosen substance.",
	"Employing 'without physics' shall not dispatch tidings to the surrounding blocks of those being set.",
	"For example: Transmuting a block beside a sand block suspended in the air 'without physics' shall not cause the sand block to fall."
})
@Example("transmute {_blocks::*} as gravel")
@Example("transmute {_blocks::*} to be sand without physics updates")
@Example("transmute {_blocks::*} as stone without neighbouring updates")
@Since("2.10")
// Originally sourced from SkBee by ShaneBee (https://github.com/ShaneBeee/SkBee/blob/master/src/main/java/com/shanebeestudios/skbee/elements/other/effects/EffBlockstateUpdate.java)
public class EffBlockUpdate extends Effect {

	static {
		Skript.registerEffect(EffBlockUpdate.class,
			"transmute %blocks% (as|to be) %blockdata% [physics:without [neighbo[u]r[ing]|adjacent] [physics] update[s]]");
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
