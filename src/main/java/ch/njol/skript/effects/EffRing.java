package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.block.Bell;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Ring Bell")
@Description({
	"Causes a bell to ring.",
	"Optionally, the entity that rang the bell and the direction the bell should ring can be specified.",
	"A bell can only ring in two directions, and the direction is determined by which way the bell is facing.",
	"By default, the bell will ring in the direction it is facing.",
})
@Examples({"make player ring target-block"})
@RequiredPlugins("Spigot 1.19.4+")
@Since("2.9.0")
public class EffRing extends Effect {

	static {
		if (Skript.classExists("org.bukkit.block.Bell") && Skript.methodExists(Bell.class, "ring", Entity.class, BlockFace.class))
			Skript.registerEffect(EffRing.class,
					"ring %blocks% [from [the]] [%-direction%]",
					"(make|let) %entity% ring %blocks% [from [the]] [%-direction%]"
			);
	}

	@Nullable
	private Expression<Entity> entity;

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Block> blocks;

	@Nullable
	private Expression<Direction> direction;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entity = matchedPattern == 0 ? null : (Expression<Entity>) exprs[0];
		blocks = (Expression<Block>) exprs[matchedPattern];
		direction = (Expression<Direction>) exprs[matchedPattern + 1];
		return true;
	}

	@Nullable
	private BlockFace getBlockFace(Event event) {
		if (this.direction == null)
			return null;

		Direction direction = this.direction.getSingle(event);
		if (direction == null)
			return null;

		return Direction.getFacing(direction.getDirection(), true);
	}

	@Override
	protected void execute(Event event) {
		BlockFace blockFace = getBlockFace(event);
		Entity actualEntity = entity == null ? null : entity.getSingle(event);

		for (Block block : blocks.getArray(event)) {
			BlockState state = block.getState(false);
			if (state instanceof Bell) {
				Bell bell = (Bell) state;
				bell.ring(actualEntity, blockFace);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (entity != null ? "make " + entity.toString(event, debug) + " " : "") +
				"ring " + blocks.toString(event, debug) + " from " + (direction != null ? direction.toString(event, debug) : "");
	}

}
