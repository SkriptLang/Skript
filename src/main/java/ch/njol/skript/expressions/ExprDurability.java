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

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
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

/**
 * @author Peter Güttinger
 */
@Name("Data/Damage Value")
@Description({"The data/damage value of an item/block. Data values of blocks are only supported on 1.12.2 and below.",
		"You usually don't need this expression as you can check and set items with aliases easily, ",
		"but this expression can e.g. be used to \"add 1 to data of &lt;item&gt;\", e.g. for cycling through all wool colours."})
@Examples({"set damage value of player's tool to 10",
		"set data value of target block of player to 3",
		"add 1 to the data value of the clicked block",
		"reset data value of block at player"})
@Since("1.2")
public class ExprDurability extends SimplePropertyExpression<Object, Long> {

	static {
		register(ExprDurability.class, Long.class, "((data|damage)[s] [value[s]]|durabilit(y|ies))", "itemtypes/blocks/slots");
	}
	
	@Override
	@Nullable
	public Long convert(final Object o) {
		if (o instanceof Slot) {
			final ItemStack i = ((Slot) o).getItem();
			return i == null ? null : (long) ItemUtils.getDamage(i);
		} else if (o instanceof ItemType) {
			ItemStack item = ((ItemType) o).getRandom();
			return (long) ItemUtils.getDamage(item);
		}
		return null;
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		switch (mode) {
			case ADD:
			case SET:
			case RESET:
			case REMOVE:
			case DELETE:
				return CollectionUtils.array(Number.class);
		}
		return null;
	}
	
	@SuppressWarnings("null")
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		int a = delta == null ? 0 : ((Number) delta[0]).intValue();
		final Object[] os = getExpr().getArray(e);
		for (final Object o : os) {
			ItemStack itemStack = null;
			Block block = null;
			
			if (o instanceof ItemType)
				itemStack = ((ItemType) o).getRandom();
			else if (o instanceof Slot)
				itemStack = ((Slot) o).getItem();
			else
				return;
			
			int changeValue = itemStack != null ? ItemUtils.getDamage(itemStack) : block != null ? block.getData() : 0;
			
			switch (mode) {
				case REMOVE:
					a = -a;
					//$FALL-THROUGH$
				case ADD:
					changeValue += a;
					break;
				case SET:
					changeValue = a;
					break;
				case DELETE:
				case RESET:
					changeValue = 0;
					break;
				case REMOVE_ALL:
					assert false;
			}
			if (o instanceof ItemType && itemStack != null) {
				ItemUtils.setDamage(itemStack,changeValue);
				((ItemType) o).setTo(new ItemType(itemStack));
			} else if (o instanceof Slot) {
				ItemUtils.setDamage(itemStack,changeValue);
				((Slot) o).setItem(itemStack);
			} else {
				BlockState blockState = ((Block) o).getState();
				try {
					blockState.setRawData((byte) Math.max(0, changeValue));
					blockState.update();
				} catch (IllegalArgumentException | NullPointerException ignore) {} // Catch when a user sets the amount too high
			}
		}
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	public String getPropertyName() {
		return "data";
	}

}
