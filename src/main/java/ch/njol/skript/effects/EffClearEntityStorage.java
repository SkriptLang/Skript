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

@Name("Clear Entity Storage")
@Description("Clear the stored entities of an entity block storage (i.e. beehive).")
@Examples("clear the stored entities of {_beehive}")
@RequiredPlugins("Paper")
@Since("INSERT VERSION")
public class EffClearEntityStorage extends Effect {

	static {
		if (Skript.methodExists(EntityBlockStorage.class, "clearEntities"))
			Skript.registerEffect(EffClearEntityStorage.class,
				"(clear|empty) the (stored entities|entity storage) of %blocks%");
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
