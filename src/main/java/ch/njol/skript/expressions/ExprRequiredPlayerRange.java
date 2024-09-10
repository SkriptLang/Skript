package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Spawner Required Player Range")
@Description({
	"Gets or sets the required player range of a Spawner block.",
	"This determines the maximum distance (squared) a player can be in order for this spawner to be active."
})
@Examples({
	"set {_count} to required player range of target block",
	"set required player range of {_spawner} to 10"
})
public class ExprRequiredPlayerRange extends SimplePropertyExpression<Block, Integer> {

	static {
		register(ExprRequiredPlayerRange.class, Integer.class, "required player range", "blocks");
	}

	@Override
	public @Nullable Integer convert(Block block) {
		if (block.getState() instanceof CreatureSpawner spawner)
			return spawner.getRequiredPlayerRange();

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
	public void change(Event event, Object @Nullable [] delta, Changer.ChangeMode mode) {
		int count = delta != null ? ((Number) delta[0]).intValue() : 0;

		for (Block block : getExpr().getArray(event)) {
			if (block.getState() instanceof CreatureSpawner spawner) {
				int newCount = 0;
				switch (mode) {
					case REMOVE:
						newCount = spawner.getRequiredPlayerRange() - count;
						break;
					case ADD:
						newCount = spawner.getRequiredPlayerRange() + count;
						break;
					case SET:
						newCount = count;
						break;
					case RESET:
						newCount = 16; // Default value according to javadoc
						break;
					default:
						return;
				}
				spawner.setRequiredPlayerRange(Math.max(newCount, 0));
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
		return "required player range";
	}
}
