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

@Name("Maximum Nearby Entity Cap")
@Description("""
	Returns the maximum nearby entity cap of similar entities within the spawn range of the mob spawner data. The mob \
	spawner will no longer spawn entities if the value was surpassed.

	By default, the maximum nearby entity cap is 6.
	""")
@Example("""
	set {_data} to mob spawner data of event-block
	set {_data}'s maximum nearby entity cap to 8
	broadcast the maximum nearby entity cap of {_data}
	""")
@Example("""
	modify the mob spawner data of event-block:
		set maximum nearby entity cap of {_data} to 10
		add 5 to maximum nearby entity cap of {_data}
		remove 2 from maximum nearby entity cap of {_data}
		reset maximum nearby entity cap of {_data}
	""")
@Since("INSERT VERSION")
public class ExprMaxNearbyEntities extends SimplePropertyExpression<SkriptMobSpawnerData, Integer> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprMaxNearbyEntities.class, Integer.class,
			"max[imum] nearby entity (count|amount|cap)[s]", "mobspawnerdatas", true)
				.supplier(ExprMaxNearbyEntities::new)
				.build()
		);
	}

	@Override
	public Integer convert(SkriptMobSpawnerData data) {
		return data.getMaxNearbyEntityCap();
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

		for (SkriptMobSpawnerData data: getExpr().getArray(event)) {
			int base = data.getMaxNearbyEntityCap();
			data.setMaxNearbyEntityCap(switch (mode) {
				case ADD -> base + count;
				case REMOVE -> base - count;
				case RESET -> SpawnerUtils.DEFAULT_MAX_NEARBY_ENTITIES;
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
		return "maximum nearby entity cap";
	}

}
