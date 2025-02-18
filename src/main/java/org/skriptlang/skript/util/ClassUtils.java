package org.skriptlang.skript.util;

import java.lang.reflect.Modifier;

/**
 * Utilities for interacting with classes.
 */
public final class ClassUtils {

	/**
	 * @param clazz The class to check.
	 * @return True if <code>clazz</code> does not represent an annotation, array, primitive, interface, or abstract class.
	 */
	public static boolean isNormalClass(Class<?> clazz) {
		return !clazz.isAnnotation() && !clazz.isArray() && !clazz.isPrimitive()
				&& !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers());
	}

}
