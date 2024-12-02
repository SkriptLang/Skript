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
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

@Name("Item Cooldown")
@Description("Change the cooldown of a specific material to a certain amount of <a href='./classes.html#timespan'>Timespan</a>.")
@Examples({
	"on right click using stick:",
		"\tset item cooldown of player's tool for player to 1 minute",
		"\tset item cooldown of stone and grass for all players to 20 seconds",
		"\treset item cooldown of cobblestone and dirt for all players"
})
@Since("2.8.0")
public class ExprItemCooldown extends SimpleExpression<Timespan> {
	
	static {
		Skript.registerExpression(ExprItemCooldown.class, Timespan.class, ExpressionType.COMBINED, 
				"[the] [item] cooldown of %itemtypes% for %players%",
				"%players%'[s] [item] cooldown for %itemtypes%");
	}
	
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Player> players;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<ItemType> itemtypes;
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) exprs[matchedPattern ^ 1];
		itemtypes = (Expression<ItemType>) exprs[matchedPattern];
		return true;
	}
	
	@Override
	protected Timespan[] get(Event event) {
		Player[] players = this.players.getArray(event);

		List<ItemType> itemTypes = this.itemtypes.stream(event)
				.filter(ItemType::hasType)
				.collect(Collectors.toList());

		Timespan[] timespan = new Timespan[players.length * itemTypes.size()];
		
		int i = 0;
		for (Player player : players) {
			for (ItemType itemType : itemTypes) {
				timespan[i++] = Timespan.fromTicks_i(player.getCooldown(itemType.getMaterial()));
			}
		}
		return timespan;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		return mode == ChangeMode.REMOVE_ALL ? null : CollectionUtils.array(Timespan.class);
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (mode != ChangeMode.RESET && mode != ChangeMode.DELETE && delta == null)
			return;
		
		int ticks = delta != null ? (int) ((Timespan) delta[0]).getTicks_i() : 0; // 0 for DELETE/RESET
		Player[] players = this.players.getArray(event);
		List<ItemType> itemTypes = this.itemtypes.stream(event)
				.filter(ItemType::hasType)
				.collect(Collectors.toList());

		for (Player player : players) {
			for (ItemType itemtype : itemTypes) {
				Material material = itemtype.getMaterial();
				switch (mode) {
					case RESET:
					case DELETE:
					case SET:
						player.setCooldown(material, ticks);
						break;
					case REMOVE:
						player.setCooldown(material, Math.max(player.getCooldown(material) - ticks, 0));
						break;
					case ADD:
						player.setCooldown(material, player.getCooldown(material) + ticks);
						break;
				}
			}
		}
	}

	@Override
	public boolean isSingle() {
		return players.isSingle() && itemtypes.isSingle();
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "cooldown of " + itemtypes.toString(event, debug) + " for " + players.toString(event, debug);
	}

}
