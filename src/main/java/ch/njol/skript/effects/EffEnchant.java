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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

@Name("Enchant/Disenchant")
@Description("Enchant or disenchant an existing item with/from [stored] enchantment.")
@Examples({
	"enchant the player's tool with sharpness 5",
	"disenchant the player's tool",
	"disenchant the player's tool from unbreaking and sharpness",
	"",
	"# For enchanted books",
	"enchant player's tool with stored unbreaking 3",
	"disenchant stored enchantments from player's tool"
})
@Since("2.0, INSERT VERSION (stored, specific disenchant)")
public class EffEnchant extends Effect {
	static {
		Skript.registerEffect(EffEnchant.class,
				"enchant %~itemtypes% with %enchantmenttypes%",
				"disenchant %~itemtypes% [specific:(of|from) %-enchantmenttypes%]",
				"store %~itemtypes% on %enchantmenttypes%",
				"unstore (specific:%enchantmenttypes%|enchant[ment]s) (of|from) %~itemtypes%");
	}

	@SuppressWarnings("null")
	private Expression<ItemType> items;
	@Nullable
	private Expression<EnchantmentType> enchantments;
	private boolean isStored, isDisenchant, isSpecificDisenchant;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		items = (Expression<ItemType>) (matchedPattern == 2 ? exprs[1] : exprs[0]);

		if (!ChangerUtils.acceptsChange(items, ChangeMode.SET, ItemStack.class)) {
			Skript.error(items + " cannot be changed, thus it cannot be (dis)enchanted");
			return false;
		}
		isStored = matchedPattern >= 2;
		isDisenchant = matchedPattern > 0;
		isSpecificDisenchant = parseResult.tags.contains("specific");
		enchantments = exprs.length == 2 ? (Expression<EnchantmentType>) exprs[(matchedPattern == 2 ? 0 : 1)] : null;
		return true;
	}

	@Override
	protected void execute(Event event) {
		ItemType[] items = this.items.getArray(event);
		if (items.length < 1)
			return;

		EnchantmentType[] types = null;

		if ((isSpecificDisenchant || !isDisenchant)) {
			if (enchantments == null)
				return;

			types = enchantments.getArray(event);
			if (types.length < 1)
				return;
		}

		if (!isDisenchant) {
			for (ItemType item : items) {
				for (EnchantmentType type : types) {
					Enchantment enchantment = type.getType();
					assert enchantment != null;
					if (isStored) {
						item.addStoredEnchantments(new EnchantmentType(enchantment, type.getLevel()));
					} else {
						item.addEnchantments(new EnchantmentType(enchantment, type.getLevel()));
					}
				}
			}
		} else {
			for (ItemType item : items) {
				if (!isSpecificDisenchant)
					types = isStored ? item.getStoredEnchantmentTypes() : item.getEnchantmentTypes();

				if (types == null) // returned [stored] enchantments of item might be null
					return;

				if (isStored) {
					item.removeStoredEnchantments(types);
				} else {
					item.removeEnchantments(types);
				}
			}
		}
		this.items.change(event, items.clone(), ChangeMode.SET);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (enchantments == null) {
			return "disenchant " + (isStored ? "stored " + (isSpecificDisenchant ? enchantments.toString(event, debug) :
				"enchantments") + " of " : "") + items.toString(event, debug) + (!isStored && isSpecificDisenchant ? " of " +
				enchantments.toString(event, debug) : "");
		return "enchant " + items.toString(event, debug) + " with " + (isStored ? "stored " : "") + enchantments;
	}

}
