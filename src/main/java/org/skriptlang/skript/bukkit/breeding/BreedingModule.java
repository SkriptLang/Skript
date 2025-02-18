package org.skriptlang.skript.bukkit.breeding;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.EventValues;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class BreedingModule {

	public static void load() throws IOException {
		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.breeding", "elements");

		Skript.registerEvent("Love Mode Enter", SimpleEvent.class, EntityEnterLoveModeEvent.class,
				"[entity] enter[s] love mode", "[entity] love mode [enter]")
			.description("Called whenever an entity enters a state of being in love.")
			.examples(
				"on love mode enter:",
					"\tcancel event # No one is allowed love here"
			)
			.since("2.10");

		EventValues.registerEventValue(EntityBreedEvent.class, ItemStack.class, EntityBreedEvent::getBredWith);
		EventValues.registerEventValue(EntityEnterLoveModeEvent.class, LivingEntity.class, EntityEnterLoveModeEvent::getEntity);
		EventValues.registerEventValue(EntityEnterLoveModeEvent.class, HumanEntity.class, EntityEnterLoveModeEvent::getHumanEntity);
	}

}
