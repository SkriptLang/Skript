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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Player Chat Completions")
@Description({
	"The custom chat completion suggestions. You can add, set, remove, and clear them. Removing online players name with this expression is ineffective.",
	"This expression will not return anything due to Bukkit limitations."
})
@Examples({
	"add \"Skript\" and \"Njol\" to chat completions of all players",
	"remove \"text\" from {_p}'s chat completions",
	"clear player's chat completions"
})
@RequiredPlugins("Spigot 1.19+")
@Since("INSERT VERSION")
public class ExprPlayerChatCompletions extends SimplePropertyExpression<Player, String> {

	static {
		if (Skript.methodExists(Player.class, "addCustomChatCompletions", Collection.class))
			register(ExprPlayerChatCompletions.class, String.class, "[custom] chat completion[s]", "players");
	}

	@Override
	@Nullable
	public String convert(Player player) {
		return null; // Due to Bukkit limitations
	}

	@Override
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
        switch (mode) {
            case ADD:
            case SET:
            case REMOVE:
            case DELETE:
            case RESET:
				return CollectionUtils.array(String[].class);
			default:
				return null;
        }
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Player[] players = getExpr().getArray(event);
		if (players.length == 0)
			return;
		List<String> completions = new ArrayList<>();
		if (delta != null && (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE || mode == ChangeMode.SET)) {
			completions = Arrays.stream(delta)
				.filter(String.class::isInstance)
				.map(String.class::cast)
				.collect(Collectors.toList());
		}
		switch (mode) {
            case DELETE:
            case RESET:
			case SET:
				for (Player player : players)
                	player.setCustomChatCompletions(completions);
                break;
            case ADD:
				for (Player player : players)
					player.addCustomChatCompletions(completions);
				break;
            case REMOVE:
				for (Player player : players)
					player.removeCustomChatCompletions(completions);
                break;
        }
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "custom chat completions";
	}

}
