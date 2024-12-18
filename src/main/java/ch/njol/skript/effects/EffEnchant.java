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
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

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
		Function<ItemType, ItemType> changeFunction;

		if (enchantments != null) {
			EnchantmentType[] types = enchantments.getArray(event);
			if (types.length == 0)
				return;
			changeFunction = item -> {
				item.addEnchantments(types);
				return item;
			};
		} else {
			changeFunction = item -> {
				item.clearEnchantments();
				return item;
			};
		}

		this.items.changeInPlace(event, changeFunction);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return enchantments == null ? "disenchant " + items.toString(event, debug) : "enchant " + items.toString(event, debug) + " with " + enchantments;
	}
	
}
