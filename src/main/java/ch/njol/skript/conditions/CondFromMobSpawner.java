package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Entity;

@Name("From A Mob Spawner")
@Description("Check if an entity was spawned from a mob spawner.")
@Examples({
	"send true if target is from a mob spawner"
})
@Since("INSERT VERSION")
public class CondFromMobSpawner extends PropertyCondition<Entity> {

	static {
		register(CondFromMobSpawner.class, PropertyType.BE,
			"from a mob spawner", "entities");
	}

	@Override
	public boolean check(Entity value) {
		return value.fromMobSpawner();
	}

	@Override
	protected String getPropertyName() {
		return "from a mob spawner";
	}

}
