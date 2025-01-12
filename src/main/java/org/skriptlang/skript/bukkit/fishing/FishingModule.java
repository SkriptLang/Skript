package org.skriptlang.skript.bukkit.fishing;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.fishing.elements.*;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class FishingModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		SyntaxRegistry registry = addon.syntaxRegistry();

		CondFishingLure.register(registry);
		CondIsInOpenWater.register(registry);
		EffFishingLure.register(registry);
		EffPullHookedEntity.register(registry);
		EvtBucketEntity.register(registry);
		EvtFish.register(registry);
		ExprFishingApproachAngle.register(registry);
		ExprFishingBiteTime.register(registry);
		ExprFishingHook.register(registry);
		ExprFishingHookEntity.register(registry);
		ExprFishingWaitTime.register(registry);
	}

}
