package org.skriptlang.skript.util;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for reflection.
 */
public class ReflectUtils {

	/**
	 * Cache for classes.
	 */
	private static final Map<String, Class<?>> CLASSES = new HashMap<>();

	/**
	 * Cache for methods of classes.
	 */
	private static final MethodMap METHODS = new MethodMap();

	/**
	 * Cache for fields of classes.
	 */
	private static final FieldMap FIELDS = new FieldMap();

	/**
	 * @param className The full package and class name.
	 * @return Whether the class exists.
	 */
	public static boolean classExists(String className) {
		return getClass(className) != null;
	}

	/**
	 * @param className The full package and class name.
	 * @return The resulting {@link Class} if found, otherwise {@code null}.
	 */
	public static @Nullable Class<?> getClass(String className) {
		if (CLASSES.containsKey(className))
			return CLASSES.get(className);
		Class<?> c = null;
		try {
			c = Class.forName(className);
		} catch (ClassNotFoundException ignored) {}
		CLASSES.put(className, c);
		return c;
	}

	/**
	 * @param className The full package and class name.
	 * @param methodName The name of the method.
	 * @param params The {@link Class}es used as parameters for the desired method.
	 * @return Whether the method exists.
	 */
	public static boolean methodExists(String className, String methodName, Class<?> @Nullable ... params) {
		Class<?> c = getClass(className);
		if (c == null)
			return false;
		return methodExists(c, methodName, params);
	}

	/**
	 * @param c The {@link Class} to check the method for.
	 * @param methodName The name of the method.
	 * @param params The {@link Class}es used as parameters for the desired method.
	 * @return Whether the method exists.
	 */
	public static boolean methodExists(Class<?> c, String methodName, Class<?> @Nullable ... params) {
		return getMethod(c, methodName, params) != null;
	}

	/**
	 * @param className The full package and class name.
	 * @param methodName The name of the method.
	 * @param params The {@link Class}es used as parameters for the desired method.
	 * @return The resulting {@link Method} if it exists, otherwise {@code null}.
	 */
	public static @Nullable Method getMethod(String className, String methodName, Class<?> @Nullable ... params) {
		Class<?> c = getClass(className);
		if (c == null)
			return null;
		return getMethod(c, methodName, params);
	}

	/**
	 * @param c The {@link Class} to get the method from.
	 * @param methodName The name of the method.
	 * @param params The {@link Class}es used as parameters for the desired method.
	 * @return The resulting {@link Method} if it exists, otherwise {@code null}.
	 */
	public static @Nullable Method getMethod(Class<?> c, String methodName, Class<?> @Nullable ... params) {
		MethodID methodID = new MethodID(c, methodName, params);
		if (METHODS.contains(c, methodID))
			return METHODS.get(c, methodID);
		Method method = null;
		try {
			method = c.getDeclaredMethod(methodName, params);
		} catch (NoSuchMethodException ignored) {}

		METHODS.put(c, methodID, method);
		return method;
	}

	/**
	 * @param className The full package and class name.
	 * @param fieldName The name of the field.
	 * @return Whether the field exists.
	 */
	public static boolean fieldExists(String className, String fieldName) {
		Class<?> c = getClass(className);
		if (c == null)
			return false;
		return fieldExists(c, fieldName);
	}

	/**
	 * @param c The {@link Class} to check the field for.
	 * @param fieldName The name of the field.
	 * @return Whether the field exists.
	 */
	public static boolean fieldExists(Class<?> c, String fieldName) {
		return getField(c, fieldName) != null;
	}

	/**
	 * @param className The full package and class name.
	 * @param fieldName The name of the field.
	 * @return The resulting {@link Field} if it exists, otherwise {@code null}.
	 */
	public static @Nullable Field getField(String className, String fieldName) {
		Class<?> c = getClass(className);
		if (c == null)
			return null;
		return getField(c, fieldName);
	}

	/**
	 * @param c The {@link Class} to get the field from.
	 * @param fieldName The name of the field.
	 * @return The resulting {@link Field} if it exists, otherwise {@code null}.
	 */
	public static @Nullable Field getField(Class<?> c, String fieldName) {
		if (FIELDS.contains(c, fieldName))
			return FIELDS.get(c, fieldName);
		Field field = null;
		try {
			field = c.getDeclaredField(fieldName);
		} catch (NoSuchFieldException ignored) {}

		FIELDS.put(c, fieldName, field);
		return field;
	}

	/**
	 * Invoke a static {@link Method}.
	 * @param method The {@link Method} to invoke.
	 * @return The result of the invocation if successful, otherwise {@code null}.
	 * @param <Type> The expected return type from the invocation.
	 */
	public static <Type> @Nullable Type methodInvoke(Method method) {
		return methodInvoke(method, null);
	}

	/**
	 * Invoke a {@link Method}.
	 * @param method The {@link Method} to invoke.
	 * @param holder The holder object to invoke for.
	 * @param params The parameters to pass into the invocation.
	 * @return The result of the invocation if successful, otherwise {@code null}.
	 * @param <Type> The expected return type from the invocation.
	 */
	public static <Type> @Nullable Type methodInvoke(Method method, @Nullable Object holder, Object @Nullable ... params) {
		method.setAccessible(true);
		try {
			//noinspection unchecked
			return (Type) method.invoke(holder, params);
		} catch (IllegalAccessException | InvocationTargetException ignored) {}
		return null;
	}

	/**
	 * Gets the values of a static {@link Field}.
	 * @param field The {@link Field} to get from.
	 * @return The value of the {@link Field}.
	 * @param <Type> The expected return type.
	 */
	public static <Type> @Nullable Type fieldGet(Field field) {
		return fieldGet(field, null);
	}

	/**
	 * Gets the values of a {@link Field}.
	 * @param field The {@link Field} to get from.
	 * @param holder The holder object to get the field for.
	 * @return The value of the {@link Field}.
	 * @param <Type> The expected return type.
	 */
	public static <Type> @Nullable Type fieldGet(Field field, @Nullable Object holder) {
		field.setAccessible(true);
		try {
			//noinspection unchecked
			return (Type) field.get(holder);
		} catch (IllegalAccessException ignored) {}
		return null;
	}

	/**
	 * Record for caching data for a method.
	 * @param c The {@link Class} the method belongs to.
	 * @param methodName The name of the method.
	 * @param params The types of parameters for the method.
	 */
	private record MethodID(Class<?> c, String methodName, Class<?> @Nullable ... params) {}

	/**
	 * Custom map for correlating a {@link Class} to a {@link Method}.
	 */
	private static class MethodMap extends HashMap<Class<?>, Map<MethodID, Method>> {

		public boolean contains(Class<?> c, MethodID methodID) {
			if (!containsKey(c))
				return false;
			return get(c).containsKey(methodID);
		}

		public void put(Class<?> c, MethodID methodID, Method method) {
			computeIfAbsent(c, map -> new HashMap<>()).put(methodID, method);
		}

		public @Nullable Method get(Class<?> c, MethodID methodID) {
			if (!contains(c, methodID))
				return null;
			return get(c).get(methodID);
		}

	}

	/**
	 * Custom map for correlating a {@link Class} to a {@link Field}.
	 */
	private static class FieldMap extends HashMap<Class<?>, Map<String, Field>> {

		public boolean contains(Class<?> c, String fieldName) {
			if (!containsKey(c))
				return false;
			return get(c).containsKey(fieldName);
		}

		public void put(Class<?> c, String fieldName, Field field) {
			computeIfAbsent(c, map -> new HashMap<>()).put(fieldName, field);
		}

		public @Nullable Field get(Class<?> c, String fieldName) {
			if (!contains(c, fieldName))
				return null;
			return get(c).get(fieldName);
		}

	}

}
