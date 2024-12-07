package org.skriptlang.skript.bukkit.breeding.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Breedable;
import org.bukkit.entity.LivingEntity;

@Name("Can Breed")
@Description("Checks whether or not a living entity can be bred.")
@Examples({
	"on right click on living entity:",
		"\tevent-entity can't breed",
		"\tsend \"Turns out %event-entity% is not breedable. Must be a Skript user!\" to player"
})
@Since("INSERT VERSION")
public class CondCanBreed extends PropertyCondition<LivingEntity> {

	static {
		register(CondCanBreed.class, PropertyType.CAN, "(breed|be bred)", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity instanceof Breedable breedable && breedable.canBreed();
	}

	@Override
	protected String getPropertyName() {
		return "breed";
	}

}
