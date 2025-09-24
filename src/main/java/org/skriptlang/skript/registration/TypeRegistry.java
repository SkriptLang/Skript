package org.skriptlang.skript.registration;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.util.Registry;
import org.skriptlang.skript.util.ViewProvider;

import java.util.Collection;

@ApiStatus.Experimental
public interface TypeRegistry extends ViewProvider<TypeRegistry>, Registry<TypeInfo<?>> {

	/**
	 * Constructs a default implementation of a type registry.
	 *
	 * @return A type registry containing no elements.
	 */
	@Contract("-> new")
	static @NotNull TypeRegistry empty() {
		return new TypeRegistryImpl();
	}

	/**
	 * Registers a new type.
	 *
	 * @param info The type info to register.
	 * @param <I>  The type's type.
	 */
	<I extends TypeInfo<?>> void register(@NotNull I info);

	/**
	 * Unregisters all registrations of a type.
	 *
	 * @param info The type info to unregister.
	 */
	void unregister(@NotNull TypeInfo<?> info);

	/**
	 * Constructs an unmodifiable view of this type registry.
	 * That is, the returned registry will not allow registrations.
	 *
	 * @return An unmodifiable view of this type registry.
	 */
	@Override
	@Contract("-> new")
	default @NotNull TypeRegistry unmodifiableView() {
		return new TypeRegistryImpl.UnmodifiableRegistry(this);
	}

	/**
	 * {@inheritDoc}
	 * There are no guarantees on the ordering of the returned collection.
	 *
	 * @return An unmodifiable snapshot of all types registered.
	 */

	@Override
	@Unmodifiable @NotNull Collection<TypeInfo<?>> elements();

	/**
	 * Returns the {@link TypeInfo} from a string pattern,
	 * which should equal one of the values in {@link TypeInfo#patterns()}.
	 *
	 * @param pattern The pattern.
	 * @param <T>     The type of the info.
	 * @return The {@link TypeInfo}, or null if none is found.
	 */
	<T> TypeInfo<T> fromPattern(@NotNull String pattern);

	/**
	 * Returns the {@link TypeInfo} from a class.
	 *
	 * @param cls The class.
	 * @param <T> The type of the info.
	 * @return The {@link TypeInfo}, or null if none is found.
	 */
	<T> TypeInfo<T> fromClass(@NotNull Class<T> cls);

	/**
	 * Returns a {@link TypeInfo} from a class.
	 * If no exact type is found, returns a {@link TypeInfo}
	 * of a super class of {@link TypeInfo#type()}.
	 *
	 * @param cls The class.
	 * @param <T> The type of the info.
	 * @return The {@link TypeInfo}, or null if none is found.
	 */
	<T> @NotNull TypeInfo<? extends T> superTypeFromClass(@NotNull Class<T> cls);

}
