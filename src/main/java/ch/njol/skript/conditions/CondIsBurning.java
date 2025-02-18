package ch.njol.skript.conditions;

import org.bukkit.entity.Entity;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

/**
 * @author Peter Güttinger
 */
@Name("Is Burning")
@Description("Checks whether an entity is on fire, e.g. a zombie due to being in sunlight, or any entity after falling into lava.")
@Examples({"# increased attack against burning targets",
		"victim is burning:",
		"	increase damage by 2"})
@Since("1.4.4")
public class CondIsBurning extends PropertyCondition<Entity> {
	
	static {
		register(CondIsBurning.class, "(burning|ignited|on fire)", "entities");
	}
	
	@Override
	public boolean check(final Entity e) {
		return e.getFireTicks() > 0;
	}
	
	@Override
	protected String getPropertyName() {
		return "burning";
	}
	
}
