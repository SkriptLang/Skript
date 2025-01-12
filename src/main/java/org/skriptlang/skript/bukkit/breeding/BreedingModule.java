package org.skriptlang.skript.bukkit.breeding;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.EventValues;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;
import org.bukkit.inventory.ItemStack;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.breeding.elements.*;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class BreedingModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		SyntaxRegistry registry = addon.syntaxRegistry();

		CondCanAge.register(registry);
		CondCanBreed.register(registry);
		CondIsAdult.register(registry);
		CondIsBaby.register(registry);
		CondIsInLove.register(registry);
		EffAllowAging.register(registry);
		EffBreedable.register(registry);
		EffMakeAdultOrBaby.register(registry);
		EvtBreed.register(registry);
		ExprBreedingFamily.register(registry);
		ExprLoveTime.register(registry);

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
