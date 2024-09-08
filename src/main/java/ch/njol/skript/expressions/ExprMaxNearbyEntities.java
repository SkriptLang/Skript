package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.Event;
import org.bukkit.spawner.Spawner;
import org.jetbrains.annotations.Nullable;

@Name("Spawner Max Nearby Entities")
@Description({
	"Gets or sets the maximum nearby entities of a Spawner block.",
	"This determines the maximum amount of similar entities that are allowed to be within spawning range of the spawner.",
	"If there are more entities then the maximum number, the spawner will not spawn mobs."
})
@Examples({
	"set {_count} to maximum nearby entities of target block",
	"set max nearby entities of {_spawner} to 10"
})
public class ExprMaxNearbyEntities extends SimplePropertyExpression<Block, Integer> {

	static {
		register(ExprMaxNearbyEntities.class, Integer.class, "max[imum] nearby entities", "blocks");
	}

	@Override
	public @Nullable Integer convert(Block block) {
		if (block.getState() instanceof Spawner spawner)
			return spawner.getMaxNearbyEntities();

		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE -> new Class[]{Number.class};
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, Changer.ChangeMode mode) {
		Block block = getExpr().getSingle(event);
		if (block == null || !(block.getState() instanceof CreatureSpawner)) return;

		CreatureSpawner spawner = (CreatureSpawner) block.getState();
		Integer count = delta != null && delta[0] instanceof Integer ? (Integer) delta[0] : null;

		switch (mode) {
			case SET:
				spawner.setMaxNearbyEntities(count);
				block.getState().update();
				break;
			case ADD:
				spawner.setMaxNearbyEntities(spawner.getSpawnCount() + count);
				block.getState().update();
				break;
			case REMOVE:
				spawner.setMaxNearbyEntities(spawner.getSpawnCount() - count);
				block.getState().update();
				break;
			case RESET:
				spawner.setMaxNearbyEntities(16); // Default is 16 according to javadoc.
				block.getState().update();
				break;
			default:
				break;
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "maximum nearby entities";
	}
}
