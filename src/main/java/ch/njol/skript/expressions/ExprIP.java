/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.stream.Stream;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.eclipse.jdt.annotation.Nullable;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("IP")
@Description("The IP address of a player, or the connected player in a <a href='events.html#connect'>connect</a> event, " +
		"or the pinger in a <a href='events.html#server_list_ping'>server list ping</a> event.")
@Examples({"ban the IP address of the player",
		"broadcast \"Banned the IP %IP of player%\"",
		"",
		"on connect:",
		"\tlog \"[%now%] %player% (%ip%) is connected to the server.\"",
		"",
		"on server list ping:",
		"\tsend \"%IP-address%\" to the console"})
@Since("1.4, 2.2-dev26 (when used in connect event), 2.3 (when used in server list ping event)")
public class ExprIP extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprIP.class, String.class, ExpressionType.PROPERTY,
				"IP[s][( |-)address[es]] of %players%",
				"%players%'[s] IP[s][( |-)address[es]]",
				"IP[( |-)address]");
	}

	private static final boolean PAPER_EVENT_EXISTS = Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent");

	@SuppressWarnings("null")
	private Expression<Player> players;

	private boolean isProperty;

	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isProperty = matchedPattern < 2;
		boolean isConnectEvent = getParser().isCurrentEvent(PlayerLoginEvent.class);
		boolean isServerPingEvent = getParser().isCurrentEvent(ServerListPingEvent.class) ||
				(PAPER_EVENT_EXISTS && getParser().isCurrentEvent(PaperServerListPingEvent.class));
		if (isProperty) {
			players = (Expression<Player>) exprs[0];
		} else if (!isConnectEvent && !isServerPingEvent) {
			Skript.error("You must specify players whose IP addresses to get outside of server list ping and connect events.");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event e) {
		if (!isProperty) {
			InetAddress address;
			if (e instanceof PlayerLoginEvent)
				// Return IP address of the connected player in connect event
				address = ((PlayerLoginEvent) e).getAddress();
			else if (e instanceof ServerListPingEvent)
				// Return IP address of the pinger in server list ping event
				address = ((ServerListPingEvent) e).getAddress();
			else
				return null;
			return CollectionUtils.array(address.getHostAddress());
		}

		return Stream.of(players.getArray(e))
				.map(player -> {
					assert player != null;
					return getIP(player, e);
				})
				.toArray(String[]::new);
	}

	private String getIP(Player player, Event e) {
		InetAddress address;
		// The player has no IP yet in a connect event, but the event has it
		// It is a "feature" of Spigot, apparently
		if (e instanceof PlayerLoginEvent && ((PlayerLoginEvent) e).getPlayer().equals(player)) {
			address = ((PlayerLoginEvent) e).getAddress();
		} else {
			InetSocketAddress sockAddr = player.getAddress();
			assert sockAddr != null; // Not in connect event
			address = sockAddr.getAddress();
		}
		
		String hostAddress = address == null ? "unknown" : address.getHostAddress();
		assert hostAddress != null;
		return hostAddress;
	}

	@Override
	public boolean isSingle() {
		return !isProperty || players.isSingle();
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (e == null || !isProperty)
			return "the IP address";
		return "the IP address of " + players.toString(e, debug);
	}

}