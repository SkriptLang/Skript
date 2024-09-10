package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.Event;
import org.bukkit.spawner.Spawner;
import org.jetbrains.annotations.Nullable;

@Name("Spawner Reset Timer")
@Description("Resets the spawn delay timer for a spawner. Requires a Paper server.")
@Examples({
	"on right click on spawner:",
		"\treset spawner timer",
	"",
	"reset spawner spawn delay timer of target block"
})
@Since("INSERT VERSION")
@RequiredPlugins("Paper")
public class EffSpawnerResetTimer extends Effect {

	private static final boolean HAS_RESET_TIMER = Skript.methodExists(Spawner.class, "resetTimer");

	static {
		if (HAS_RESET_TIMER)
			Skript.registerEffect(EffSpawnerResetTimer.class, "reset spawner [spawn delay] timer [of %-blocks%]");
	}

	private Expression<Block> blocks;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.blocks = (Expression<Block>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Block block : blocks.getArray(event)) {
			if (block.getState() instanceof CreatureSpawner spawner) {
				spawner.resetTimer();
				spawner.update();
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "reset spawner spawn delay timer of " + blocks.toString(event, debug);
	}

}
