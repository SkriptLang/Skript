package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
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
		register(ExprSpawnDelay.class, Integer.class, "[:min[imum]|:max[imum]|] spawn delay", "blocks");
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
			case SET, ADD, REMOVE -> new Class[]{Number.class};
			default -> null;
		};
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
		Block block = getExpr().getSingle(event);
		if (block == null || !(block.getState() instanceof CreatureSpawner)) return;

		CreatureSpawner spawner = (CreatureSpawner) block.getState();
		Integer count = delta != null && delta[0] instanceof Integer ? (Integer) delta[0] : null;

		switch (mode) {
			case SET:
				setSpawnDelay(spawner, count);
				block.getState().update();
				break;
			case ADD:
				setSpawnDelay(spawner,getSpawnDelay(spawner) + count);
				block.getState().update();
				break;
			case REMOVE:
				setSpawnDelay(spawner, getSpawnDelay(spawner) - count);
				block.getState().update();
				break;
			case RESET:
				if (isMax) {
					setSpawnDelay(spawner, 800);
				} else if (isMin) {
					setSpawnDelay(spawner, 200);
				} else {
					spawner.setDelay(-1);
				}
				block.getState().update();
				break;
			default:
				break;
		}
	}

	private void setSpawnDelay(CreatureSpawner spawner, int delay) {
		if (isMax) {
			spawner.setMaxSpawnDelay(delay);
		} else if (isMin) {
			spawner.setMinSpawnDelay(delay);
		} else {
			spawner.setDelay(delay);
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
