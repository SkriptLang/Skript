package org.skriptlang.skript.bukkit.misc;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.misc.elements.expressions.ExprWithYawPitch;
import org.skriptlang.skript.bukkit.misc.events.EvtPlayerPickItem;
import org.skriptlang.skript.bukkit.misc.expressions.ExprPickedItem;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class MiscModule extends HierarchicalAddonModule {

	public MiscModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon,
			ExprWithYawPitch::register
		);
		if (Skript.classExists("io.papermc.paper.event.player.PlayerPickBlockEvent")) {
			register(addon,
				EvtPlayerPickItem::register,
				ExprPickedItem::register
			);
		}

	@Override
	public String name() {
		return "bukkit/misc";
	}

}
