package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Entity;

@Name("Is Frozen")
@Description("Checks whether an entity is frozen.")
@Example("""
	if player is frozen:
		kill player
	""")
@Since("2.7")
public class CondIsFrozen extends PropertyCondition<Entity> {

	static {
		if (Skript.methodExists(Entity.class, "isFrozen"))
			register(CondIsFrozen.class, "frozen", "entities");
	}

	@Override
	public boolean check(Entity entity) {
		return entity.isFrozen();
	}

	@Override
	protected String getPropertyName() {
		return "frozen";
	}

}
