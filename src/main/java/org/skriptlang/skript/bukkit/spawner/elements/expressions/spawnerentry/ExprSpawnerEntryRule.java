package org.skriptlang.skript.bukkit.spawner.elements.expressions.spawnerentry;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.spawner.SpawnRule;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;

public class ExprSpawnerEntryRule extends SimplePropertyExpression<SpawnerEntry, SpawnRule> {

	static {
		registerDefault(SpawnerModule.SYNTAX_REGISTRY, ExprSpawnerEntryRule.class, SpawnRule.class,
				"spawn rule", "spawnerentries");
	}

	@Override
	public @Nullable SpawnRule convert(SpawnerEntry entry) {
		return entry.getSpawnRule();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE -> CollectionUtils.array(SpawnRule.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		SpawnRule rule = delta != null ? (SpawnRule) delta[0] : null;

		for (SpawnerEntry entry : getExpr().getArray(event)) {
			entry.setSpawnRule(rule);
		}
	}

	@Override
	public Class<? extends SpawnRule> getReturnType() {
		return SpawnRule.class;
	}

	@Override
	protected String getPropertyName() {
		return "spawner entry rule";
	}

}
