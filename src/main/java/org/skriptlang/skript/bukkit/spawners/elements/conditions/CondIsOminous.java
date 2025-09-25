package org.skriptlang.skript.bukkit.spawners.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.bukkit.spawners.util.SpawnerUtils;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Trial Spawner Is Ominous")
@Description("""
	Checks whether a trial spawner is ominous. This can also be used with trial spawner block datas.
	""")
@Example("""
	if the block at player is ominous:
		send "The trial spawner is ominous!" to player
	""")
@Example("""
	set {_data} to block data of block at player
	send whether {_data} is ominous to player
	""")
@Since("INSERT VERSION")
public class CondIsOminous extends PropertyCondition<Object> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.CONDITION, infoBuilder(CondIsOminous.class, PropertyType.BE,
			"ominous", "blocks/blockdatas")
				.supplier(CondIsOminous::new)
				.build()
		);
	}

	@Override
	public boolean check(Object object) {
		if (SpawnerUtils.isTrialSpawner(object)) {
			return SpawnerUtils.getTrialSpawner(object).isOminous();
		} else if (object instanceof org.bukkit.block.data.type.TrialSpawner spawner) {
			return spawner.isOminous();
		}

		return false;
	}

	@Override
	protected String getPropertyName() {
		return "ominous";
	}

}
