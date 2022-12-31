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

import ch.njol.skript.expressions.base.PropertyExpression;
import org.bukkit.attribute.Attribute;

import java.util.Arrays;
import java.util.Objects;

import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Entity Attribute")
@Description({
		"The numerical value of an entity's particular attribute.",
		"Note that the movement speed attribute cannot be reliably used for players. For that purpose, use the speed expression instead.",
		"Resetting an entity's attribute is only available in Minecraft 1.11 and above."
})
@Examples({
		"on damage of player:",
		"\tsend \"You are wounded!\"",
		"\tset victim's attack speed attribute to 2"
})
@Since("2.5, 2.6.1 (final attribute value)")
public class ExprEntityAttribute extends PropertyExpression<Entity, Number> {
	
	static {
		register(ExprEntityAttribute.class, Number.class, "[(base|1:total|1:final|1:modified)] attribute [value]", "entities");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Attribute> attributes;
	private boolean withModifiers;

	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		attributes = (Expression<Attribute>) exprs[matchedPattern];
		setExpr((Expression<? extends Entity>) exprs[matchedPattern ^ 1]);
		withModifiers = parseResult.mark == 1;
		return true;
	}

	@Override
	@SuppressWarnings("null")
	protected Number[] get(Event event, Entity[] entities) {
		Attribute attribute = attributes.getSingle(event);
		if (attribute == null)
			return new Number[0];

		return Arrays.stream(entities)
				.filter(ent -> ent instanceof Attributable)
				.map(ent -> ((Attributable) ent).getAttribute(attribute))
				.filter(Objects::nonNull)
				.map(att -> withModifiers ? att.getValue() : att.getBaseValue())
				.toArray(Number[]::new);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL || withModifiers)
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
		for (Entity entity : getExpr().getArray(event)) {
			if (!(entity instanceof Attributable))
				continue;

			AttributeInstance instance = ((Attributable) entity).getAttribute(attribute);
			if (instance == null)
				continue;

			switch (mode) {
				case SET:
					instance.setBaseValue(value);
					break;
				case REMOVE:
					value = -value;
				case ADD:
					instance.setBaseValue(instance.getBaseValue() + value);
					break;
				case DELETE:
					instance.setBaseValue(0);
					break;
				case RESET:
					instance.setBaseValue(instance.getDefaultValue());
					break;
				default:
					return;
			}
		}
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	@SuppressWarnings("null")
	public String toString(@Nullable Event e, boolean debug) {
		return "entity " + getExpr().toString(e, debug) + "'s " + attributes.toString(e, debug) + (withModifiers ? " modified" : "") + " attribute";
	}

}
