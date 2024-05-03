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
package ch.njol.skript.conditions;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Can See")
@Description({
	"Checks whether the given players can see other entities.",
	"Note this means if the entities have been manually hidden from the player, " +
	"and not if they have line of sight to eachother."
})
@Examples({
	"if the player can't see the player-argument:",
		"\tmessage \"&cThe player %player-argument% is not online!\""
})
@Since("2.3, INSERT VERSION (entities)")
public class CondCanSee extends Condition {

	static {
		Skript.registerCondition(CondCanSee.class,
				"%entities% (is|are) [:in]visible for %players%",
				"%players% can see %entities%",
				"%entities% (is|are)(n't| not) [:in]visible for %players%",
				"%players% can('t| not) see %entities%");
	}

	private Expression<Entity> entities;
	private Expression<Player> players;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 1 || matchedPattern == 3) {
			players = (Expression<Player>) exprs[0];
			entities = (Expression<Entity>) exprs[1];
		} else {
			players = (Expression<Player>) exprs[1];
			entities = (Expression<Entity>) exprs[0];
		}
		setNegated(matchedPattern > 1 ^ parseResult.hasTag("in"));
		return true;
	}

	@Override
	public boolean check(Event event) {
		return entities.check(event, entity -> {
			// Old Spigot API (< 1.19) has Player#canSee(Player) and still exists. So lets directly use it.
			if (entity instanceof Player)
				return players.check(event, player -> player.canSee((Player) entity));
			return players.check(event, player -> player.canSee(entity));
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.CAN, event, debug, players,
				"see " + entities.toString(event, debug));
	}

}
