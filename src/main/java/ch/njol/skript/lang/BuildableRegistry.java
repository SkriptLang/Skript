package ch.njol.skript.lang;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.registrations.Classes;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for registering classes that normally can not be builded upon, such as {@link Enum}s or from {@link org.bukkit.Registry}.
 * But can be used to build another object, such as {@link InventoryType} to {@link Inventory}
 */
public class BuildableRegistry {

	private static final Map<Class<?>, BuildableIdentifier> registry = new HashMap<>();

	/**
	 * Register {@code from} to be builded as {@code to} using {@code converter}.
	 *
	 * @param from The {@link Class} that normally can not be builded upon.
	 * @param to The {@link Class} it can be used to build as.
	 * @param converter The {@link Converter} to convert {@code from} to {@code to}.
	 * @throws SkriptAPIException If {@code from} is already registered to another class.
	 */
	public static <F, T> void registerBuildable(Class<F> from, Class<T> to, Converter<F, T> converter) {
		BuildableIdentifier identifier = new BuildableIdentifier(to, converter);
		if (registry.containsKey(from)) {
			throw new SkriptAPIException("A buildable of '" + Classes.toString(from) + "' is already registered to '" +
				Classes.toString(registry.get(from).to) + "'.");
		}
		registry.put(from, identifier);
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
	 * Retrieves the {@link Converter} that {@code from} is registered to.
	 *
	 * @param from The {@link Class} to get the registered converter.
	 * @return The {@link Converter} or {@code null}.
	 */
	public static <F, T> @Nullable Converter<F, T> getConverter(Class<F> from) {
		if (!isRegistered(from))
			return null;
		BuildableIdentifier identifier = registry.get(from);
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

		BuildableIdentifier identifier = registry.get(from);
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
		BuildableIdentifier identifier = registry.get(fromClass);
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

	private record BuildableIdentifier(Class<?> to, Converter<?, ?> converter) {}

}
