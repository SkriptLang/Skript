package org.skriptlang.skript.lang.properties;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A base interface for syntaxes dealing with properties to extend and use for common utilities.
 * @param <Handler> The property handler used for the applicable property.
 */
@ApiStatus.Experimental
public interface PropertyBaseSyntax<Handler extends PropertyHandler<?>> {

	/**
	 * Produces a standard error message for use when an expression returns types that do not have the
	 * correct property.
	 * @param expr The expression that has bad types.
	 * @return An error message.
	 */
	default @Nullable String getBadTypesErrorMessage(@NotNull Expression<?> expr) {
		expr = LiteralUtils.defendExpression(expr);
		List<ClassInfo<?>> invalidTypes = new ArrayList<>();
		for (Class<?> type : expr.possibleReturnTypes()) {
			ClassInfo<?> info = Classes.getSuperClassInfo(type);
			if (info.hasProperty(getProperty()))
				continue;
			invalidTypes.add(info);
		}
		return "The expression '" + expr + "' returns the following types that do not have the "
			+ getPropertyName() + " property: "
			+ Classes.toString(invalidTypes.toArray(), true);
	}

	/**
	 * Gets the property this expression represents.
	 * This is used to find the appropriate handlers for the expression's input types.
	 *
	 * @return The property this expression represents.
	 */
	@NotNull Property<Handler> getProperty();

	/**
	 * Returns the name of the property for use in toString, e.g. "name", "display name", etc.
	 * Defaults to the {@link #getProperty()}'s name, but can be overridden for custom names.
	 * @return The name of the property to use.
	 */
	default String getPropertyName() {
		return getProperty().name();
	}

	/*
	UTILITIES
	*/

	/**
	 * Converts the given expression to an expression that returns types that have the given property.
	 * This is useful for ensuring that an expression can be used with a property.
	 *
	 * @param property the property to check for
	 * @param expr the expression to convert
	 * @return an expression that returns types that have the property, or null if no such expression can be created
	 */
	static @Nullable Expression<?> asProperty(Property<?> property, Expression<?> expr) {
		if (expr == null) {
			return null; // no expression to convert
		}

		// get all types with a name property
		Set<ClassInfo<?>> classInfos = Classes.getClassInfosByProperty(property);
		Class<?>[] classes = classInfos.stream().map(ClassInfo::getC).toArray(Class[]::new);

		if (classes.length == 0)
			return null;

		//noinspection unchecked,rawtypes
		return LiteralUtils.defendExpression(expr).getConvertedExpression((Class[]) classes);
	}

	/**
	 * Gets a map of all possible property infos for the given expression's return types.
	 * This is useful for determining which property handlers can be used with an expression.
	 *
	 * @param property the property to check for
	 * @param expr the expression to check
	 * @param <Handler> the type of the property handler
	 * @return a map of classes to property infos for the given expression's return types
	 */
	static <Handler extends PropertyHandler<?>> PropertyMap<Handler> getPossiblePropertyInfos(
		Property<Handler> property,
		Expression<?> expr
	) {
		PropertyMap<Handler> propertyInfos = new PropertyMap<>();

		// get all types with a name property
		Set<ClassInfo<?>> classInfos = Classes.getClassInfosByProperty(property);

		// for each return type, match to a classinfo w/ name property
		for (Class<?> returnType : expr.possibleReturnTypes()) {
			ClassInfo<?> closestInfo = null;
			for (ClassInfo<?> propertiedClassInfo : classInfos) {
				if (propertiedClassInfo.getC() == returnType) {
					// exact match, use it
					closestInfo = propertiedClassInfo;
					break;
				}
				if (propertiedClassInfo.getC().isAssignableFrom(returnType)) {
					// closest match so far
					if (closestInfo == null || closestInfo.getC().isAssignableFrom(propertiedClassInfo.getC())) {
						closestInfo = propertiedClassInfo;
					}
				}
			}
			if (closestInfo == null) {
				continue; // no name property
			}

			// get property
			var propertyInfo = closestInfo.getPropertyInfo(property);
			if (propertyInfo != null) {
				var clonedHandler = propertyInfo.handler().newInstance();
				if (clonedHandler.init(expr, expr.getParser())) {
					// overwrite with cloned handler
					//noinspection unchecked
					propertyInfo = new Property.PropertyInfo<>(propertyInfo.property(), (Handler) clonedHandler);
				} else {
					propertyInfo = null; // failed to init, invalid property
				}
			}
			ClassInfo<?> classInfo = Classes.getSuperClassInfo(returnType);
			propertyInfos.put(classInfo.getC(), propertyInfo);
			propertyInfos.put(closestInfo.getC(), propertyInfo);
		}
		return propertyInfos;
	}

}
