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

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.coll.CollectionUtils;

@Name("Damage Value/Durability")
@Description("The damage value/durability of an item.")
@Examples({
	"set damage value of player's tool to 10",
	"reset the durability of {_item}",
	"set durability of player's held item to 0"
})
@Since("1.2, 2.7 (durability reversed)")
public class ExprDurability extends SimplePropertyExpression<Object, Long> {

	private boolean durability;

	static {
		register(ExprDurability.class, Long.class, "(damage[s] [value[s]]|durability:durabilit(y|ies))", "itemtypes/slots");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		durability = parseResult.hasTag("durability");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	@Nullable
	public Long convert(Object object) {
		ItemStack itemStack = null;
		if (object instanceof Slot) {
			itemStack = ((Slot) object).getItem();
		} else if (object instanceof ItemType) {
			itemStack = ((ItemType) object).getRandom();
		}
		if (itemStack == null)
			return null;
		long damage = ItemUtils.getDamage(itemStack);
		return durability ? itemStack.getType().getMaxDurability() - damage : damage;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case ADD:
			case REMOVE:
			case DELETE:
			case RESET:
				return CollectionUtils.array(Number.class);
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		int i = delta == null ? 0 : ((Number) delta[0]).intValue();
		Object[] objects = getExpr().getArray(event);
		for (Object object : objects) {
			ItemStack itemStack = null;

			if (object instanceof ItemType) {
				itemStack = ((ItemType) object).getRandom();
			} else if (object instanceof Slot) {
				itemStack = ((Slot) object).getItem();
			}
			if (itemStack == null)
				return;

			int changeValue = ItemUtils.getDamage(itemStack);
			if (durability)
				changeValue = itemStack.getType().getMaxDurability() - changeValue;

			switch (mode) {
				case REMOVE:
					i = -i;
					//$FALL-THROUGH$
				case ADD:
					changeValue += i;
					break;
				case SET:
					changeValue = i;
					break;
				case DELETE:
				case RESET:
					changeValue = 0;
					break;
				case REMOVE_ALL:
					assert false;
			}

			if (durability && mode != ChangeMode.RESET && mode != ChangeMode.DELETE)
				changeValue = itemStack.getType().getMaxDurability() - changeValue;

			if (object instanceof ItemType) {
				ItemUtils.setDamage(itemStack, changeValue);
				((ItemType) object).setTo(new ItemType(itemStack));
			} else {
				ItemUtils.setDamage(itemStack, changeValue);
				((Slot) object).setItem(itemStack);
			}
		}
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	public String getPropertyName() {
		return durability ? "durability" : "damage";
	}

}
