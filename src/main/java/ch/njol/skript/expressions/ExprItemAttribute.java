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

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Multimap;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;

@Name("Item Attribute")
@Description({
		"The numerical value of an item's particular attribute.",
		"It is also possible to limit the attribute to a certain equipment slot. If no limitation is specified, it will work for any slot.",
		"Note that the movement speed attribute cannot be reliably used for players. For that purpose, use the speed expression instead.",
		"Resetting an item's attribute is only available in Minecraft 1.11 and above."
})
@Examples({
		"set {_item} to diamond sword named \"OP SWORD\"",
		"set {_item}'s attack damage attribute value in main hand to 100",
		"set {_talisman} to feather named \"Speed Talisman\"",
		"set {_talisman}'s movement speed attribute in off hand slot to 0.2 # Very fast!"
})
@Since("INSERT VERSION")
public class ExprItemAttribute extends PropertyExpression<ItemType, Number> {

	static {
		register(ExprItemAttribute.class, Number.class, "attribute [value] [(in|of|for) [the] (1:main[ ]hand|2:off[ ]hand|3:boot[s]|3:shoe[s]|4:leg[ging][s]|5:chestplate[s]|6:helm[et][s]) [slot]]", "itemtypes");
	}

	private static final String ATTRIBUTE_NAME = "skript";

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Attribute> attributes;
	@Nullable
	private EquipmentSlot equipmentSlot;

	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		attributes = (Expression<Attribute>) exprs[matchedPattern];
		setExpr((Expression<ItemType>) exprs[matchedPattern ^ 1]);

		if (parseResult.mark != 0)
			equipmentSlot = EquipmentSlot.values()[parseResult.mark - 1];
		return true;
	}

	@Override
	@SuppressWarnings("null")
	protected Number[] get(Event event, ItemType[] items) {
		Attribute attribute = attributes.getSingle(event);
		if (attribute == null)
			return new Number[0];

		List<Number> values = new ArrayList<>();
		for (ItemType item : items) {
			// It's complicated...
			// Interesting link: https://minecraft.fandom.com/wiki/Attribute#Operations
			double base = 0;
			double additiveMultiplier = 1;
			double multiplier = 1;

			Collection<AttributeModifier> modifiers = item.getItemMeta().getAttributeModifiers(attribute);
			if (modifiers == null)
				continue;

			for (AttributeModifier modifier : modifiers) {
				if (equipmentSlot != null && modifier.getSlot() != equipmentSlot)
					continue;

				switch (modifier.getOperation()) {
					case ADD_NUMBER:
						base += modifier.getAmount();
						break;
					case ADD_SCALAR:
						additiveMultiplier += modifier.getAmount();
						break;
					case MULTIPLY_SCALAR_1:
						multiplier *= modifier.getAmount() + 1;
						break;
					default:
						assert false;
				}
			}
			values.add(base * additiveMultiplier * multiplier);
		}
		return values.toArray(new Number[0]);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL)
			return null;
		return CollectionUtils.array(Number.class);
	}

	@Override
	@SuppressWarnings("null")
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		Attribute attribute = attributes.getSingle(event);
		if (attribute == null)
			return;

		double value = delta == null ? 0 : ((Number) delta[0]).doubleValue();
		for (ItemType item : getExpr().getArray(event)) {
			ItemMeta meta = item.getItemMeta();

			switch (mode) {
				case DELETE:
				case RESET:
					// We first delete all other attributes for that slot,
					// then just add an attribute value.
					Multimap<Attribute, AttributeModifier> modifierMap = meta.getAttributeModifiers();
					if (modifierMap != null) {
						Collection<AttributeModifier> toRemain = modifierMap.get(attribute);
						toRemain.removeIf(mod -> equipmentSlot == null || mod.getSlot() == null || mod.getSlot() == equipmentSlot);
						modifierMap.replaceValues(attribute, toRemain);
					}
					meta.setAttributeModifiers(modifierMap);
				case SET:
					meta.addAttributeModifier(attribute, new AttributeModifier(ATTRIBUTE_NAME, value, Operation.ADD_NUMBER));
					break;
				case REMOVE:
					value = -value;
				case ADD:
					meta.addAttributeModifier(attribute, new AttributeModifier(ATTRIBUTE_NAME, value, Operation.ADD_NUMBER));
					break;
				default:
					return;
			}
			item.setItemMeta(meta);
		}
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return getExpr().toString(event, debug) + "'s " + attributes.toString(event, debug) + " attribute";
	}

}
