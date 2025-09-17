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
import org.skriptlang.skript.bukkit.spawners.util.spawnerdata.SkriptTrialSpawnerData;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Activation Range")
@Description("""
	Returns the activation range of the spawner data. This is the distance from the spawner within which \
	players must be present for it to remain active. A value of 0 or less makes the spawner always active \
	as long as a player is online.

	By default, the activation range is 16 for mob spawners and 14 for trial spawners.
	""")
@Example("""
	set {_data} to spawner data of event-block
	set activation range of {_data} to 20
	reset activation range of {_data}
	""")
@Example("""
	modify the spawner data of event-block:
		set the activation radius to 20
		add 5 to the activation radius
		remove 3 from the activation range
	""")
@Since("INSERT VERSION")
public class ExprActivationRange extends SimplePropertyExpression<SkriptSpawnerData, Integer> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprActivationRange.class, Integer.class,
			"activation (radi(us[es]|i)|range[s])", "spawnerdatas", true)
				.supplier(ExprActivationRange::new)
				.build()
		);
	}

	@Override
	public Integer convert(SkriptSpawnerData data) {
		return data.getActivationRange();
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

		for (SkriptSpawnerData data : getExpr().getArray(event)) {
			int base = data.getActivationRange();
			data.setActivationRange(switch (mode) {
				case ADD -> base + count;
				case REMOVE -> base - count;
				case RESET -> data instanceof SkriptTrialSpawnerData
					? SpawnerUtils.DEFAULT_TRIAL_ACTIVATION_RANGE
					: SpawnerUtils.DEFAULT_ACTIVATION_RANGE;
				default -> count;
			});
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName () {
		return "activation range";
	}

}
