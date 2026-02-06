package org.skriptlang.skript.bukkit.breeding;

import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.EventValues;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;
import org.bukkit.inventory.ItemStack;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.ChildAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.breeding.elements.*;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;

import java.util.List;

public class BreedingModule extends ChildAddonModule {

	public BreedingModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	public void load(SkriptAddon addon) {
		register(addon, List.of(
			CondCanAge::register,
			CondCanBreed::register,
			CondIsAdult::register,
			CondIsBaby::register,
			CondIsInLove::register,

			EffAllowAging::register,
			EffBreedable::register,
			EffMakeAdultOrBaby::register,

			EvtBreed::register,

			ExprBreedingFamily::register,
			ExprLoveTime::register
		));

		moduleRegistry(addon).register(
				BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Love Mode Enter")
				.addEvent(EntityEnterLoveModeEvent.class)
				.addPatterns(
					"[entity] enter[s] love mode",
					"[entity] love mode [enter]")
				.addDescription("Called whenever an entity enters a state of being in love.")
				.addExample("""
					on love mode enter:
						cancel event # No one is allowed love here
					""")
				.addSince("2.10")
				.build());

		EventValues.registerEventValue(EntityBreedEvent.class, ItemStack.class, EntityBreedEvent::getBredWith);
		EventValues.registerEventValue(EntityEnterLoveModeEvent.class, LivingEntity.class, EntityEnterLoveModeEvent::getEntity);
		EventValues.registerEventValue(EntityEnterLoveModeEvent.class, HumanEntity.class, EntityEnterLoveModeEvent::getHumanEntity);
	}

	@Override
	public String name() {
		return "breeding";
	}

}
