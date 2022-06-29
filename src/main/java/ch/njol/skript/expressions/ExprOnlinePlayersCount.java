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

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.server.ServerListPingEvent;
import org.eclipse.jdt.annotation.Nullable;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
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

@Name("Online Player Count")
@Description({"The amount of online players. This can be changed in a",
		"<a href='events.html#server_list_ping'>server list ping</a> event only to show fake online player amount.",
		"'real online player count' always returns the real count of online players and can't be changed.",
		"",
		"Fake online player count requires PaperSpigot 1.12.2+."})
@Examples({"on server list ping:",
		"	# This will make the max players count 5 if there are 4 players online.",
		"	set the fake max players count to (online players count + 1)"})
@Since("2.3")
public class ExprOnlinePlayersCount extends SimpleExpression<Long> {

	static {
		Skript.registerExpression(ExprOnlinePlayersCount.class, Long.class, ExpressionType.PROPERTY,
				"[the] [(1¦(real|default)|2¦(fake|shown|displayed))] [online] player (count|amount|number)",
				"[the] [(1¦(real|default)|2¦(fake|shown|displayed))] (count|amount|number|size) of online players");
	}

	private static final boolean PAPER_EVENT_EXISTS = Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent");

	private boolean isReal;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		boolean isPaperEvent = PAPER_EVENT_EXISTS && getParser().isCurrentEvent(PaperServerListPingEvent.class);
		if (parseResult.mark == 2) {
			if (getParser().isCurrentEvent(ServerListPingEvent.class)) {
				Skript.error("The 'fake' online players count expression requires Paper 1.12.2 or newer");
				return false;
			} else if (!isPaperEvent) {
				Skript.error("The 'fake' online players count expression can't be used outside of a server list ping event");
				return false;
			}
		}
		isReal = (parseResult.mark == 0 && !isPaperEvent) || parseResult.mark == 1;
		return true;
	}

	@Override
	@Nullable
	public Long[] get(Event e) {
		if (isReal)
			return CollectionUtils.array((long) Bukkit.getOnlinePlayers().size());
		else
			return CollectionUtils.array((long) ((PaperServerListPingEvent) e).getNumPlayers());
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (!isReal) {
			if (getParser().getHasDelayBefore().isTrue()) {
				Skript.error("Can't change the shown online players count anymore after the server list ping event has already passed");
				return null;
			}
			switch (mode) {
				case SET:
				case ADD:
				case REMOVE:
				case DELETE:
				case RESET:
					return CollectionUtils.array(Number.class);
			}
		}
		return null;
	}

	@SuppressWarnings("null")
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		PaperServerListPingEvent event = (PaperServerListPingEvent) e;
		switch (mode) {
			case SET:
				event.setNumPlayers(((Number) delta[0]).intValue());
				break;
			case ADD:
				event.setNumPlayers(event.getNumPlayers() + ((Number) delta[0]).intValue());
				break;
			case REMOVE:
				event.setNumPlayers(event.getNumPlayers() - ((Number) delta[0]).intValue());
				break;
			case DELETE:
			case RESET:
				event.setNumPlayers(Bukkit.getOnlinePlayers().size());
		}
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the count of " + (isReal ? "real max players" : "max players");
	}

}