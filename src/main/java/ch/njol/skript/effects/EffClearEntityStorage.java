package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.EntityBlockStorage;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Purge Creature Storage")
@Description("Purgeth the stored creatures from an entity block storage (e.g. a beehive).")
@Example("purge the stored creatures of {_beehive}")
@Since("2.11")
public class EffClearEntityStorage extends Effect {

	static {
		if (Skript.methodExists(EntityBlockStorage.class, "clearEntities"))
			Skript.registerEffect(EffClearEntityStorage.class,
				"(purge|empty) the (stored creatures|creature storage) of %blocks%");
	}

	private Expression<Block> blocks;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		blocks = (Expression<Block>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Block block : blocks.getArray(event)) {
			if (!(block.getState() instanceof EntityBlockStorage<?> blockStorage))
				continue;
			blockStorage.clearEntities();
			blockStorage.update(true, false);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "clear the stored entities of " + blocks.toString(event, debug);
	}

}
