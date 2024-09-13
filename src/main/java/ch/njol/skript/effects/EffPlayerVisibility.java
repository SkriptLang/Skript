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
package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.expressions.ExprHiddenPlayers;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Player Visibility")
@Description({"Change visibility of a player for the given players.",
		"When reveal is used in combination of the <a href='expressions.html#ExprHiddenPlayers'>hidden players</a> expression and the viewers are not specified, " +
		"this will default it to the given player in the hidden players expression.",
		"",
		"Note: if a player was hidden and relogs, this player will be visible again."})
@Examples({"on join:",
		"	if {vanished::%player's uuid%} is true:",
		"		hide the player from all players",
		"",
		"reveal hidden players of {_player}"})
@Since("2.3")
public class EffPlayerVisibility extends Effect {

	private static final boolean USE_DEPRECATED_METHOD = !Skript.methodExists(Player.class, "hidePlayer", Plugin.class, Player.class);
	
	static {
		Skript.registerEffect(EffPlayerVisibility.class,
				"hide %players% [(from|for) %-players%]",
				"reveal %players% [(to|for|from) %-players%]");
	}

	@SuppressWarnings("null")
	private Expression<Player> players;
	
	@Nullable
	private Expression<Player> targetPlayers;
	
	private boolean reveal;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		reveal = matchedPattern == 1;
		players = (Expression<Player>) exprs[0];
		if (reveal && players instanceof ExprHiddenPlayers)
			targetPlayers = exprs.length > 1 ? (Expression<Player>) exprs[1] : ((ExprHiddenPlayers) players).getPlayers();
		else
			targetPlayers = exprs.length > 1 ? (Expression<Player>) exprs[1] : null;
		return true;
	}

    @Override
    @SuppressWarnings("null")
    protected void execute(Event e) {
        Player[] targets = targetPlayers == null ? Bukkit.getOnlinePlayers().toArray(new Player[0]) : targetPlayers.getArray(e);
        for (Player targetPlayer : targets) {
            for (Player player : players.getArray(e)) {
                if (reveal) {
                    if (USE_DEPRECATED_METHOD)
                        targetPlayer.showPlayer(player);
                    else
                        targetPlayer.showPlayer(Skript.getInstance(), player);
                } else {
                    if (USE_DEPRECATED_METHOD)
                        targetPlayer.hidePlayer(player);
                    else
                        targetPlayer.hidePlayer(Skript.getInstance(), player);
                }
            }
        }
    }

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return (reveal ? "show " : "hide ") + players.toString(e, debug) + (reveal ? " to " : " from ") + (targetPlayers != null ? targetPlayers.toString(e, debug) : "");
	}

}