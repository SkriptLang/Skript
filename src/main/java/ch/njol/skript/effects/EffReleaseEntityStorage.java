package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.EntityBlockStorage;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Name("Release From Entity Storage")
@Description({
	"Releases the stored entities in an entity block storage (i.e. beehive).",
	"When using beehives, providing a timespan will prevent the released bees from re-entering the beehive for that amount of time.",
	"Due to unstable behaviour on older versions, this effect requires Minecraft version 1.21+."
})
@Examples({
	"release the stored entities of {_beehive}",
	"release the entity storage of {_hive} for 5 seconds"
})
@RequiredPlugins("Minecraft 1.21")
@Since("INSERT VERSION")
public class EffReleaseEntityStorage extends Effect {

	/*
		Minecraft versions 1.19.4 -> 1.20.6 have unstable behavior.
		Either entities are not released or are released and not clearing the stored entities.
		Adding entities into EntityBlockStorage's are also unstable.
		Entities are either not added, or added but still exist.
	 */

	static {
		if (Skript.isRunningMinecraft(1, 21, 0)) {
			Skript.registerEffect(EffReleaseEntityStorage.class,
				"(release|evict) [the] (stored entities|entity storage) of %blocks% [for %-timespan%]");
		}
	}

	private Expression<Block> blocks;
	private @Nullable Expression<Timespan> timespan;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		blocks = (Expression<Block>) exprs[0];
		if (exprs[1] != null)
			//noinspection unchecked
			timespan = (Expression<Timespan>) exprs[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		Integer ticks = null;
		if (timespan != null) {
			Timespan time = timespan.getSingle(event);
			if (time != null)
				ticks = (int) time.getAs(TimePeriod.TICK);
		}
		for (Block block : blocks.getArray(event)) {
			if (!(block.getState() instanceof EntityBlockStorage<?> blockStorage))
				continue;
			List<? extends Entity> released = blockStorage.releaseEntities();
            if (ticks != null) {
				for (Entity entity : released) {
					if (entity instanceof Bee bee) {
						bee.setCannotEnterHiveTicks(ticks);
					}
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("release the stored entities of", blocks);
		if (timespan != null)
			builder.append("for", timespan);
		return builder.toString();
	}

}
