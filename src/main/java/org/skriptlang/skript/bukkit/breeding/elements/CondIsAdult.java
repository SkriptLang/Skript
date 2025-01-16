package org.skriptlang.skript.bukkit.breeding.elements;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.LivingEntity;

@Name("Is Adult")
@Description("Checks whether or not a living entity is an adult.")
@Examples({
	"on drink:",
		"\tevent-entity is not an adult",
		"\tkill event-entity"
})
@Since("2.10")
public class CondIsAdult extends PropertyCondition<LivingEntity> {

	static {
		register(CondIsAdult.class, "[an] adult", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity instanceof Ageable ageable && ageable.isAdult();
	}

	@Override
	protected String getPropertyName() {
		return "an adult";
	}

}
