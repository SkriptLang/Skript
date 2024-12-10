package ch.njol.skript.util;

/**
 * Simple utility methods based around {@link Class Classes}
 */
public class ClassUtils {

	/**
	 * Get a class from a string
	 * <p>
	 * Will throw a runtime exception if class does not exist
	 *
	 * @param className Fully qualified string of class
	 * @return Class if available
	 */
	public static Class<?> getClass(final String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
