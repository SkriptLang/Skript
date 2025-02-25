package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;

@Name("Can Despawn")
@Description({
	"Check if an entity can despawn when the chunk they're located at is unloaded.",
	"More information on what and when entities despawn can be found at "
		+ "<a href=\"https://minecraft.wiki/w/Mob_spawning#Despawning\">reference</a>."
})
@Examples({
	"if last spawned entity can despawn on chunk unload:",
		"\tmake last spawned entity not despawn on chunk unload"
})
@Since("INSERT VERSION")
public class CondEntityUnload extends PropertyCondition<LivingEntity> {

	static {
		register(CondEntityUnload.class, PropertyType.CAN, "despawn (on chunk unload|when far away)", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity.getRemoveWhenFarAway();
	}

	@Override
	protected String getPropertyName() {
		return "despawn on chunk unload";
	}

}
