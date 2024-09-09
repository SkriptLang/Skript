package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Spawner Spawn Delay")
@Description({
	"Gets or sets the spawn delay of a Spawner block. Can also be used to get the minimum or maximum value.",
	"This value is used when the spawner resets its delay. Measured in ticks.",
	"When setting minimum/maximum values, the spawner will choose a random number between the minimum and maximum delay to use as it's delay."
})
@Examples({
	"set {_count} to minimum spawn delay of target block",
	"set spawn delay of {_spawner} to 800"
})
@Since("INSERT VERSION")
public class ExprSpawnDelay extends SimplePropertyExpression<Block, Integer> {

	static {
		register(ExprSpawnDelay.class, Integer.class, "[:max[imum]|:min[imum]] spawn delay", "blocks");
	}

	boolean isMax;
	boolean isMin;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isMax = parseResult.hasTag("max");
		isMin = parseResult.hasTag("min");
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Integer convert(Block block) {
		if (block.getState() instanceof CreatureSpawner spawner) {
			return getSpawnDelay(spawner);
		}

		return null;
	}

	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
		int count = delta != null ? ((Number) delta[0]).intValue() : 0;
		Skript.info(String.valueOf(count));

		for (Block block : getExpr().getArray(event)) {
			if (block.getState() instanceof CreatureSpawner spawner) {
				int newCount = 0;
				switch (mode) {
					case SET:
						newCount = count;
						break;
					case ADD:
						newCount = getSpawnDelay(spawner) + count;
						break;
					case REMOVE:
						newCount = getSpawnDelay(spawner) - count;
						break;
					case RESET:
						if (isMax) {
							newCount = 800; // Default max spawn delay
						} else if (isMin) {
							newCount = 200; // Default min spawn delay
						} else {
							spawner.setDelay(-1); // reset
							block.getState().update();
							continue;
						}
						break;
					default:
						return;
				}
				Skript.info(String.valueOf(newCount));
				setSpawnDelay(spawner, Math.max(newCount, 0));
				Skript.info(String.valueOf(Math.max(newCount, 0)));
				block.getState().update();
			}
		}
	}

	private void setSpawnDelay(CreatureSpawner spawner, int delay) {
		if (isMax) {
			spawner.setMaxSpawnDelay(delay);
		} else if (isMin) {
			spawner.setMinSpawnDelay(delay);
		} else {
			spawner.setDelay(delay);
			Skript.info(String.valueOf(delay));
		}
	}

	private int getSpawnDelay(CreatureSpawner spawner) {
		if (isMax) {
			return spawner.getMaxSpawnDelay();
		} else if (isMin) {
			return spawner.getMinSpawnDelay();
		} else {
			return spawner.getDelay();
		}
	}


	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		if (isMax) {
			return "max spawn delay";
		} else if (isMin) {
			return "min spawn delay";
		} else {
			return "spawn delay";
		}
	}

}
