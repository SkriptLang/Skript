package org.skriptlang.skript.bukkit.spawners.elements.expressions.spawner.mobspawnerdata;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawners.util.SpawnerUtils;
import org.skriptlang.skript.bukkit.spawners.util.spawnerdata.SkriptMobSpawnerData;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Spawn Count")
@Description("""
	Returns the spawn count of the mob spawner data. This is the amount of entities the mob spawner \
	will attempt to spawn each spawn attempt. Though, if the spawner is spawning items, the spawn count is the \
	amount of stacks of item to spawn.

	By default, the spawn count is 4.
	""")
@Example("""
	set {_data} to mob spawner data of event-block
	set spawn count of {_data} to 5
	broadcast {_data}'s spawn count
	""")
@Example("""
	modify the mob spawner data of event-block:
		set spawn count of {_data} to 5
		add 2 to spawn count of {_data}
		remove 1 from spawn count of {_data}
		reset spawn count of {_data}
	""")
@Since("INSERT VERSION")
public class ExprSpawnCount extends SimplePropertyExpression<SkriptMobSpawnerData, Integer> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprSpawnCount.class, Integer.class,
			"spawn (count|amount)[s]", "mobspawnerdatas", true)
				.supplier(ExprSpawnCount::new)
				.build()
		);
	}

	@Override
	public Integer convert(SkriptMobSpawnerData data) {
		return data.getSpawnCount();
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

		for (SkriptMobSpawnerData data : getExpr().getArray(event)) {
			int base = data.getSpawnCount();
			data.setSpawnCount(switch (mode) {
				case ADD -> base + count;
				case REMOVE -> base - count;
				case RESET -> SpawnerUtils.DEFAULT_SPAWN_RANGE;
				default -> count;
			});
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
