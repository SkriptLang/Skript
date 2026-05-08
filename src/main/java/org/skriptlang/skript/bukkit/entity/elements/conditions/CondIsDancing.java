package org.skriptlang.skript.bukkit.entity.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Allay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Piglin;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Dancing")
@Description("Checks whether an entity is dancing, such as allays, parrots, or piglins.")
@Example("""
	if last spawned allay is dancing:
		broadcast "Dance Party!"
	""")
@Since("2.11")
public class CondIsDancing extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondIsDancing.class, PropertyType.BE, "dancing", "livingentities")
				.supplier(CondIsDancing::new)
				.build()
		);
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Allay allay) {
			return allay.isDancing();
		} else if (entity instanceof Parrot parrot) {
			return parrot.isDancing();
		} else if (entity instanceof Piglin piglin) {
			return piglin.isDancing();
		}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "dancing";
	}

}
