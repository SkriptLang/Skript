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

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Name("Attribute Modifier")
@Description({
	"Certain items can have attribute modifiers, which are used to modify the attributes of the item.",
	"An attribute modifier has a name, a value, and optionally an operation, and an equipment slot.",
	"An operation can be either additively scaling, multiplicatively scaling, or a plain addition.",
	"See <a href=\"https://minecraft.fandom.com/wiki/Attribute#Operations\">the Minecraft wiki</a> for more information.",
	"The equipment slot is the slot in which the item must be equipped in order for the modifier to work.",
	"If not specified, the modifier will work in any slot."
})
@Examples({
	"set {_modifier} to a new attribute modifier named \"Damage\" with value 5 and for the main hand slot",
	"set {_item} to a diamond sword",
	"add {_modifier} to the attack damage attribute modifiers of {_item}",
	"set {_modifier2} to a new multiplicatively scaling attribute modifier named \"Speed\" with value 0.25 and for the boots slot # Gives a 25% speed boost, works multiplicatively with other modifiers",
})
@Since("INSERT VERSION")
public class ExprAttributeModifier extends SimpleExpression<AttributeModifier> {

	static {
		Skript.registerExpression(
			ExprAttributeModifier.class,
			AttributeModifier.class,
			ExpressionType.COMBINED,
			"[a] [new] [(plain|add:additive[[ly] scaling]|mult:multiplicative[[ly] scaling])] attribute [modifier] (named|with name[s]) %string% (and|with) value %number% [and] [(in|of|for) [the] (1:main[ ]hand|2:off[ ]hand|3:boot[s]|3:shoe[s]|4:leg[ging][s]|5:chestplate[s]|6:helm[et][s]) [slot]]"
		);
	}

	// Starting from 1.21, the AttributeModifier uses a NamespacedKey instead of a UUID
	// Since the API is marked as experimental, this is not implemented yet.
	private static final boolean USE_NAMESPACED_KEY = Skript.isRunningMinecraft(1, 21);

	private AttributeModifier.Operation operation;
	private Expression<String> name;
	private Expression<Number> value;
	@Nullable
	private EquipmentSlot equipmentSlot;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		operation = parseResult.hasTag("add")
			? Operation.ADD_SCALAR : parseResult.hasTag("mult")
			? Operation.MULTIPLY_SCALAR_1 : Operation.ADD_NUMBER;
		name = (Expression<String>) exprs[0];
		value = (Expression<Number>) exprs[1];

		if (parseResult.mark != 0)
			equipmentSlot = EquipmentSlot.values()[parseResult.mark - 1];
		return true;
	}

	@Override
	protected @Nullable AttributeModifier[] get(Event event) {
		String name = this.name.getSingle(event);
		Number value = this.value.getSingle(event);
		if (name == null || value == null)
			return null;

		return new AttributeModifier[] { new AttributeModifier(UUID.randomUUID(), name, value.doubleValue(), operation, equipmentSlot) };
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends AttributeModifier> getReturnType() {
		return AttributeModifier.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return null;
	}

}
