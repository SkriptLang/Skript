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

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.event.server.ServerListPingEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Max Players")
@Description({"The count of max players. This can be changed in a <a href='events.html#server_list_ping'>server list ping</a> event only.",
		"'real max players' returns the real count of max players of the server and can be modified on Paper 1.16 or later."})
@Examples({"on server list ping:",
		"	set the max players count to (online players count + 1)"})
@RequiredPlugins("Paper 1.16+ (modify max real players)")
@Since("2.3, INSERT VERSION (modify max real players)")
public class ExprMaxPlayers extends SimpleExpression<Integer> {

	static {
		Skript.registerExpression(ExprMaxPlayers.class, Integer.class, ExpressionType.PROPERTY,
				"[the] [1:(real|default)|2:(fake|shown|displayed)] max[imum] player[s] [count|amount|number|size]",
				"[the] [1:(real|default)|2:(fake|shown|displayed)] max[imum] (count|amount|number|size) of players"
			);
	}

	private static final boolean PAPER_EVENT_EXISTS = Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent");
	private static final boolean SET_MAX_PLAYERS_EXISTS = Skript.methodExists(Server.class, "setMaxPlayers", int.class);

	private boolean isReal;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		boolean isServerPingEvent = getParser().isCurrentEvent(ServerListPingEvent.class) ||
				(PAPER_EVENT_EXISTS && getParser().isCurrentEvent(PaperServerListPingEvent.class));
		
		if (parseResult.mark == 2 && !isServerPingEvent) {
			Skript.error("The 'shown' max players count expression can't be used outside of a server list ping event");
			return false;
		}
		
		isReal = (parseResult.mark == 0 && !isServerPingEvent) || parseResult.mark == 1;
		return true;
	}

	@Override
	@Nullable
	public Integer[] get(Event event) {
		if (!isReal && !(event instanceof ServerListPingEvent))
			return null;

		if (isReal) {
			return CollectionUtils.array(Bukkit.getMaxPlayers());
		} else {
			return CollectionUtils.array(((ServerListPingEvent) event).getMaxPlayers());
		}
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (!isReal && getParser().getHasDelayBefore().isTrue()) {
			Skript.error("Can't change the fake max players count anymore after the server list ping event has already passed");
			return null;
		}
		
		if (isReal && !SET_MAX_PLAYERS_EXISTS) {
			Skript.error("Modifying the 'real max player count' is only supported on Paper 1.16 and newer");
			return null;
		}
		
		switch (mode) {
			case SET:
			case ADD:
			case REMOVE:
				return CollectionUtils.array(Number.class);
			case RESET:
			case DELETE:
				if (!isReal)
					return CollectionUtils.array(Number.class);
		}
		
		return null;
	}

	@SuppressWarnings("null")
	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		int amount = delta == null ? 0 : ((Number) delta[0]).intValue();
		
		if (!isReal && !(event instanceof ServerListPingEvent))
			return;

		if (isReal) {
			switch (mode) {
				case SET:
					Bukkit.setMaxPlayers(amount);
					break;
				case ADD:
					Bukkit.setMaxPlayers(Bukkit.getMaxPlayers() + amount);
					break;
				case REMOVE:
					Bukkit.setMaxPlayers(Bukkit.getMaxPlayers() - amount);
					break;
			}
		} else {
			ServerListPingEvent pingEvent = (ServerListPingEvent) event;
			switch (mode) {
				case SET:
					pingEvent.setMaxPlayers(amount);
					break;
				case ADD:
					pingEvent.setMaxPlayers(pingEvent.getMaxPlayers() + amount);
					break;
				case REMOVE:
					pingEvent.setMaxPlayers(pingEvent.getMaxPlayers() - amount);
					break;
				case RESET:
				case DELETE:
					pingEvent.setMaxPlayers(Bukkit.getMaxPlayers());
			}
		}
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the count of " + (isReal ? "real max players" : "max players");
	}

}