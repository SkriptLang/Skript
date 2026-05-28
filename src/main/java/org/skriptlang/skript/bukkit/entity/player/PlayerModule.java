package org.skriptlang.skript.bukkit.entity.player;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.util.SimpleEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.player.elements.events.*;
import org.skriptlang.skript.bukkit.entity.player.elements.expressions.*;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class PlayerModule extends HierarchicalAddonModule {

	public PlayerModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon,
			ExprChatFormat::register,
			ExprChatMessage::register,
			ExprChatRecipients::register,
			ExprJoinMessage::register,
			ExprKickMessage::register,
			ExprOnScreenKickMessage::register,
			ExprPlayerListHeaderFooter::register,
			ExprPlayerListName::register,
			ExprQuitMessage::register
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
	}

	@Override
	public String name() {
		return "player";
	}

}
