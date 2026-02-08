package org.skriptlang.skript.bukkit.entity.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import io.papermc.paper.entity.Shearable;
import org.bukkit.entity.Cow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Entity Is Sheared")
@Description("Checks whether entities are sheared.")
@Example("""
	if targeted entity of player is sheared:
		send "This entity has nothing left to shear!" to player
	""")
@Since("2.8.0")
public class CondIsSheared extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondIsSheared.class, PropertyType.BE, "(sheared|shorn)", "livingentities")
				.supplier(CondIsSheared::new)
				.build()
		);
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Cow) {
			return entity.getEntitySpawnReason() == CreatureSpawnEvent.SpawnReason.SHEARED;
		} else if (entity instanceof Shearable shearable) {
			return !shearable.readyToBeSheared();
		}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "sheared";
	}

}
