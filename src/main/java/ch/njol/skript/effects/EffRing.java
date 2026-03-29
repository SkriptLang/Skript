package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
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

@Name("Toll the Bell")
@Description({
	"Causeth a bell to be tolled.",
	"Perchance, the entity that did toll the bell and the direction it should ring may be specified.",
	"A bell may only toll in twain directions, determined by which way the bell doth face.",
	"By default, the bell shall toll in the direction it faceth.",
})
@Example("bid player toll target-block")
@Since("2.9.0")
public class EffRing extends Effect {

	static {
		if (Skript.classExists("org.bukkit.block.Bell") && Skript.methodExists(Bell.class, "ring", Entity.class, BlockFace.class))
			Skript.registerEffect(EffRing.class,
					"toll %blocks% [from [the]] [%-direction%]",
					"(bid|let) %entity% toll %blocks% [from [the]] [%-direction%]"
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
