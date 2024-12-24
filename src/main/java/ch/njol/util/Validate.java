package ch.njol.util;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

/**
 * @author Peter Güttinger
 */
public abstract class Validate {
	
	public static void notNull(final Object... objects) {
		for (int i = 0; i < objects.length; i++) {
			if (objects[i] == null)
				throw new IllegalArgumentException("the " + StringUtils.fancyOrderNumber(i + 1) + " parameter must not be null");
		}
	}
	
	public static void notNull(final @Nullable Object object, final String name) {
		if (object == null)
			throw new IllegalArgumentException(name + " must not be null");
	}
	
	public static void isTrue(final boolean b, final String error) {
		if (!b)
			throw new IllegalArgumentException(error);
	}
	
	public static void isFalse(final boolean b, final String error) {
		if (b)
			throw new IllegalArgumentException(error);
	}
	
	public static void notNullOrEmpty(final @Nullable String s, final String name) {
		if (s == null || s.isEmpty())
			throw new IllegalArgumentException(name + " must neither be null nor empty");
	}
	
	public static void notNullOrEmpty(final @Nullable Object[] array, final String name) {
		if (array == null || array.length == 0)
			throw new IllegalArgumentException(name + " must neither be null nor empty");
	}
	
	public static void notNullOrEmpty(final @Nullable Collection<?> collection, final String name) {
		if (collection == null || collection.isEmpty())
			throw new IllegalArgumentException(name + " must neither be null nor empty");
	}
	
	public static void notEmpty(final @Nullable String s, final String name) {
		if (s != null && s.isEmpty())
			throw new IllegalArgumentException(name + " must not be empty");
	}
	
	public static void notEmpty(final Object[] array, final String name) {
		if (array.length == 0)
			throw new IllegalArgumentException(name + " must not be empty");
	}
	
	public static void notEmpty(final int[] nums, final String name) {
		if (nums.length == 0)
			throw new IllegalArgumentException(name + " must not be empty");
	}
	
}
