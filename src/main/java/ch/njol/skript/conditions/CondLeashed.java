package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

@Name("Is Leashed")
@Description("Checks to see if an entity is currently leashed.")
@Example("target entity is leashed")
@Since("2.5")
public class CondLeashed extends PropertyCondition<Entity> {

	private static final boolean SUPPORTS_LEASHABLE = Skript.classExists("io.papermc.paper.entity.Leashable");

	static {
		register(CondLeashed.class, PropertyType.BE, "leashed", "entities");
	}

	@Override
	public boolean check(Entity entity) {
		if (SUPPORTS_LEASHABLE) {
			if (entity instanceof io.papermc.paper.entity.Leashable leashable) {
				return leashable.isLeashed();
			}
			return false;
		}
		// Fallback for older versions
		if (entity instanceof LivingEntity livingEntity) {
			return livingEntity.isLeashed();
		}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "leashed";
	}

}
