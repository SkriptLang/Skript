package ch.njol.skript.effects;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;

@Name("Connect")
@Description({
	"Connects a player to another bungeecord server",
	"If the server is running Minecraft 1.20.5 or above, you may specify an IP and Port to transfer a player over to that server.",
	"When transferring players using an IP, the transfer will not complete if the `accepts-transfers` option isn't enabled in `server.properties` for the server specified.",
	"The port will default to `25565` if not specified."
})
@Examples({
	"connect all players to \"hub\"",
	"transfer player to \"my.server.com\"",
	"transfer player to \"localhost\" on port 25566"
})
@Since("2.3, INSERT VERSION (transfer)")
public class EffConnect extends Effect {

	public static final String BUNGEE_CHANNEL = "BungeeCord";
	public static final String GET_SERVERS_CHANNEL = "GetServers";
	public static final String CONNECT_CHANNEL = "Connect";
	private static final boolean TRANSFER_PACKET_EXISTS = Skript.methodExists(Player.class, "transfer", String.class, int.class);

	static {
		Skript.registerEffect(EffConnect.class,
				"connect %players% to [server] %string%",
				"(transfer|send) %players% to server [with ip] %string% [(and|with|on) port %-number%]"
		);
	}

	private Expression<Player> players;
	private Expression<String> server;
	private Expression<Number> port;
	private boolean transfer;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		server = (Expression<String>) exprs[1];
		port = (Expression<Number>) exprs[2];
		transfer = matchedPattern == 1;
		if (!TRANSFER_PACKET_EXISTS && transfer) {
			Skript.error("Transferring players via IP is not available on this version.");
			return false;
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		String server = this.server.getSingle(event);
		Player[] players = this.players.stream(event)
			.filter(Player::isOnline)
			.toArray(Player[]::new);

		if (players.length == 0)
			return;

		if (transfer) {
			int port = 25565; // default port
			if (this.port != null) {
				Number portNum = this.port.getSingle(event);
				if (portNum != null) {
					port = portNum.intValue();
				}
			}
			for (Player player : players) {
				player.transfer(server, port);
			}
		} else {
			// the message channel is case-sensitive, so let's fix that
			Utils.sendPluginMessage(players[0], BUNGEE_CHANNEL, r -> GET_SERVERS_CHANNEL.equals(r.readUTF()), GET_SERVERS_CHANNEL)
				.thenAccept(response -> {
					// for loop isn't as pretty as a stream, but will be faster with tons of servers
					for (String validServer : response.readUTF().split(", ")) {
						if (validServer.equalsIgnoreCase(server)) {
							for (Player player : players)
								Utils.sendPluginMessage(player, BUNGEE_CHANNEL, CONNECT_CHANNEL, validServer);
							break;
						}
					}
				});
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String portString = port != null ? (" and on port " + port.toString(event, debug)) : "";
		return "connect or transfer " + players.toString(event, debug) + " to " + server.toString(event, debug) + portString;
	}

}
