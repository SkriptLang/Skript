package org.skriptlang.skript.lang.properties;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.PropertyHandler.ConditionPropertyHandler;
import org.skriptlang.skript.lang.properties.PropertyUtils.PropertyMap;

public abstract class PropertyBaseCondition<Handler extends ConditionPropertyHandler<?>> extends Condition
	implements PropertyBaseSyntax<Handler>{

	protected Expression<?> propertyHolder;
	private PropertyMap<Handler> properties;
	private final Property<Handler> property = getProperty();

	public static void register(Class<? extends Condition> condition, String property, String type) {
		PropertyCondition.register(condition, property, type);
	}

	public static void register(Class<? extends Condition> condition, PropertyType propertyType, String property, String type) {
		PropertyCondition.register(condition, propertyType, property, type);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.propertyHolder = PropertyBaseSyntax.asProperty(property, expressions[0]);
		if (propertyHolder == null) {
			Skript.error("The expression " + expressions[0] + " returns types that do not have the " + getPropertyName() + " property."); // todo: improve error message (which types?)
			return false;
		}

		// get all possible property infos for the expression's return types
		properties = PropertyBaseSyntax.getPossiblePropertyInfos(property, propertyHolder);
		if (properties.isEmpty()) {
			Skript.error("The expression " + propertyHolder + " returns types that do not have the " + getPropertyName() + " property.");
			return false; // no name property found
		}
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return propertyHolder.check(event, (element) -> {
				// determine handler
				//noinspection unchecked
				var handler = (ConditionPropertyHandler<Object>) properties.getHandler(element.getClass());
				if (handler == null)
					return false;
				return handler.check(element);
			}, isNegated());
	}

	protected PropertyType getPropertyType() {
		return PropertyType.BE;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return PropertyCondition.toString(this, getPropertyType(), event, debug, propertyHolder, getPropertyName());
	}

}
