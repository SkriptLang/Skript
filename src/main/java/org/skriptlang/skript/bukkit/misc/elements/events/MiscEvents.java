package org.skriptlang.skript.bukkit.misc.elements.events;

import ch.njol.skript.lang.util.SimpleEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.bukkit.inventory.ItemStack;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class MiscEvents {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Broadcast Message")
			.addEvent(BroadcastMessageEvent.class)
			.addPatterns(
				"broadcast [message]",
				"message being broadcast[ed]"
			)
			.addDescription("Called when a message is broadcasted.")
			.addExample("""
				on message being broadcasted:
				   set broadcast-message to "<gray>[<red><bold>BROADCAST<reset><gray>] <white>%broadcasted message%"
				""")
			.addSince("2.10")
			.addSince("INSERT VERSION (pattern update)")
			.supplier(() -> new SimpleEvent("broadcast message"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Server List Ping")
			.addEvent(BroadcastMessageEvent.class)
			.addPattern("server [list] ping")
			.addDescription("""
				Called when a server list ping is coming in, generally when a Minecraft client pings the server to show its information in the server list.
				The <a href='#ExprIP'>IP</a> expression can be used to get the IP address of the pinger.
				Cancelling this event will make the player will see the server as offline (but still can join).
				
				See <a href='#ExprMOTD'>MOTD</a>, <a href='#ExprMaxPlayers'>Max Players</a>,\s
				<a href='#ExprOnlinePlayersCount'>Online Players Count</a>, <a href='#ExprProtocolVersion'>Protocol Version</a>,\s
				<a href='#ExprVersionString'>Version String</a>,\s
				<a href='#ExprHoverList'>Hover List</a> and <a href='#ExprServerIcon'>Server Icon</a>\s
				<a href='#EffPlayerInfoVisibility'>Player Info Visibility</a> and <a href='#EffHidePlayerFromServerList'>Hide Player from Server List</a>\s
				for how to modify the server list.
				""")
			.addExample("""
				on server list ping:
				    set the message of the day to "Welcome %{player-by-IP::%ip%}%! Join now!" if {player-by-IP::%ip%} is set, else "Join now!"
				    set the fake max players count to (size of all players + 1)
				    set the shown icon to a random server icon out of {server-icons::*}
				""")
			.addSince("2.3")
			.supplier(() -> new SimpleEvent("server list ping"))
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(SimpleEvent.class, "Anvil Prepare")
			.addEvent(PrepareAnvilEvent.class)
			.addPattern("anvil prepar(e|ing)")
			.addDescription("""
				Called when an item is put in a slot for repair by an anvil.
				Note that this event is called multiple times in a single item slot move.
				""")
			.addExample("""
				on anvil prepare:
				    event-item is set
				    chance of 5%:
				        set repair cost to cost * 50%
				        send "Your LUCKY! You got 50% discount!" to player
				""")
			.addSince("2.7")
			.supplier(() -> new SimpleEvent("anvil prepare"))
			.build());

		eventValueRegistry.register(EventValue.builder(PrepareAnvilEvent.class, ItemStack.class)
			.getter(PrepareAnvilEvent::getResult)
			.build());
	}
}
