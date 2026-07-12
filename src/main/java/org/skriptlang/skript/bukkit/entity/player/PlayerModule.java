package org.skriptlang.skript.bukkit.entity.player;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.util.SimpleEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.player.elements.effects.*;
import org.skriptlang.skript.bukkit.entity.player.elements.events.*;
import org.skriptlang.skript.bukkit.entity.player.elements.expressions.*;
import org.skriptlang.skript.bukkit.entity.player.elements.conditions.*;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class PlayerModule extends HierarchicalAddonModule {

	public PlayerModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		EventValueRegistry eventValueRegistry = addon.registry(EventValueRegistry.class);

		register(addon,
			EffBan::register,
			EffKick::register,
			syntaxRegistry -> EvtPlayerGameModeChange.register(syntaxRegistry, eventValueRegistry),
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
			ExprQuitMessage::register,
			ExprRespawnLocation::register,
			CondRespawnLocation::register
		);
		if (Skript.classExists("io.papermc.paper.event.player.PlayerPickBlockEvent")) {
			register(addon,
				EvtPlayerPickItem::register,
				ExprPickedItem::register
			);
		}

		SyntaxRegistry syntaxRegistry = moduleRegistry(addon);

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Chat")
			.addDescription("Called whenever a player chats.",
				"Use <a href='#ExprChatFormat'>chat format</a> to change message format.",
				"Use <a href='#ExprChatRecipients'>chat recipients</a> to edit chat recipients.")
			.addExample("""
				on chat:
					if the player has permission "owner":
						set the chat format to "<red>[player]<light gray>: <light red>[message]"
					else if the player has permission "admin":
						set the chat format to "<light red>[player]<light gray>: <orange>[message]"
					else: # default message format
						set the chat format to "<orange>[player]<light gray>: <white>[message]"
				""")
			.addSince("1.4.1")
			.addPattern("chat")
			.addEvent(AsyncChatEvent.class)
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Player Respawn")
			.addDescription("Called when a player respawns via death or entering the end portal in the end. You should prefer this event over the <a href='#death'>death event</a> as the player is technically alive when this event is called.")
			.addExample("on respawn:")
			.addSince("1.0")
			.addPattern("[player] respawn[ing]")
			.addEvent(PlayerRespawnEvent.class)
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "After Player Respawn")
			.addDescription("Called after a player respawns via death or entering the end portal in the end. You should prefer this event over the <a href='#respawn'>respawn event</a> if you need to ensure your changes to the player stick after they respawn.")
			.addExample("""
				after respawn:
					if respawn location is a bed:
						broadcast "%player% respawned at their bed at %respawn location%"
					else if respawn location is a respawn anchor:
						broadcast "%player% respawned at their anchor at %respawn location%"
					else:
						broadcast "%player% respawned at %respawn location%"
				""")
			.addSince("INSERT VERSION")
			.addPattern("after [player] respawn[ing]")
			.addEvent(PlayerPostRespawnEvent.class)
			.build());
	}

	@Override
	public String name() {
		return "player";
	}

}
