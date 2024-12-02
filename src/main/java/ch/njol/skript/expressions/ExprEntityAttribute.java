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
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.stream.Stream;

@Name("Entity Attribute")
@Description({
	"The numerical value of an entity's particular attribute.",
	"Note that the movement speed attribute cannot be reliably used for players. For that purpose, use the speed expression instead.",
	"Resetting an entity's attribute is only available in Minecraft 1.11 and above."
})
@Examples({
	"on damage of player:",
		"\tsend \"You are wounded!\" to victim",
		"\tset victim's attack speed attribute to 2"
})
@Since("2.5, 2.6.1 (final attribute value)")
public class ExprEntityAttribute extends PropertyExpression<Entity, Number> {
	
	static {
		Skript.registerExpression(ExprEntityAttribute.class, Number.class, ExpressionType.COMBINED,
				"[the] %attributetype% [(1:(total|final|modified))] attribute [value] of %entities%",
				"%entities%'[s] %attributetype% [(1:(total|final|modified))] attribute [value]");
	}

	@Nullable
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
		return Stream.of(entities)
		    .map(ent -> getAttribute(ent, attribute))
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
		double deltaValue = delta == null ? 0 : ((Number) delta[0]).doubleValue();
		for (Entity entity : getExpr().getArray(event)) {
			AttributeInstance instance = getAttribute(entity, attribute);
			if (instance != null) {
				switch(mode) {
					case ADD:
						instance.setBaseValue(instance.getBaseValue() + deltaValue);
						break;
					case SET:
						instance.setBaseValue(deltaValue);
						break;
					case DELETE:
						instance.setBaseValue(0);
						break;
					case RESET:
						instance.setBaseValue(instance.getDefaultValue());
						break;
					case REMOVE:
						instance.setBaseValue(instance.getBaseValue() - deltaValue);
						break;
					case REMOVE_ALL:
						assert false;
				}
			}
		}
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	@SuppressWarnings("null")
	public String toString(@Nullable Event event, boolean debug) {
		return "entity " + getExpr().toString(event, debug) + "'s " + (attributes == null ? "" : attributes.toString(event, debug)) + "attribute";
	}
	
	@Nullable
	private static AttributeInstance getAttribute(Entity entity, @Nullable Attribute attribute) {
	    if (attribute != null && entity instanceof Attributable) {
	        return ((Attributable) entity).getAttribute(attribute);
	    }
	   return null;
	}

}
