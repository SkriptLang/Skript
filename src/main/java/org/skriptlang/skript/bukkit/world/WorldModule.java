package org.skriptlang.skript.bukkit.world;

import ch.njol.skript.Skript;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.world.boarder.events.EvtWorldBoarder;
import org.skriptlang.skript.bukkit.world.elements.events.EvtWorld;

public class WorldModule extends HierarchicalAddonModule {

	public WorldModule(AddonModule parentModule) { super(parentModule); }

	@Override
	public void loadSelf(SkriptAddon addon) {
		register(addon,
			EvtWorld::register
		);

		if (Skript.classExists("io.papermc.paper.event.world.border.WorldBorderEvent")) {
			register(addon,
				EvtWorldBoarder::register
			);
		}
	}

	@Override
	public String name() { return "world"; }

}
