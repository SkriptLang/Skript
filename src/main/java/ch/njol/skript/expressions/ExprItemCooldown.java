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
import org.eclipse.jdt.annotation.Nullable;

@Name("Item Cooldown")
@Description("Set the cooldown of a specific material for a certain amount of ticks. Setting this to <code>0 tick</code> will remove the cooldown.")
@Examples({
	"on right click using stick:" +
	"\tset item cooldown of player's tool for player to 1 minute",
	"\tset item cooldown of stone and grass for all players to 20 seconds",
	"\treset item cooldown of cobblestone and dirt for all players"
})
@Since("INSERT VERSION")
public class ExprItemCooldown extends SimpleExpression<Timespan> {
	
	static {
		Skript.registerExpression(ExprItemCooldown.class, Timespan.class, ExpressionType.COMBINED, 
			"[the] [item] cooldown of %itemtypes% for %players%",
			"%players%'[s] [item] cooldown for %itemtypes%");
	}
	
	private Expression<Player> players;
	private Expression<ItemType> itemtypes;
	
	@Override
	@SuppressWarnings({"null", "unchecked"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) exprs[1];
		itemtypes = (Expression<ItemType>) exprs[0];
		return true;
	}
	
	@Override
	@SuppressWarnings("null")
	protected Timespan[] get(Event e) {
		Player[] players = this.players.getArray(e);
		ItemType[] itemtypes = this.itemtypes.getArray(e);
		if (players.length == 0 || itemtypes.length == 0)
			return new Timespan[0];

		int size = players.length * itemtypes.length;
		
		Timespan[] timespan = new Timespan[size];
		
		int i = 0;
		for (Player p : players) {
			for (ItemType it : itemtypes) {
				timespan[i] = Timespan.fromTicks_i(p.getCooldown(it.getMaterial()));
				i++;
			}
		}
		return timespan;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case RESET:
			case REMOVE:
			case DELETE:
			case ADD:
				return CollectionUtils.array(Timespan.class);
			default:
				return null;
		} 
	}

	@Override
	public void change(Event e, Object[] delta, ChangeMode mode) {
		if (mode != ChangeMode.RESET && mode != ChangeMode.DELETE && (delta == null || !(delta[0] instanceof Timespan)))
			return;
		
		int timespan = delta != null ? (int) ((Timespan) delta[0]).getTicks_i() : 0;
		Player[] players = this.players.getArray(e);
		ItemType[] itemtypes = this.itemtypes.getArray(e);

		if (players.length == 0 || itemtypes.length == 0)
			return;

		for (Player p : players) {
			for (ItemType it : itemtypes) {
				Material mat = it.getMaterial();
				switch (mode) {
					case RESET:
					case DELETE:
					case SET:
						p.setCooldown(mat, mode == ChangeMode.SET ? timespan : 0); // 0 for DELETE/RESET
						break;
					case REMOVE:
						p.setCooldown(mat, Math.max(p.getCooldown(mat) - timespan, 0));
						break;
					case ADD:
						p.setCooldown(mat, p.getCooldown(mat) + timespan);
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
	public String toString(@Nullable Event e, boolean debug) {
		return "the cooldown of " + itemtypes.toString(e, debug) + " for " + players.toString(e, debug);
	}

}
