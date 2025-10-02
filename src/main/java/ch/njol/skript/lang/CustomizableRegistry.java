package ch.njol.skript.lang;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.registrations.Classes;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for registering classes that normally can not be builded upon, such as {@link Enum}s or from {@link org.bukkit.Registry}.
 * But can be used to build another object, such as {@link InventoryType} to {@link Inventory}
 */
public class CustomizableRegistry {

	private static final Map<Class<?>, CustomizableIdentifier> registry = new HashMap<>();
	private static final List<Class<?>> disallowed = new ArrayList<>();

	/**
	 * Register {@code from} to be builded as {@code to} using {@code converter}.
	 *
	 * @param from The {@link Class} that normally can not be builded upon.
	 * @param to The {@link Class} it can be used to build as.
	 * @param converter The {@link Converter} to convert {@code from} to {@code to}.
	 * @throws SkriptAPIException If {@code from} is already registered to another class.
	 * @throws SkriptAPIException If {@code from} is already disallowed.
	 */
	public static <F, T> void registerCustomizable(Class<F> from, Class<T> to, Converter<F, T> converter) {
		CustomizableIdentifier identifier = new CustomizableIdentifier(to, converter);
		if (registry.containsKey(from)) {
			throw new SkriptAPIException("A buildable of '" + Classes.toString(from) + "' is already registered to '" +
				Classes.toString(registry.get(from).to) + "'.");
		} else if (disallowed.contains(from)) {
			throw new SkriptAPIException("Unable to register '" + Classes.toString(from) + "' as it has already been disallowed.");
		}
		registry.put(from, identifier);
	}

	/**
	 * Disallow {@link Class}es from being builded upon.
	 *
	 * @param types The {@link Class}es to disallow.
	 * @throws SkriptAPIException If any of {@code types} is already registered.
	 */
	public static void registerDisallowed(Class<?>... types) {
		for (Class<?> type : types) {
			if (registry.containsKey(type))
				throw new SkriptAPIException("Unable to disallow '" + Classes.toString(type) + "' as it is already registered.");
			disallowed.add(type);
		}
	}

	/**
	 * Check if {@code from} is registered.
	 *
	 * @param from The {@link Class} to check.
	 * @return {@code true} if it's registered, otherwise {@code false}.
	 */
	public static <F> boolean isRegistered(Class<F> from) {
		return registry.containsKey(from);
	}

	/**
	 * Check if {@code type} is disallowed exactly.
	 *
	 * @param type The {@link Class} to check.
	 * @return {@code true} if it's disallowed, otherwise {@code false}.
	 */
	public static boolean isExactDisallowed(Class<?> type) {
		return disallowed.contains(type);
	}

	/**
	 * Check if {@code type} or super type is disallowed.
	 *
	 * @param type The {@link Class} to check.
	 * @return {@code true} if it's disallowed, otherwise {@code false}.
	 */
	public static boolean isDisallowed(Class<?> type) {
		for (Class<?> clazz : disallowed) {
			if (clazz.isAssignableFrom(type))
				return true;
		}
		return false;
	}

	/**
	 * Retrieves the {@link Converter} that {@code from} is registered to.
	 *
	 * @param from The {@link Class} to get the registered converter.
	 * @return The {@link Converter} or {@code null}.
	 */
	public static <F, T> @Nullable Converter<F, T> getConverter(Class<F> from) {
		if (!isRegistered(from))
			return null;
		CustomizableIdentifier identifier = registry.get(from);
		//noinspection unchecked
		return (Converter<F, T>) identifier.converter;
	}

	/**
	 * Retrieves the {@link Class} that {@code from} is registered to convert to.
	 *
	 * @param from The {@link Class} to be converted from.
	 * @return The {@link Class} {@code from} is registered to, otherwise {@code null}.
	 */
	public static <F, T> @Nullable Class<T> getConvertedClass(Class<F> from) {
		if (!isRegistered(from))
			return null;

		CustomizableIdentifier identifier = registry.get(from);
		//noinspection unchecked
		return (Class<T>) identifier.to;
	}

	/**
	 * Converts {@code from} to the {@link Class} it is registered to.
	 *
	 * @param from The {@link Object} used to convert.
	 * @return The converted {@link Object} or {@code null}.
	 */
	public static <F, T> @Nullable T convert(F from) {
		if (from == null)
			return null;

		//noinspection unchecked
		Class<F> fromClass = (Class<F>) from.getClass();
		if (!isRegistered(fromClass))
			return null;
		CustomizableIdentifier identifier = registry.get(fromClass);
		//noinspection unchecked
		Class<T> toClass = (Class<T>) identifier.to;

		if (toClass.isInstance(from)) {
			//noinspection unchecked
			return (T) from;
		}

		//noinspection unchecked
		Converter<F, T> converter = (Converter<F, T>) identifier.converter;
		return converter.convert(from);
	}

	private record CustomizableIdentifier(Class<?> to, Converter<?, ?> converter) {}

}
