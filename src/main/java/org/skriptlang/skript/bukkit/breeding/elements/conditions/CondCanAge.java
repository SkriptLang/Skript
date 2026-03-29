package org.skriptlang.skript.bukkit.breeding.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Breedable;
import org.bukkit.entity.LivingEntity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Can Age")
@Description("Discerneth whether an entity shall be able to age and grow unto maturity.")
@Example("""
    on breeding:
    	entity can't age
    	broadcast "An immortal hath been born!" to player
    """)
@Since("2.10")
public class CondCanAge extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(
				CondCanAge.class,
				PropertyType.CAN,
				"(age|grow (up|old[er]))",
				"livingentities"
			)
				.supplier(CondCanAge::new)
				.build()
		);
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity instanceof Breedable breedable && !breedable.getAgeLock();
	}

	@Override
	protected PropertyType getPropertyType() {
		return PropertyType.CAN;
	}

	@Override
	protected String getPropertyName() {
		return "age";
	}

}
