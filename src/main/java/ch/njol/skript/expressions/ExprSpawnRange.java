package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.jetbrains.annotations.Nullable;


public class ExprSpawnRange extends SimplePropertyExpression<Block, Integer> {

	static {
		register(ExprSpawnRange.class, Integer.class, "spawn range", "blocks");
	}

	@Override
	public @Nullable Integer convert(Block block) {
		if (block.getState() instanceof CreatureSpawner spawner)
			return spawner.getSpawnCount();

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
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "spawn range";
	}

}
