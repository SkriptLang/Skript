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
import org.eclipse.jdt.annotation.Nullable;

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

@Name("Enchant/Disenchant")
@Description("Enchant or disenchant an existing item.")
@Examples({"enchant the player's tool with sharpness 5",
		"disenchant the player's tool",
		"disenchant the player's tool of unbreaking and sharpness",
		"",
		"# For enchanted books",
		"enchant player's tool with stored unbreaking 3",
		"disenchant stored enchantmentes of player's tool"})
@Since("2.0, INSERT VERSION (stored, specific disenchant)")
public class EffEnchant extends Effect {
	static {
		Skript.registerEffect(EffEnchant.class,
				"enchant %~itemtypes% with [(isStored:stored)] %enchantmenttypes%",
				"disenchant %~itemtypes% [(isSpecificDisenchant:of %-enchantmenttypes%)]",
				"disenchant (isStored:stored (isSpecificDisenchant:%-enchantmenttypes%|enchant[ment]s) of) %~itemtypes%");
	}

	@SuppressWarnings("null")
	private Expression<ItemType> items;
	@Nullable
	private Expression<EnchantmentType> enchs;
	private boolean isStored, isDisenchant, isSpecificDisenchant;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 2)
			items = (Expression<ItemType>) exprs[1];
		else
			items = (Expression<ItemType>) exprs[0];

		if (!ChangerUtils.acceptsChange(items, ChangeMode.SET, ItemStack.class)) {
			Skript.error(items + " cannot be changed, thus it cannot be (dis)enchanted");
			return false;
		}
		isStored = parseResult.tags.contains("isStored");
		isDisenchant = matchedPattern > 0;
		isSpecificDisenchant = parseResult.tags.contains("isSpecificDisenchant");
		enchs = exprs.length == 2 ? (Expression<EnchantmentType>) exprs[(matchedPattern == 2 ? 0 : 1)] : null;
		return true;
	}

	@Override
	protected void execute(Event e) {
		ItemType[] items = this.items.getArray(e);
		if (items == null || items.length < 1 || items[0] == null)
			return;
		EnchantmentType[] types = null;
		if ((isSpecificDisenchant || !isDisenchant)) {
			types = enchs.getArray(e);
			if (types == null)
				return;
		}

		for (ItemType i : items) {
			if (!isDisenchant) {
				for (EnchantmentType type : types) {
					Enchantment ench = type.getType();
					assert ench != null;
					if (isStored)
						i.addStoredEnchantments(new EnchantmentType(ench, type.getLevel()));
					else
						i.addEnchantments(new EnchantmentType(ench, type.getLevel()));
				}
			} else {
				if (!isSpecificDisenchant)
					types = isStored ? i.getStoredEnchantmentTypes() : i.getEnchantmentTypes();

				if (types == null)
					return;

				if (isStored)
					i.removeStoredEnchantments(types);
				else
					i.removeEnchantments(types);
			}
		}
		this.items.change(e, items.clone(), ChangeMode.SET);
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (enchs == null)
			return "disenchant " + (isStored ? "stored " + (isSpecificDisenchant ? enchs.toString(e, debug) :
				"enchantments") + " of " : "") + items.toString(e, debug) + (!isStored && isSpecificDisenchant ? " of " +
				enchs.toString(e, debug) : "");
		else
			return "enchant " + items.toString(e, debug) + " with " + (isStored ? "stored " : "") + enchs;
	}

}
