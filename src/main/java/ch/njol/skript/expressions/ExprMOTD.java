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

@Name("MOTD")
@Description({"The message of the day in the server list. " +
		"This can be changed in a <a href='events.html#server_list_ping'>server list ping</a> event only.",
		"'default MOTD' returns the default MOTD always and can't be changed."})
@Examples({"on server list ping:",
		"	set the motd to \"Join now!\""})
@Since("2.3")
public class ExprMOTD extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprMOTD.class, String.class, ExpressionType.SIMPLE, "[the] [(1¦default)|(2¦shown|displayed)] (MOTD|message of [the] day)");
	}

	private static final boolean PAPER_EVENT_EXISTS = Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent");

	private boolean isDefault;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		boolean isServerPingEvent = getParser().isCurrentEvent(ServerListPingEvent.class) ||
				(PAPER_EVENT_EXISTS && getParser().isCurrentEvent(PaperServerListPingEvent.class));
		if (parseResult.mark == 2 && !isServerPingEvent) {
			Skript.error("The 'shown' MOTD expression can't be used outside of a server list ping event");
			return false;
		}
		isDefault = (parseResult.mark == 0 && !isServerPingEvent) || parseResult.mark == 1;
		return true;
	}

	@Override
	@Nullable
	public String[] get(Event e) {
		if (!isDefault && !(e instanceof ServerListPingEvent))
			return null;

		if (isDefault)
			return CollectionUtils.array(Bukkit.getMotd());
		else
			return CollectionUtils.array(((ServerListPingEvent) e).getMotd());
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (!isDefault) {
			if (getParser().getHasDelayBefore().isTrue()) {
				Skript.error("Can't change the MOTD anymore after the server list ping event has already passed");
				return null;
			}
			switch (mode) {
				case SET:
				case DELETE:
				case RESET:
					return CollectionUtils.array(String.class);
			}
		}
		return null;
	}

	@SuppressWarnings("null")
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (!(e instanceof ServerListPingEvent))
			return;

		ServerListPingEvent event = (ServerListPingEvent) e;
		switch (mode) {
			case SET:
				event.setMotd((String) delta[0]);
				break;
			case DELETE:
				event.setMotd("");
				break;
			case RESET:
				event.setMotd(Bukkit.getMotd());
		}
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the " + (isDefault ? "default MOTD" : "MOTD");
	}

}