package org.skriptlang.skript.lang.properties;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property.PropertyInfo;

import java.util.HashMap;
import java.util.List;

/**
 * Utilities for dealing with {@link Property}s and {@link PropertyHandler}s.
 */
public class PropertyUtils {

	/**
	 * A map of property handlers for different classes. Useful for storing which classes have which property handlers.
	 * Caches the closest matching class for faster lookup.
	 *
	 * @param <Handler> The type of PropertyHandler.
	 */
	public static class PropertyMap<Handler extends PropertyHandler<?>> extends HashMap<Class<?>, PropertyInfo<Handler>> {

		/**
		 * Get the appropriate handler for the given input class.
		 * This method will find the closest matching class in the map that is assignable from the actual class.
		 * The result is cached for faster lookup next time.
		 *
		 * @param inputClass the class to get the handler for
		 * @return the handler for the given class, or null if no handler is found
		 */
		public @Nullable Handler getHandler(Class<?> inputClass) {
			PropertyInfo<Handler> propertyInfo;
			// check if we don't already know the right info for this class
			propertyInfo = get(inputClass);
			if (propertyInfo == null) {
				// no property info found, return null
				return null;
			}
			// get the name using the property handler
			return propertyInfo.handler();
		}

		/**
		 * Get the property info for the given actual class.
		 * This method will find the closest matching class in the map that is assignable from the actual class.
		 * The result is cached for faster lookup next time.
		 *
		 * @param actualClass The actual class to get the property info for.
		 * @return The property info for the given class, or null if no property info is found.
		 */
		public PropertyInfo<Handler> get(Class<?> actualClass) {
			if (super.containsKey(actualClass)) {
				return super.get(actualClass);
			}

			Class<?> closestClass = null;
			for (Class<?> candidateClass : keySet()) {
				// need to make sure we get the closest match
				if (candidateClass.isAssignableFrom(actualClass)) {
					if (closestClass == null || closestClass.isAssignableFrom(candidateClass)) {
						closestClass = candidateClass;
					}
				}
			}

			var propertyInfo = super.get(closestClass);
			// add to properties so we don't have to search again
			put(actualClass, propertyInfo);
			return propertyInfo;
		}

	}

	/**
	 * Converts the given expression to an expression that returns types that have the given property.
	 * This is useful for ensuring that an expression can be used with a property.
	 *
	 * @param property the property to check for
	 * @param expr the expression to convert
	 * @return an expression that returns types that have the property, or null if no such expression can be created
	 */
	public static @Nullable Expression<?> asProperty(Property<?> property, Expression<?> expr) {
		if (expr == null) {
			return null; // no expression to convert
		}

		// get all types with a name property
		List<ClassInfo<?>> classInfos = Classes.getClassInfosByProperty(property);
		Class<?>[] classes = classInfos.stream().map(ClassInfo::getC).toArray(Class[]::new);

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
	public static <Handler extends PropertyHandler<?>> PropertyMap<Handler> getPossiblePropertyInfos(
		Property<Handler> property,
		Expression<?> expr
	) {
		PropertyMap<Handler> propertyInfos = new PropertyMap<>();

		// get all types with a name property
		List<ClassInfo<?>> classInfos = Classes.getClassInfosByProperty(property);

		// for each return type, match to a classinfo w/ name property
		for (Class<?> returnType : expr.possibleReturnTypes()) {
			ClassInfo<?> closestInfo = null;
			for (ClassInfo<?> propertiedClassInfo  : classInfos) {
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
					propertyInfo = new PropertyInfo<>(propertyInfo.property(), (Handler) clonedHandler);
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
