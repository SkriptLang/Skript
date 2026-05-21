package org.skriptlang.skript.bukkit.entity.player;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.Classes;
import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.connection.PlayerLoginConnection;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.player.elements.events.EvtPlayerLogin;
import org.skriptlang.skript.bukkit.entity.player.elements.events.EvtPlayerPickItem;
import org.skriptlang.skript.bukkit.entity.player.elements.expressions.*;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;
import org.skriptlang.skript.registration.SyntaxRegistry;

/**
 * @noinspection UnstableApiUsage
 */
public class PlayerModule extends HierarchicalAddonModule {

	public PlayerModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void initSelf(SkriptAddon addon) {
		registerConnectionTypes(addon);
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
			ExprQuitMessage::register,

			EvtPlayerLogin::register
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

	private void registerConnectionTypes(SkriptAddon addon) {
		Classes.registerClass(new ClassInfo<>(PlayerLoginConnection.class, "playerloginconnection")
			.user("player ?login ?connections?")
			.name("Player Login Connection")
			.description("Represents a player's connection during the login process. This will have the player's IP address, UUID, and name.")
			.since("INSERT VERSION")
			.property(Property.NAME,
				"The username of the player trying to log in.",
				addon,
				ExpressionPropertyHandler.of(connection -> {
					PlayerProfile profile = connection.getAuthenticatedProfile();
					if (profile != null)
						return profile.getName();
					profile = connection.getUnsafeProfile();
					if (profile != null)
						return profile.getName();
					return null;
				}, String.class))
			.property(Property.)
		);
	}

}
