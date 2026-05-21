package org.skriptlang.skript.bukkit.entity.player.elements.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.connection.PlayerLoginConnection;
import io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@SuppressWarnings("UnstableApiUsage")
public class EvtPlayerLogin extends SkriptEvent {
	// on connection/player init: - when connection is PaperPlayerLoginConnection
	// on connection/player configuration: - when connection is PaperPlayerConfigurationConnection

	public static void register(@NotNull SyntaxRegistry registry) {
		registry.register(
			BukkitSyntaxInfos.Event.KEY,
			BukkitSyntaxInfos.Event.builder(EvtPlayerLogin.class, "Player Login")
				.addEvent(PlayerConnectionValidateLoginEvent.class)
				.addPatterns(
					"player [connection] beginning login", // this pattern sucks
					"player [connection] [finishing|completing] login"
				)
				.addDescription("""
					Called when a player initially connects to a server.
					At the beginning stage, the player information may or may not be available. Only the IP address is guaranteed.
					At the finishing stage, player information is available and things like dialogs may be sent to the user.
					Neither stage has a player object yet, just the player profile. If you need the player
					""")
				.addExample("""
					on player beginning login:
						if {banned_ips::*} contains connection's ip:
							refuse the connection due to "Your IP is banned!"
					""")
				.addExample("""
					on player finishing login:
						if connection's name is "Jeb":
							send "Welcome back, Jeb!" to connection
					""")
				.addSince("INSERT VERSION")
				.build());
	}

	private enum ConnectStage {
		BEGINNING,
		FINISHING
	}

	private ConnectStage stage;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		stage = switch (matchedPattern) {
			case 0 -> ConnectStage.BEGINNING;
			case 1 -> ConnectStage.FINISHING;
			default -> throw new IllegalStateException("Unexpected pattern index: " + matchedPattern);
		};
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof PlayerConnectionValidateLoginEvent loginEvent))
			return false;
		return switch (stage) {
			case BEGINNING -> loginEvent.getConnection() instanceof PlayerLoginConnection;
			case FINISHING -> loginEvent.getConnection() instanceof PlayerConfigurationConnection;
		};
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return switch (stage) {
			case BEGINNING -> "player beginning login";
			case FINISHING -> "player finishing login";
		};
	}

}
