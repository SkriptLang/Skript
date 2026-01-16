package org.skriptlang.skript.bukkit.entity.general.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Climbing")
@Description("Whether a living entity is climbing, such as a spider up a wall or a player on a ladder.")
@Example("""
	spawn a spider at location of spawn
	wait a second
	if the last spawned spider is climbing:
		message "The spider is now climbing!"
	""")
@RequiredPlugins("Minecraft 1.17+")
@Since("2.8.0")
public class CondIsClimbing extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondIsClimbing.class, PropertyType.BE, "climbing", "livingentities")
				.supplier(CondIsClimbing::new)
				.build()
		);
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity.isClimbing();
	}

	@Override
	protected String getPropertyName() {
		return "climbing";
	}

}
