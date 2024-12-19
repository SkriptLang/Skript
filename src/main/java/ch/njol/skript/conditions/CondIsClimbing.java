package ch.njol.skript.conditions;

import org.bukkit.entity.LivingEntity;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;

@Name("Is Climbing")
@Description("Whether a living entity is climbing, such as a spider up a wall or a player on a ladder.")
@Examples({
	"spawn a spider at location of spawn",
	"wait a second",
	"if the last spawned spider is climbing:",
		"\tmessage\"The spider is now climbing!\""
})
@RequiredPlugins("Minecraft 1.17+")
@Since("2.8.0")
public class CondIsClimbing extends PropertyCondition<LivingEntity> {

	static {
		if (Skript.methodExists(LivingEntity.class, "isClimbing"))
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
