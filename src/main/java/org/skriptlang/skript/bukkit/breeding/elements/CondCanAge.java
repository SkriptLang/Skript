package org.skriptlang.skript.bukkit.breeding.elements;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Breedable;
import org.bukkit.entity.LivingEntity;

@Name("Can Age")
@Description("Checks whether or not an entity will be able to age/grow up.")
@Examples({
	"on breeding:",
		"\tentity can't age",
		"\tbroadcast \"An immortal has been born!\" to player"
})
@Since("2.10")
public class CondCanAge extends PropertyCondition<LivingEntity> {

	static {
		register(CondCanAge.class, PropertyType.CAN, "(age|grow (up|old[er]))", "livingentities");
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
