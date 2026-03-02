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

	/**
	 * Calculates the distance between two classes in the inheritance hierarchy.
	 * @param a The superclass.
	 * @param b The subclass.
	 * @return The distance between class <code>a</code> and class <code>b</code>.
	 * If <code>a</code> is not a superclass of <code>b</code>, returns -1.
	 * If <code>a</code> equals <code>b</code>, returns 0.
	 */
	public static int hierarchyDistance(Class<?> a, Class<?> b) {
		if (!a.isAssignableFrom(b))
			return -1;
		if (a.equals(b))
			return 0;
		int distance = 0;
		Class<?> current = b;
		while (current != null && !a.equals(current)) {
			current = current.getSuperclass();
			distance++;
		}
		return distance;
	}

	/**
	 * Calculates the distance between two classes in the inheritance hierarchy, in either direction.
	 * @param a The first class.
	 * @param b The second class.
	 * @return The distance between class <code>a</code> and class <code>b</code>.
	 * If neither class is a superclass of the other, returns -1.
	 * If the classes are equal, returns 0.
	 */
	public static int hierarchyDistanceBetween(Class<?> a, Class<?> b) {
		int dist = hierarchyDistance(a, b);
		return dist != -1 ? dist : hierarchyDistance(b, a);
	}

	/**
	 * Checks if two classes are related in the inheritance hierarchy.
	 * @param a The first class.
	 * @param b The second class.
	 * @return True if either class is a superclass of the other.
	 */
	public static boolean isRelatedTo(Class<?> a, Class<?> b) {
		return a.isAssignableFrom(b) || b.isAssignableFrom(a);
	}

}
