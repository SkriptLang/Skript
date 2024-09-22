package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Breedable;
import org.bukkit.entity.LivingEntity;

@Name("Can Breed")
@Description("Returns whether or not a living entity can be bred.")
@Examples({
	"on right click on living entity:",
		"\tevent-entity can't breed",
		"\tsend \"Turns out %event-entity% is not breedable. Must be a Skript user!\" to player"
})
@Since("INSERT VERSION")
public class CondCanBreed extends PropertyCondition<LivingEntity> {

	static {
		if (Skript.classExists("org.bukkit.entity.Breedable"))
			register(CondCanBreed.class, PropertyType.CAN, "breed", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Breedable breedable)
			return breedable.canBreed();

		return false;
	}

	@Override
	protected String getPropertyName() {
		return "breed";
	}

}
