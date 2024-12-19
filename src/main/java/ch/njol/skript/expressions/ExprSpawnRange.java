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

@Name("Spawner Spawn Range")
@Description({
	"Gets or sets the spawn range of a Spawner block.",
	"This determines the radius around which the spawner will attempt to spawn mobs in."
})
@Examples({
	"set {_count} to spawn range of target block",
	"set spawn range of {_spawner} to 10"
})
@Since("INSERT VERSION")
public class ExprSpawnRange extends SimplePropertyExpression<Block, Integer> {

	static {
		register(ExprSpawnRange.class, Integer.class, "spawn range", "blocks");
	}

	@Override
	public @Nullable Integer convert(Block block) {
		if (block.getState() instanceof CreatureSpawner spawner)
			return spawner.getSpawnRange();

		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
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
						newCount = spawner.getSpawnRange() - count;
						break;
					case ADD:
						newCount = spawner.getSpawnRange() + count;
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
				spawner.setSpawnRange(Math.max(newCount, 0));
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
		return "spawn range";
	}

}
