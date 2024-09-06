package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Spawner Spawn Count")
@Description({
	"Gets or sets the spawn count of a Spawner block. This determines how many mobs attempt to spawn."
})
@Examples({
	"set {_count} to spawn count of spawner at location of player",
	"set spawn count of spawner at location of player to 10"
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
		return mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.ADD || mode == Changer.ChangeMode.REMOVE ? new Class[]{Integer.class} : null;
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
		Block block = getExpr().getSingle(e);
		if (block == null || !(block.getState() instanceof CreatureSpawner)) return;

		CreatureSpawner spawner = (CreatureSpawner) block.getState();
		Integer count = delta != null && delta[0] instanceof Integer ? (Integer) delta[0] : null;

		switch (mode) {
			case SET:
				spawner.setSpawnCount(count);
				block.getState().update();
				break;
			case ADD:
				spawner.setSpawnCount(spawner.getSpawnCount() + count);
				block.getState().update();
				break;
			case REMOVE:
				spawner.setSpawnCount(spawner.getSpawnCount() - count);
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
		return "spawn count";
	}

}
