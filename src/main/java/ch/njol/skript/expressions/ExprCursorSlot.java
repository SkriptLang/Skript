/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.slot.CursorSlot;
import ch.njol.skript.util.slot.Slot;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Cursor item slot is not actually an inventory slot, but an item which the player
 * has in their cursor when any inventory is open for them.
 */
@Name("Cursor Slot")
@Description("The item which the player has on their cursor. This slot is always empty if player has no inventories open.")
@Examples({"cursor slot of player is dirt",
	"set cursor slot of player to 64 diamonds"})
@Since("2.2-dev17")
public class ExprCursorSlot extends SimplePropertyExpression<Player, Slot> {

	static {
		register(ExprCursorSlot.class, Slot.class, "cursor slot", "players");
	}

	@Override
	public Class<? extends Slot> getReturnType() {
		return Slot.class;
	}

	@Override
	protected String getPropertyName() {
		return "cursor slot";
	}

	@Override
	@Nullable
	public Slot convert(final Player player) {
		return new CursorSlot(player);
	}

}
