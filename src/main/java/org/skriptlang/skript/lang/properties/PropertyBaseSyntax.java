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
	 * @deprecated use {@link #getPossiblePropertyInfos(Property, Expression, Expression)}, which lets the calling
	 *             syntax pass itself as the parent expression.
	 */
	@Deprecated(forRemoval = true)
	static <Handler extends PropertyHandler<?>> PropertyMap<Handler> getPossiblePropertyInfos(
		Property<Handler> property,
		Expression<?> expr
	) {
		return getPossiblePropertyInfos(property, expr, expr);
	}

	/**
	 * Gets a map of all possible property infos for the given expression's return types.
	 * This is useful for determining which property handlers can be used with an expression.
	 *
	 * @param property the property to check for
	 * @param expr the expression to check
	 * @param parentExpression the expression the handlers will be used by, handed to
	 *                         {@link PropertyHandler#init(Expression, ch.njol.skript.lang.parser.ParserInstance)}.
	 * @param <Handler> the type of the property handler
	 * @return a map of classes to property infos for the given expression's return types
	 */
	static <Handler extends PropertyHandler<?>> PropertyMap<Handler> getPossiblePropertyInfos(
		Property<Handler> property,
		Expression<?> expr,
		Expression<?> parentExpression
	) {
		PropertyMap<Handler> propertyInfos = new PropertyMap<>();

		// get all types with a name property
		Set<ClassInfo<?>> classInfos = Classes.getClassInfosByProperty(property);

		// for each return type, match to a classinfo w/ name property
		for (Class<?> returnType : expr.possibleReturnTypes()) {
			ClassInfo<?> closestInfo = null;
			List<ClassInfo<?>> subtypeInfos = new ArrayList<>();
			for (ClassInfo<?> propertiedClassInfo : classInfos) {
				Class<?> propertiedClass = propertiedClassInfo.getC();
				if (propertiedClass == returnType) {
					// exact match, use it
					closestInfo = propertiedClassInfo;
				} else if (propertiedClass.isAssignableFrom(returnType)) {
					// a supertype of the declared type: closest match so far
					if (closestInfo == null || closestInfo.getC().isAssignableFrom(propertiedClass)) {
						closestInfo = propertiedClassInfo;
					}
				} else if (returnType.isAssignableFrom(propertiedClass)) {
					// a subtype of the declared type: the value may be one of these at runtime, e.g. a display for
					// an expression only known to return entities. PropertyMap picks the most specific one then.
					subtypeInfos.add(propertiedClassInfo);
				}
			}
			if (closestInfo == null && subtypeInfos.isEmpty()) {
				continue; // no name property
			}

			// get property
			if (closestInfo != null) {
				var propertyInfo = PropertyBaseSyntax.<Handler>resolvePropertyInfo(property, closestInfo, expr, parentExpression);
				ClassInfo<?> classInfo = Classes.getSuperClassInfo(returnType);
				propertyInfos.put(classInfo.getC(), propertyInfo);
				propertyInfos.put(closestInfo.getC(), propertyInfo);
			}
			for (ClassInfo<?> subtypeInfo : subtypeInfos) {
				propertyInfos.put(subtypeInfo.getC(),
					PropertyBaseSyntax.<Handler>resolvePropertyInfo(property, subtypeInfo, expr, parentExpression));
			}
		}
		return propertyInfos;
	}

	/**
	 * Looks up the property info a class info holds for the given property, and replaces its handler with a freshly
	 * initialised instance for this usage.
	 *
	 * @return the property info with its own handler instance, or null if the handler refused to initialise
	 */
	private static <Handler extends PropertyHandler<?>> Property.@Nullable PropertyInfo<Handler> resolvePropertyInfo(
		Property<Handler> property,
		ClassInfo<?> classInfo,
		Expression<?> expr,
		Expression<?> parentExpression
	) {
		Property.PropertyInfo<Handler> propertyInfo = classInfo.getPropertyInfo(property);
		if (propertyInfo == null) {
			return null;
		}
		var clonedHandler = propertyInfo.handler().newInstance();
		if (!clonedHandler.init(parentExpression, expr.getParser())) {
			return null; // failed to init, invalid property
		}
		//noinspection unchecked
		return new Property.PropertyInfo<>(propertyInfo.property(), (Handler) clonedHandler);
	}

}
