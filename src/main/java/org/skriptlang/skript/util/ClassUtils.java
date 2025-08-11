package org.skriptlang.skript.util;

import com.google.common.base.Preconditions;

import java.lang.invoke.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

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

	private static final Map<Class<?>, Supplier<?>> instanceSuppliers = new ConcurrentHashMap<>();

	/**
	 * Creates supplier for given class if its nullary constructor exists.
	 *
	 * @param type class to create the supplier for
	 * @return supplier for the instances of given class, using its nullary constructor
	 * @param <T> type
	 */
	@SuppressWarnings("unchecked")
	public static <T> Supplier<T> instanceSupplier(Class<T> type) throws Throwable {
		Supplier<?> cached = instanceSuppliers.get(type);
		if (cached != null)
			return (Supplier<T>) cached;
		Preconditions.checkArgument(
			!Modifier.isAbstract(type.getModifiers()) && !Modifier.isInterface(type.getModifiers()),
			"You can not create instance supplier for abstract classes");
		Constructor<T> nullaryConstructor = type.getDeclaredConstructor();
		MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(type, MethodHandles.lookup());
		MethodHandle methodHandle = lookup.unreflectConstructor(nullaryConstructor);
		CallSite callSite = LambdaMetafactory.metafactory(lookup,
			"get",
			MethodType.methodType(Supplier.class),
			MethodType.methodType(Object.class),
			methodHandle,
			methodHandle.type()
		);
		Supplier<T> created = (Supplier<T>) callSite.getTarget().invokeExact();
		instanceSuppliers.put(type, created);
		return created;
	}

}
