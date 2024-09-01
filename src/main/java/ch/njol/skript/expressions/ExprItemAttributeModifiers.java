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
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Name("Item Attribute Modifiers")
@Description({
	"The attribute modifiers of a given attribute type of an item.",
	"Returns nothing if the item has no attribute modifiers for the specified attribute type.",
	"One can add, remove, set, or reset the attribute modifiers of an item."
})
@Examples({
	"set {_modifier} to a new attribute modifier named \"Damage\" with value 5 and for the main hand slot",
	"set {_item} to a diamond sword",
	"add {_modifier} to the attack damage attribute modifiers of {_item}",
})
@Since("INSERT VERSION")
public class ExprItemAttributeModifiers extends PropertyExpression<ItemType, AttributeModifier> {

	static {
		register(
			ExprItemAttributeModifiers.class,
			AttributeModifier.class,
			"%attributetype% attribute[s] [modifier[s]]",
			"itemtypes"
		);
	}

	private Expression<Attribute> attributeType;

	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		attributeType = (Expression<Attribute>) exprs[matchedPattern];
		setExpr((Expression<ItemType>) exprs[matchedPattern ^ 1]);
		return true;
	}

	@Override
	protected AttributeModifier[] get(Event event, ItemType[] items) {
		Attribute attribute = attributeType.getSingle(event);
		if (attribute == null)
			return new AttributeModifier[0];

		return Arrays.stream(items)
			.map(item -> item.getItemMeta().getAttributeModifiers(attribute))
			.filter(Objects::nonNull)
			.flatMap(Collection::stream)
			.toArray(AttributeModifier[]::new);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL)
			return null;
		return CollectionUtils.array(AttributeModifier[].class);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		AttributeModifier[] modifiers = (AttributeModifier[]) delta;
		Attribute attribute = attributeType.getSingle(event);
		if (attribute == null)
			return;

		for (ItemType item : getExpr().getArray(event)) {
			ItemMeta meta = item.getItemMeta();
			switch (mode) {
				case ADD:
					for (AttributeModifier modifier : modifiers) {
						meta.addAttributeModifier(attribute, modifier);
					}
					break;
				case REMOVE:
					removeModifiers(meta, attribute, modifiers);
					break;
				case SET:
					setModifiers(meta, attribute, modifiers);
					break;
				case DELETE:
				case RESET:
					// Currently, delete and reset has the same outcome
					if (meta.getAttributeModifiers() == null)
						break;
					removeModifiers(meta, attribute, meta.getAttributeModifiers(attribute).toArray(new AttributeModifier[0]));
					break;
				default:
					assert false;
			}
			item.setItemMeta(meta);
		}
	}

	@Override
	public Class<? extends AttributeModifier> getReturnType() {
		return AttributeModifier.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return getExpr().toString(event, debug) + "'s " + attributeType.toString(event, debug) + " attribute modifiers";
	}

	private static void removeModifiers(ItemMeta meta, Attribute attribute, AttributeModifier[] modifiers) {
		// If no modifiers are present, nothing needs to be done
		if (meta.getAttributeModifiers() == null)
			return;
		// A modifiable copy of the attribute modifiers needs to be created
		Multimap<Attribute, AttributeModifier> modifierMap = ArrayListMultimap.create(meta.getAttributeModifiers());

		AttributeModifier[] remaining = modifierMap.get(attribute).stream()
			.filter(m -> !CollectionUtils.contains(modifiers, m))
			.toArray(AttributeModifier[]::new);
		setModifiers(meta, attribute, remaining);
	}

	private static void setModifiers(ItemMeta meta, Attribute attribute, AttributeModifier[] modifiers) {
		if (meta.getAttributeModifiers() == null) {
			// Since no modifiers are present, the attribute modifiers can be added directly
			for (AttributeModifier modifier : modifiers) {
				meta.addAttributeModifier(attribute, modifier);
			}
		} else {
			// A modifiable copy of the attribute modifiers needs to be created
			Multimap<Attribute, AttributeModifier> modifierMap = ArrayListMultimap.create(meta.getAttributeModifiers());
			modifierMap.replaceValues(attribute, Arrays.asList(modifiers));
			meta.setAttributeModifiers(modifierMap);
		}
	}
}
