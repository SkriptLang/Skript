package org.skriptlang.skript.bukkit.misc.elements.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import org.bukkit.event.Event;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtServerListPing extends SkriptEvent {

	public static void register(SyntaxRegistry registry) {

		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtServerListPing.class, "Server List Ping")
				.addEvent(PaperServerListPingEvent.class)
				.addPatterns("server [list] ping")
				.addDescription(
					"Called when a server list ping is coming in, generally when a Minecraft client pings the server to show its information in the server list.",
					"The <a href='#ExprIP'>IP</a> expression can be used to get the IP adress of the pinger.",
					"This event can be cancelled on PaperSpigot 1.12.2+ only and this means the player will see the server as offline (but still can join).",
					"",
					"Also you can use <a href='#ExprMOTD'>MOTD</a>, <a href='#ExprMaxPlayers'>Max Players</a>, " +
						"<a href='#ExprOnlinePlayersCount'>Online Players Count</a>, <a href='#ExprProtocolVersion'>Protocol Version</a>, " +
						"<a href='#ExprVersionString'>Version String</a>, <a href='#ExprHoverList'>Hover List</a> and <a href='#ExprServerIcon'>Server Icon</a> " +
						"expressions, and <a href='#EffPlayerInfoVisibility'>Player Info Visibility</a> and <a href='#EffHidePlayerFromServerList'>Hide Player from Server List</a> effects to modify the server list."
				)
				.addExample("""
					on server list ping:
						set the motd to "Welcome %{player-by-IP::%ip%}%! Join now!" if {player-by-IP::%ip%} is set, else "Join now!"
						set the fake max players count to (online players count + 1)
						set the shown icon to a random server icon out of {server-icons::*}
					""")
				.addSince("2.3")
				.supplier(EvtServerListPing::new)
				.build()
		);
	}

	@Override
	public boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		return true;
	}

	@Override
	public boolean check(Event event) {
		return true;
	}

	@Override
	public String toString(Event event, boolean debug) { return "server list ping event"; }

}
