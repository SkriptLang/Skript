package org.skriptlang.skript.bukkit.spawners.elements.expressions.spawner.spawnerdata;

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
import org.skriptlang.skript.bukkit.spawners.util.spawnerdata.SkriptSpawnerData;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Spawn Range")
@Description("""
	Returns the spawn range of the spawner data. The spawn range is the radius of the area in which the spawner \
	can spawn entities.

	By default, the spawn range is 4.
	""")
@Example("""
	set {_data} to spawner data of event-block
	set the spawn range of {_data} to 6
	add 3 to the spawn range of {_data}
	reset the spawn range of {_data}
	""")
@Example("""
	modify the spawner data of event-block:
		set the spawn range to 10
		add 5 to the spawn range
		remove 9 from the spawn range
	""")
@Since("INSERT VERSION")
public class ExprSpawnRange extends SimplePropertyExpression<SkriptSpawnerData, Integer> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprSpawnRange.class, Integer.class,
			"spawn (radi(us[es]|i)|range[s])", "spawnerdatas", true)
				.supplier(ExprSpawnRange::new)
				.build()
		);
	}

	@Override
	public Integer convert(SkriptSpawnerData data) {
		return data.getSpawnRange();
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
		int range = delta != null ? ((int) delta[0]) : 0;

		for (SkriptSpawnerData data : getExpr().getArray(event)) {
			int base = data.getSpawnRange();
			data.setSpawnRange(switch (mode) {
				case ADD -> base + range;
				case REMOVE -> base - range;
				case RESET -> SpawnerUtils.DEFAULT_SPAWN_RANGE;
				default -> range;
			});
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
