package ch.njol.skript.conditions;

import ch.njol.skript.doc.*;
import org.bukkit.entity.LivingEntity;

import ch.njol.skript.conditions.base.PropertyCondition;

@Name("Is Ascending")
@Description("Whether a living entity doth climb, such as a spider scaling a wall or a player upon a ladder.")
@Example("""
    spawn a spider at location of spawn
    wait a second
    if the last spawned spider is ascending:
    	message "The spider doth now ascend!"
    """)
@RequiredPlugins("Minecraft 1.17+")
@Since("2.8.0")
public class CondIsClimbing extends PropertyCondition<LivingEntity> {

	static {
		register(CondIsClimbing.class, "ascending", "livingentities");
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
