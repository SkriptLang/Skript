package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Spawner Spawn Count")
@Description({
	"Gets or sets the spawn count of a Spawner block.",
	"This determines how many mobs attempt to spawn."
})
@Examples({
	"set {_count} to spawn count of target block",
	"set spawn count of {_spawner} to 10"
})
@Since("INSERT VERSION")
public class ExprSpawnCount extends SimplePropertyExpression<Block, Integer> {

	static {
		register(ExprSpawnCount.class, Integer.class, "spawn count", "blocks");
	}

	@Override
	public @Nullable Integer convert(Block block) {
		if (block.getState() instanceof CreatureSpawner spawner)
			return spawner.getSpawnCount();

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

		for (Block block : getExpr().getArray(event)) {
			if (block.getState() instanceof CreatureSpawner spawner) {
				int newCount = 0;
				switch (mode) {
					case REMOVE:
						newCount = spawner.getSpawnCount() - count;
						break;
					case ADD:
						newCount = spawner.getSpawnCount() + count;
						break;
					case SET:
						newCount = count;
						break;
					case RESET:
						newCount = 4; // Default value according to javadoc
						break;
					default:
						return;
				}
				spawner.setSpawnCount(Math.max(newCount, 0));
				spawner.update();
			}
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "spawn count";
	}

}
