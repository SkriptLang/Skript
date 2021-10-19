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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.destroystokyo.paper.profile.PlayerProfile;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Hover List")
@Description({"The list when you hover on the player counts of the server in the server list.",
		"This can be changed using texts or players in a <a href='events.html#server_list_ping'>server list ping</a> event only. " +
		"Adding players to the list means adding the name of the players.",
		"And note that, for example if there are 5 online players (includes <a href='#ExprOnlinePlayersCount'>fake online count</a>) " +
		"in the server and the hover list is set to 3 values, Minecraft will show \"... and 2 more ...\" at end of the list."})
@Examples({"on server list ping:",
		"\tclear the hover list",
		"\tadd \"&aWelcome to the &6Minecraft &aserver!\" to the hover list",
		"\tadd \"\" to the hover list # A blank line",
		"\tadd \"&cThere are &6%online players count% &conline players!\" to the hover list"})
@Since("2.3")
@RequiredPlugins("Paper 1.12.2 or newer")
@Events("server list ping")
public class ExprHoverList extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprHoverList.class, String.class, ExpressionType.SIMPLE,
				"[the] [custom] [(player|server)] (hover|sample) ([message] list|message)",
				"[the] [custom] player [(hover|sample)] list");
	}

	private static final boolean PAPER_EVENT_EXISTS = Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent");

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!PAPER_EVENT_EXISTS) {
			Skript.error("The hover list expression requires Paper 1.12.2 or newer");
			return false;
		} else if (!getParser().isCurrentEvent(PaperServerListPingEvent.class)) {
			Skript.error("The hover list expression can't be used outside of a server list ping event");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	public String[] get(Event e) {
		return ((PaperServerListPingEvent) e).getPlayerSample().stream()
				.map(PlayerProfile::getName)
				.toArray(String[]::new);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (getParser().getHasDelayBefore().isTrue()) {
			Skript.error("Can't change the hover list anymore after the server list ping event has already passed");
			return null;
		}
		switch (mode) {
			case SET:
			case ADD:
			case REMOVE:
			case DELETE:
			case RESET:
				return CollectionUtils.array(String[].class, Player[].class);
		}
		return null;
	}

	@SuppressWarnings("null")
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		List<PlayerProfile> values = new ArrayList<>();
		if (mode != ChangeMode.DELETE && mode != ChangeMode.RESET) {
			for (Object o : delta) {
				if (o instanceof Player) {
					Player player = ((Player) o);
					values.add(Bukkit.createProfile(player.getUniqueId(), player.getName()));
				} else {
					values.add(Bukkit.createProfile(UUID.randomUUID(), (String) o));
				}
			}
		}

		List<PlayerProfile> sample = ((PaperServerListPingEvent) e).getPlayerSample();
		switch (mode){
			case SET:
				sample.clear();
				sample.addAll(values);
				break;
			case ADD:
				sample.addAll(values);
				break;
			case REMOVE:
				sample.removeAll(values);
				break;
			case DELETE:
			case RESET:
				sample.clear();
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the hover list";
	}

}