package org.skriptlang.skript.bukkit.entity.general.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Can Despawn")
@Description({
	"Check if an entity can despawn when the chunk they're located at is unloaded.",
	"More information on what and when entities despawn can be found at "
		+ "<a href=\"https://minecraft.wiki/w/Mob_spawning#Despawning\">reference</a>."
})
@Example("""
	if last spawned entity can despawn on chunk unload:
		make last spawned entity not despawn on chunk unload
	""")
@Since("2.11")
public class CondEntityUnload extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondEntityUnload.class, PropertyType.CAN, "despawn (on chunk unload|when far away)", "livingentities")
				.supplier(CondEntityUnload::new)
				.build()
		);
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
