package org.skriptlang.skript.lang.properties;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;

import java.util.HashMap;

/**
 * A map of property handlers for different classes. Useful for storing which classes have which property handlers.
 * Caches the closest matching class for faster lookup.
 *
 * @param <Handler> The type of PropertyHandler.
 */
@ApiStatus.Experimental
public class PropertyMap<Handler extends PropertyHandler<?>> extends HashMap<Class<?>, Property.PropertyInfo<Handler>> {

	/**
	 * Get the appropriate handler for the given input class.
	 * This method will find the closest matching class in the map that is assignable from the actual class.
	 * The result is cached for faster lookup next time.
	 *
	 * @param inputClass the class to get the handler for
	 * @return the handler for the given class, or null if no handler is found
	 */
	public @Nullable Handler getHandler(Class<?> inputClass) {
		Property.PropertyInfo<Handler> propertyInfo;
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
	public Property.PropertyInfo<Handler> get(Class<?> actualClass) {
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
