package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.block.Bell;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Ring Bell")
@Description({
	"Causes a bell to ring.",
	"Optionally, the entity that rang the bell and the direction the bell should ring can be specified.",
	"A bell can only ring in two directions, and the direction is determined by which way the bell is facing.",
	"By default, the bell will ring in the direction it is facing.",
})
@Examples("make player ring target-block")
@RequiredPlugins("Spigot 1.19.4+")
@Since("2.9.0")
public class EffRing extends Effect implements SyntaxRuntimeErrorProducer {

	static {
		if (Skript.classExists("org.bukkit.block.Bell") && Skript.methodExists(Bell.class, "ring", Entity.class, BlockFace.class))
			Skript.registerEffect(EffRing.class,
				"ring %blocks% [from [the]] [%-direction%]",
				"(make|let) %entity% ring %blocks% [from [the]] [%-direction%]"
			);
	}

	private Node node;
	private @Nullable Expression<Entity> entity;
	private Expression<Block> blocks;
	private @Nullable Expression<Direction> direction;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		node = getParser().getNode();
		entity = matchedPattern == 0 ? null : (Expression<Entity>) exprs[0];
		blocks = (Expression<Block>) exprs[matchedPattern];
		direction = (Expression<Direction>) exprs[matchedPattern + 1];
		return true;
	}

	private @Nullable BlockFace getBlockFace(Event event) {
		if (this.direction == null)
			return null;

		Direction direction = this.direction.getSingle(event);
		if (direction == null) {
			warning("The provided direction was not set, so defaulted to none.", this.direction.toString());
			return null;
		}

		return Direction.getFacing(direction.getDirection(), true);
	}

	@Override
	protected void execute(Event event) {
		BlockFace blockFace = getBlockFace(event);
		Entity actualEntity = null;
		if (entity != null) {
			actualEntity = entity.getSingle(event);
			if (actualEntity == null)
				warning("The provided entity was not set, so defaulted to none.", entity.toString());
		}

		for (Block block : blocks.getArray(event)) {
			BlockState state = block.getState(false);
			if (state instanceof Bell bell)
				bell.ring(actualEntity, blockFace);
		}
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (entity != null)
			builder.append("make", entity);
		builder.append("ring", blocks);
		if (direction != null)
			builder.append("from", direction);
		return builder.toString();
	}

}
