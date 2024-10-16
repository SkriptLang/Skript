package org.skriptlang.skript.bukkit.breeding.elements;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.LivingEntity;

@Name("Is Baby")
@Description("Checks whether or not a living entity is a baby.")
@Examples({
	"on drink:",
		"\tevent-entity is a baby",
		"\tkill event-entity"
})
@Since("INSERT VERSION")
public class CondIsBaby extends PropertyCondition<LivingEntity> {

	static {
		register(CondIsBaby.class, "a (child|baby)", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity instanceof Ageable ageable && !ageable.isAdult();

	}

	@Override
	protected String getPropertyName() {
		return "a baby";
	}

}
