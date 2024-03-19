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

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.PlayerInventory;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import ch.njol.skript.util.slot.InventorySlot;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;

@Name("Hotbar Slot")
@Description({
	"The currently selected hotbar <a href='./classes.html#slot'>slot</a>.",
	"To retrieve its number use <a href='#ExprSlotIndex'>Slot Index</a> expression.",
	"Use future and past tense to grab the previous slot in an item change event, see example."
})
@Examples({
	"message \"%player's current hotbar slot%\"",
	"set player's selected hotbar slot to slot 4 of player",
	"",
	"send \"index of player's current hotbar slot = 1\" # second slot from the left",
	"",
	"on item held change:",
		"\tif the selected hotbar slot was a diamond:",
			"\t\tset the currently selected hotbar slot to slot 5 of player"
})
@Since("2.2-dev36")
public class ExprHotbarSlot extends PropertyExpression<Player, Slot> {

	static {
		registerDefault(ExprHotbarSlot.class, Slot.class, "[the] [([current:currently] selected|current:current)] hotbar slot[s]", "players");
	}

	// This exists because time states should not register when the 'currently' tag of the syntax is present.
	private boolean current;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<? extends Player>) exprs[0]);
		current = parseResult.hasTag("current");
		return true;
	}

	@Override
	protected Slot[] get(Event event, Player[] source) {
		return get(source, new Getter<Slot, Player>() {
			@Override
			@Nullable
			public Slot get(Player player) {
				int time = getTime();
				PlayerInventory inventory = player.getInventory();
				if (event instanceof PlayerItemHeldEvent && time != EventValues.TIME_NOW) {
					PlayerItemHeldEvent switchEvent = (PlayerItemHeldEvent) event;
					if (time == EventValues.TIME_FUTURE)
						return new InventorySlot(inventory, switchEvent.getNewSlot());
					if (time == EventValues.TIME_PAST)
						return new InventorySlot(inventory, switchEvent.getPreviousSlot());
				}
				return new InventorySlot(inventory, inventory.getHeldItemSlot());
			}
		});
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return new Class[] {Slot.class, Number.class};
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		assert delta != null;
		Object object = delta[0];
		Number number = null;
		if (object instanceof InventorySlot) {
			number = ((InventorySlot) object).getIndex();
		} else if (object instanceof Number) {
			number = (Number) object;
		}

		if (number == null)
			return;
		int index = number.intValue();
		if (index > 8 || index < 0) // Only slots in hotbar can be current hotbar slot
			return;

		for (Player player : getExpr().getArray(event))
			player.getInventory().setHeldItemSlot(index);
	}

	@Override
	public boolean setTime(int time) {
		if (current)
			return super.setTime(time);
		return super.setTime(time, getExpr(), PlayerItemHeldEvent.class);
	}

	@Override
	public Class<? extends Slot> getReturnType() {
		return Slot.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "hotbar slot of " + getExpr().toString(event, debug);
	}

}
