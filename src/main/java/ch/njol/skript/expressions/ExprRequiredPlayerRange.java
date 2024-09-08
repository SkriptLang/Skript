package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
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
				spawner.setRequiredPlayerRange(count);
				block.getState().update();
				break;
			case ADD:
				spawner.setRequiredPlayerRange(spawner.getSpawnCount() + count);
				block.getState().update();
				break;
			case REMOVE:
				spawner.setRequiredPlayerRange(spawner.getSpawnCount() - count);
				block.getState().update();
				break;
			case RESET:
				spawner.setRequiredPlayerRange(16); // Default is 16 according to javadoc.
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
		return "required player range";
	}
}
