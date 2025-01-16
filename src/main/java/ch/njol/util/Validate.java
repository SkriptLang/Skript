package ch.njol.util;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * use {@link com.google.common.base.Preconditions}
 */
@Deprecated(forRemoval = true)
public final class Validate {

	private Validate() {}

	public static void notNull(Object... objects) {
		for (int i = 0; i < objects.length; i++) {
			if (objects[i] == null)
				throw new IllegalArgumentException("the " + StringUtils.fancyOrderNumber(i + 1) + " parameter must not be null");
		}
	}

	public static void notNull(@Nullable Object object, String name) {
		if (object == null)
			throw new IllegalArgumentException(name + " must not be null");
	}

	public static void isTrue(boolean value, String error) {
		if (!value)
			throw new IllegalArgumentException(error);
	}

	public static void isFalse(boolean value, String error) {
		if (value)
			throw new IllegalArgumentException(error);
	}

	public static void notNullOrEmpty(@Nullable String value, final String name) {
		if (value == null || value.isEmpty())
			throw new IllegalArgumentException(name + " must neither be null nor empty");
	}

	public static void notNullOrEmpty(Object @Nullable [] array, String name) {
		if (array == null || array.length == 0)
			throw new IllegalArgumentException(name + " must neither be null nor empty");
	}

	public static void notNullOrEmpty(@Nullable Collection<?> collection, String name) {
		if (collection == null || collection.isEmpty())
			throw new IllegalArgumentException(name + " must neither be null nor empty");
	}

	public static void notEmpty(@Nullable String value, String name) {
		if (value != null && value.isEmpty())
			throw new IllegalArgumentException(name + " must not be empty");
	}

	public static void notEmpty(Object[] array, String name) {
		if (array.length == 0)
			throw new IllegalArgumentException(name + " must not be empty");
	}

	public static void notEmpty(int[] array, String name) {
		if (array.length == 0)
			throw new IllegalArgumentException(name + " must not be empty");
	}

}
