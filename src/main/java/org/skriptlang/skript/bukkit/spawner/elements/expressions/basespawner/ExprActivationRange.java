package org.skriptlang.skript.bukkit.spawner.elements.expressions.basespawner;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.TrialSpawner;
import org.bukkit.event.Event;
import org.bukkit.spawner.BaseSpawner;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerUtils;

@Name("Base Spawner - Activation Range")
@Description({
	"Gets the activation range of the base spawner. By default, this is 16.",
	"The activation range is the distance "
		+ "from the spawner that players must be within for the spawner to be active.",
	"Setting this value to less than or equal to 0, makes the spawner always active "
		+ "(given that there are players online).",
	"This expression allows trial spawners and trial spawner configurations.",
	"",
	"Base spawners are trial spawner configurations, spawner minecarts and creature spawners."
})
@Examples({
	"set {_range} to activation range of target block",
	"set activation range of target block to 32"
})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.21+")
public class ExprActivationRange extends SimplePropertyExpression<Object, Integer> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, ExprActivationRange.class, Integer.class,
			"spawner activation (radius|range)", "entities/blocks/trialspawnerconfigs");
	}

	@Override
	public @Nullable Integer convert(Object object) {
		if (SpawnerUtils.isBaseSpawner(object)) {
			return SpawnerUtils.getAsBaseSpawner(object).getRequiredPlayerRange();
		} else if (SpawnerUtils.isTrialSpawner(object)) {
			return SpawnerUtils.getAsTrialSpawner(object).getRequiredPlayerRange();
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET -> CollectionUtils.array(Integer.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int count = delta != null ? ((int) delta[0]) : 0;

		for (Object object : getExpr().getArray(event)) {
			if (SpawnerUtils.isBaseSpawner(object)) {
				BaseSpawner spawner = SpawnerUtils.getAsBaseSpawner(object);

				assert spawner != null;

				switch (mode) {
					case SET -> spawner.setRequiredPlayerRange(count);
					case ADD -> spawner.setRequiredPlayerRange(spawner.getRequiredPlayerRange() + count);
					case REMOVE -> spawner.setRequiredPlayerRange(spawner.getRequiredPlayerRange() - count);
					case RESET -> spawner.setRequiredPlayerRange(16); // default value
				}

				SpawnerUtils.updateState(spawner);

			} else if (SpawnerUtils.isTrialSpawner(object)) {
				TrialSpawner spawner = SpawnerUtils.getAsTrialSpawner(object);

				assert spawner != null;

				switch (mode) {
					case SET -> spawner.setRequiredPlayerRange(count);
					case ADD -> spawner.setRequiredPlayerRange(spawner.getRequiredPlayerRange() + count);
					case REMOVE -> spawner.setRequiredPlayerRange(spawner.getRequiredPlayerRange() - count);
					case RESET -> spawner.setRequiredPlayerRange(16); // default value
				}

				SpawnerUtils.updateState(spawner);
			}
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName () {
		return "required player range";
	}

}
