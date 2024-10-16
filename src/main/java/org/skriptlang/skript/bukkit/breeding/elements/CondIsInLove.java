package org.skriptlang.skript.bukkit.breeding.elements;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;

@Name("Is In Love")
@Description("Checks whether or not a living entity is in love.")
@Examples({
	"on spawn of living entity:",
		"\tif entity is in love:",
			"broadcast \"That was quick!\""
})
@Since("INSERT VERSION")
public class CondIsInLove extends PropertyCondition<LivingEntity> {

	static {
		register(CondIsInLove.class, "in lov(e|ing) [state|mode]", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Animals animals)
			return animals.isLoveMode();

		return false;
	}

	@Override
	protected String getPropertyName() {
		return "in love";
	}

}
