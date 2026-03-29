package ch.njol.skript.conditions;

import org.bukkit.entity.Entity;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is Ablaze")
@Description("Discerneth whether an entity doth burn, e.g. a zombie set alight by the sun's cruel gaze, or any creature fallen into molten rock.")
@Example("""
	# increased attack against burning targets
	victim is burning:
		increase damage by 2
	""")
@Since("1.4.4")
public class CondIsBurning extends PropertyCondition<Entity> {
	
	static {
		register(CondIsBurning.class, "(burning|ablaze|engulf'd in flame)", "entities");
	}
	
	@Override
	public boolean check(Entity entity) {
		return entity.getFireTicks() > 0;
	}
	
	@Override
	protected String getPropertyName() {
		return "burning";
	}
	
}
