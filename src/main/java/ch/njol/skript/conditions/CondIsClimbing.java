package ch.njol.skript.conditions;

import ch.njol.skript.doc.*;
import org.bukkit.entity.LivingEntity;

import ch.njol.skript.conditions.base.PropertyCondition;

@Name("Is Climbing")
@Description("Whether a living entity is climbing, such as a spider up a wall or a player on a ladder.")
@Example("""
	spawn a spider at location of spawn
	wait a second
	if the last spawned spider is climbing:
		message "The spider is now climbing!"
	""")
@RequiredPlugins("Minecraft 1.17+")
@Since("2.8.0")
public class CondIsClimbing extends PropertyCondition<LivingEntity> {

	static {
		register(CondIsClimbing.class, "climbing", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity.isClimbing();
	}

	@Override
	protected String getPropertyName() {
		return "climbing";
	}

}
