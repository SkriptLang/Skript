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

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Enchant/Disenchant")
@Description("Enchant or disenchant an existing item.")
@Examples({"enchant the player's tool with sharpness 5",
		"disenchant the player's tool"})
@Since("2.0")
public class EffEnchant extends Effect {
	static {
		Skript.registerEffect(EffEnchant.class,
				"enchant %~itemtypes% with %enchantmenttypes%",
				"disenchant %~itemtypes%");
	}
	
	@SuppressWarnings("null")
	private Expression<ItemType> items;
	@Nullable
	private Expression<EnchantmentType> enchantments;
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		items = (Expression<ItemType>) exprs[0];
		if (!ChangerUtils.acceptsChange(items, ChangeMode.SET, ItemStack.class)) {
			Skript.error(items + " cannot be changed, thus it cannot be (dis)enchanted");
			return false;
		}
		if (matchedPattern == 0)
			enchantments = (Expression<EnchantmentType>) exprs[1];
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		ItemType[] items = this.items.getArray(event);
		if (items.length == 0) // short circuit
			return;

		if (enchantments != null) {
			EnchantmentType[] types = enchantments.getArray(event);
			if (types.length == 0)
				return;
			for (ItemType item : items) {
				for (EnchantmentType type : types) {
					Enchantment enchantment = type.getType();
					assert enchantment != null;
					item.addEnchantments(new EnchantmentType(enchantment, type.getLevel()));
				}
			}
		} else {
			for (ItemType item : items) {
				item.clearEnchantments();
			}
		}
		this.items.change(event, items.clone(), ChangeMode.SET);
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return enchantments == null ? "disenchant " + items.toString(event, debug) : "enchant " + items.toString(event, debug) + " with " + enchantments;
	}
	
}
