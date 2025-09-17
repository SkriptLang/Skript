package org.skriptlang.skript.bukkit.spawners.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import org.skriptlang.skript.bukkit.spawners.util.SpawnerUtils;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Mob Spawner")
@Description("""
	Checks whether a block or entity is a mob spawner (monster spawner or spawner minecart).
	""")
@Example("""
	broadcast whether event-block is a mob spawner
	""")
@Example("""
	if event-entity is a mob spawner:
		broadcast "%event-entity% is a mob spawner!"
	""")
public class CondIsMobSpawner extends PropertyCondition<Object> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.CONDITION, infoBuilder(CondIsMobSpawner.class, PropertyType.BE,
			"[a] mob spawner", "blocks/entities")
				.supplier(CondIsMobSpawner::new)
				.build()
		);
	}

	@Override
	public boolean check(Object spawnerObject) {
		return SpawnerUtils.isMobSpawner(spawnerObject);
	}

	@Override
	protected String getPropertyName() {
		return "a mob spawner";
	}

}
