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
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Feed")
@Description("Feeds the specified players.")
@Examples({"feed all players", "feed the player by 5 beefs"})
@Since("2.2-dev34")
public class EffFeed extends Effect {

    static {
        Skript.registerEffect(EffFeed.class, "feed [the] %players% [by %-number% [beef[s]]]");
    }

    @SuppressWarnings("null")
    private Expression<Player> players;
    @Nullable
    private Expression<Number> beefs;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        players = (Expression<Player>) exprs[0];
        beefs = (Expression<Number>) exprs[1];
        return true;
    }

    @Override
    protected void execute(Event e) {
        int level = 20;

        if (beefs != null) {
            Number n = beefs.getSingle(e);
            if (n == null)
                return;
            level = n.intValue();
        }
        for (Player player : players.getArray(e)) {
            player.setFoodLevel(player.getFoodLevel() + level);
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "feed " + players.toString(e, debug) + (beefs != null ? " by " + beefs.toString(e, debug) : "");
    }


}
