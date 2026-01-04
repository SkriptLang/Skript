package org.skriptlang.skript.bukkit.fishing;

import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.registrations.Classes;
import org.bukkit.event.player.PlayerFishEvent;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.ChildAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.fishing.elements.conditions.CondFishingLure;
import org.skriptlang.skript.bukkit.fishing.elements.conditions.CondIsInOpenWater;
import org.skriptlang.skript.bukkit.fishing.elements.effects.EffFishingLure;
import org.skriptlang.skript.bukkit.fishing.elements.effects.EffPullHookedEntity;
import org.skriptlang.skript.bukkit.fishing.elements.events.EvtBucketEntity;
import org.skriptlang.skript.bukkit.fishing.elements.events.EvtFish;
import org.skriptlang.skript.bukkit.fishing.elements.expressions.*;

public class FishingModule extends ChildAddonModule {

	/**
	 * Constructs a child addon module with the given parent module.
	 *
	 * @param parentModule The parent module that created this child module.
	 */
	public FishingModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	public void init(SkriptAddon addon) {
		Classes.registerClass(new EnumClassInfo<>(PlayerFishEvent.State.class, "fishingstate", "fishing states")
			.user("fishing ?states?")
			.name("Fishing State")
			.description("Represents the different states of a fishing event.")
			.since("2.11")
		);
	}

	@Override
	public void load(SkriptAddon addon) {
		AddonModule.register(addon.syntaxRegistry(), origin(addon),
			CondFishingLure::register,
			CondIsInOpenWater::register,

			EffFishingLure::register,
			EffPullHookedEntity::register,

			EvtBucketEntity::register,
			EvtFish::register,

			ExprFishingApproachAngle::register,
			ExprFishingBiteTime::register,
			ExprFishingHook::register,
			ExprFishingHookEntity::register,
			ExprFishingWaitTime::register);
	}

	@Override
	public String name() {
		return "fishing";
	}
}
