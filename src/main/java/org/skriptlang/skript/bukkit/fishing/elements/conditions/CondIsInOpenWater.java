package org.skriptlang.skript.bukkit.fishing.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Fish Hook in Open Water")
@Description({
	"Checks whether the fish hook is in open water.",
	"Open water is defined by a 5x4x5 area of water, air and lily pads. " +
	"If in open water, treasure items may be caught."
})
@Example("""
	on fish catch:
		if fish hook is in open water:
			send "You will catch a shark soon!"
	""")
@Events("Fishing")
@Since("2.10")
public class CondIsInOpenWater extends PropertyCondition<Entity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.CONDITION,
			infoBuilder(CondIsInOpenWater.class, PropertyType.BE, "in open water[s]", "entities")
				.supplier(CondIsInOpenWater::new)
				.build());
	}

	@Override
	public boolean check(Entity entity) {
		if (!(entity instanceof FishHook hook))
			return false;

		return hook.isInOpenWater();
	}

	@Override
	protected String getPropertyName() {
		return "in open water";
	}
	
}
