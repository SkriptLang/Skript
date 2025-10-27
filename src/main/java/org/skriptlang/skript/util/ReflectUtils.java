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

	public ReflectUtils() {}

	/**
	 * Cache for classes.
	 */
	private final Map<String, Class<?>> classes = new HashMap<>();

	/**
	 * Cache for methods of classes.
	 */
	private final MethodMap methods = new MethodMap();

	/**
	 * Cache for fields of classes.
	 */
	private final FieldMap fields = new FieldMap();

	/**
	 * @param className The full package and class name.
	 * @return Whether the class exists.
	 */
	public boolean classExists(String className) {
		return getClass(className) != null;
	}

	/**
	 * @param className The full package and class name.
	 * @return The resulting {@link Class} if found, otherwise {@code null}.
	 */
	public @Nullable Class<?> getClass(String className) {
		if (classes.containsKey(className))
			return classes.get(className);
		Class<?> c = null;
		try {
			c = Class.forName(className);
		} catch (ClassNotFoundException ignored) {}
		classes.put(className, c);
		return c;
	}

	/**
	 * @param c The {@link Class} to check the method for.
	 * @param methodName The name of the method.
	 * @param params The {@link Class}es used as parameters for the desired method.
	 * @return Whether the method exists.
	 */
	public boolean methodExists(Class<?> c, String methodName, Class<?> @Nullable ... params) {
		return methodExists(c, methodName, params, null);
	}

	/**
	 * @param c The {@link Class} to check the method for.
	 * @param methodName The name of the method.
	 * @param params The {@link Class}es used as parameters for the desired method.
	 * @param returnTpe The return type of the desired method.
	 * @return Whether the method exists.
	 */
	public boolean methodExists(Class<?> c, String methodName, Class<?> @Nullable [] params, @Nullable Class<?> returnTpe) {
		return getMethod(c, methodName, params, returnTpe) != null;
	}

	/**
	 * @param c The {@link Class} to get the method from.
	 * @param methodName The name of the method.
	 * @param params The {@link Class}es used as parameters for the desired method.
	 * @return The resulting {@link Method} if it exists, otherwise {@code null}.
	 */
	public @Nullable Method getMethod(Class<?> c, String methodName, Class<?> @Nullable ... params) {
		return getMethod(c, methodName, params, null);
	}

	/**
	 * @param c The {@link Class} to get the method from.
	 * @param methodName The name of the method.
	 * @param params The {@link Class}es used as parameters for the desired method.
	 * @param returnType The return type of the desired method.
	 * @return The resulting {@link Method} if it exists, otherwise {@code null}.
	 */
	public @Nullable Method getMethod(Class<?> c, String methodName, Class<?> @Nullable [] params, @Nullable Class<?> returnType) {
		MethodID methodID = new MethodID(c, methodName, params);
		if (methods.contains(c, methodID)) {
			Method method = methods.get(c, methodID);
			if (method != null && returnType != null) {
				Class<?> methodType = method.getReturnType();
				if (!returnType.isAssignableFrom(methodType) && !methodType.isAssignableFrom(returnType))
					return null;
			}
			return methods.get(c, methodID);
		}
		Method method = null;
		try {
			method = c.getDeclaredMethod(methodName, params);
		} catch (NoSuchMethodException ignored) {}

		methods.put(c, methodID, method);
		if (method != null && returnType != null) {
			Class<?> methodType = method.getReturnType();
			if (!returnType.isAssignableFrom(methodType) && !methodType.isAssignableFrom(returnType))
				return null;
		}
		return method;
	}

	/**
	 * @param c The {@link Class} to check the field for.
	 * @param fieldName The name of the field.
	 * @return Whether the field exists.
	 */
	public boolean fieldExists(Class<?> c, String fieldName) {
		return getField(c, fieldName) != null;
	}

	/**
	 * @param c The {@link Class} to get the field from.
	 * @param fieldName The name of the field.
	 * @return The resulting {@link Field} if it exists, otherwise {@code null}.
	 */
	public @Nullable Field getField(Class<?> c, String fieldName) {
		if (fields.contains(c, fieldName))
			return fields.get(c, fieldName);
		Field field = null;
		try {
			field = c.getDeclaredField(fieldName);
		} catch (NoSuchFieldException ignored) {}

		fields.put(c, fieldName, field);
		return field;
	}

	/**
	 * Invoke a static {@link Method}.
	 * @param method The {@link Method} to invoke.
	 * @return The result of the invocation if successful, otherwise {@code null}.
	 * @param <Type> The expected return type from the invocation.
	 */
	public <Type> @Nullable Type invokeMethod(Method method) {
		return invokeMethod(method, null);
	}

	/**
	 * Invoke a {@link Method}.
	 * @param method The {@link Method} to invoke.
	 * @param holder The holder object to invoke for.
	 * @param params The parameters to pass into the invocation.
	 * @return The result of the invocation if successful, otherwise {@code null}.
	 * @param <Type> The expected return type from the invocation.
	 */
	public <Type> @Nullable Type invokeMethod(Method method, @Nullable Object holder, Object @Nullable ... params) {
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
	public <Type> @Nullable Type getFieldValue(Field field) {
		return getFieldValue(field, null);
	}

	/**
	 * Gets the values of a {@link Field}.
	 * @param field The {@link Field} to get from.
	 * @param holder The holder object to get the field for.
	 * @return The value of the {@link Field}.
	 * @param <Type> The expected return type.
	 */
	public <Type> @Nullable Type getFieldValue(Field field, @Nullable Object holder) {
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
	private record MethodID(Class<?> c, String methodName, Class<?> @Nullable [] params) {}

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
