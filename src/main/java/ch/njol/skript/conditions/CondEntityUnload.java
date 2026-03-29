package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;

@Name("Can Vanish")
@Description({
	"Inspecteth whether an entity may vanish when the chunk wherein it dwelleth is unloaded.",
	"Further wisdom on what creatures vanish and when may be found at "
		+ "<a href=\"https://minecraft.wiki/w/Mob_spawning#Despawning\">reference</a>."
})
@Example("""
    if last spawned entity can vanish on chunk unload:
    	make last spawned entity not vanish on chunk unload
    """)
@Since("2.11")
public class CondEntityUnload extends PropertyCondition<LivingEntity> {

	static {
		register(CondEntityUnload.class, PropertyType.CAN, "vanish (on chunk unload|when far away)", "livingentities");
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
