package org.skriptlang.skript.bukkit.entity.player;

import ch.njol.skript.Skript;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.player.elements.PlayerEvents;
import org.skriptlang.skript.bukkit.entity.player.elements.effects.*;
import org.skriptlang.skript.bukkit.entity.player.elements.events.*;
import org.skriptlang.skript.bukkit.entity.player.elements.expressions.*;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;

public class PlayerModule extends HierarchicalAddonModule {

	public PlayerModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		EventValueRegistry eventValueRegistry = addon.registry(EventValueRegistry.class);
		register(addon,
			syntaxRegistry -> PlayerEvents.register(syntaxRegistry, eventValueRegistry),
			syntaxRegistry -> EvtPlayerArmorChange.register(syntaxRegistry, eventValueRegistry),
			syntaxRegistry -> EvtPlayerFillBucket.register(syntaxRegistry, eventValueRegistry),
			syntaxRegistry -> EvtPlayerGameModeChange.register(syntaxRegistry, eventValueRegistry),
			syntaxRegistry -> EvtPlayerMoveOn.register(syntaxRegistry, eventValueRegistry),
			EvtPlayerSpectate::register,
			EffBan::register,
			EffKick::register,
			ExprChatFormat::register,
			ExprChatMessage::register,
			ExprChatRecipients::register,
			ExprGameMode::register,
			ExprJoinMessage::register,
			ExprKickMessage::register,
			ExprOnScreenKickMessage::register,
			ExprPlayerListHeaderFooter::register,
			ExprPlayerListName::register,
			ExprPlayerListPriority::register,
			ExprQuitMessage::register
		);
		if (Skript.classExists("io.papermc.paper.event.player.PlayerPickBlockEvent")) {
			register(addon,
				EvtPlayerPickItem::register,
				ExprPickedItem::register
			);
		}
	}

	@Override
	public String name() {
		return "player";
	}

}
