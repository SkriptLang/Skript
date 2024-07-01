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

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
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
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Has Item Cooldown")
@Description("Check whether a cooldown is active on the specified material for a specific player.")
@Examples({
	"if player has player's tool on cooldown:",
		"\tsend \"You can't use this item right now. Wait %item cooldown of player's tool for player%\""
})
@Since("2.8.0")
public class CondHasItemCooldown extends Condition {

	static {
		Skript.registerCondition(CondHasItemCooldown.class, 
				"%players% (has|have) [([an] item|a)] cooldown (on|for) %itemtypes%",
				"%players% (has|have) %itemtypes% on [(item|a)] cooldown",
				"%players% (doesn't|does not|do not|don't) have [([an] item|a)] cooldown (on|for) %itemtypes%",
				"%players% (doesn't|does not|do not|don't) have %itemtypes% on [(item|a)] cooldown");
	}

	private Expression<Player> players;
	private Expression<ItemType> itemtypes;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		itemtypes = (Expression<ItemType>) exprs[1];
		setNegated(matchedPattern > 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return players.check(event, (player) ->
				itemtypes.check(event, (itemType) ->
					itemType.hasType() && player.hasCooldown(itemType.getMaterial())
				)
		);
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.HAVE, event, debug, players,
				itemtypes.toString(event, debug) + " on cooldown");
	}

}
