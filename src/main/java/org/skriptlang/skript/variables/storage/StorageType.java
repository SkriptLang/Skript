package org.skriptlang.skript.variables.storage;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.skriptlang.skript.addon.SkriptAddon;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * A storage type as registered, before any storage of it is created.
 * Skript keeps one for each registered type, and creates a storage from it for every config section
 * whose {@code type} option matches one of its names.
 *
 * @param addon        the addon that registered the type
 * @param storageClass the class of the storages it creates
 * @param constructor  creates a storage, given the registering addon and the type name the config used
 * @param names        the names users can give as the {@code type}, such as {@code csv}; matched ignoring case
 * @param <T>          the class of the storages it creates
 */
@ApiStatus.Internal
record StorageType<T extends VariableStorage>(SkriptAddon addon, Class<T> storageClass,
											  BiFunction<SkriptAddon, String, T> constructor, String... names) {

	StorageType {
		Objects.requireNonNull(storageClass, "storageClass");
		Objects.requireNonNull(constructor, "constructor");
		if (names.length == 0)
			throw new IllegalArgumentException("A storage type needs at least one name: " + storageClass.getName());
		names = names.clone(); // so the caller's array can't change the names afterwards
		for (String name : names) {
			if (name == null || name.isBlank())
				throw new IllegalArgumentException("A storage type's names must not be blank: " + storageClass.getName());
		}
	}

	/**
	 * Creates a new storage of this type.
	 *
	 * @param type the type name the config used, one of {@link #names()}
	 * @return a new, unconfigured storage
	 */
	T create(String type) {
		return constructor.apply(addon, type);
	}

	/**
	 * @return a copy of the names users can give as the {@code type}
	 */
	@Override
	@Contract(value = "-> new", pure = true)
	public String[] names() {
		return Arrays.copyOf(names, names.length);
	}

	/**
	 * @param input a {@code type} from the config
	 * @return whether it is one of this type's names, ignoring case
	 */
	boolean matches(String input) {
		for (String name : names) {
			if (name.equalsIgnoreCase(input))
				return true;
		}
		return false;
	}

	/**
	 * @param other another registered type
	 * @return whether the two share a name, ignoring case, so a config's {@code type} couldn't tell them apart
	 */
	boolean sharesNameWith(StorageType<?> other) {
		for (String name : names) {
			if (other.matches(name))
				return true;
		}
		return false;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof StorageType<?> other && addon == other.addon && storageClass == other.storageClass
			&& constructor == other.constructor && Arrays.equals(names, other.names);
	}

	@Override
	public int hashCode() {
		return Objects.hash(addon, storageClass, constructor, Arrays.hashCode(names));
	}

	@Override
	public String toString() {
		return "StorageType[" + storageClass.getName() + " as " + Arrays.toString(names) + "]";
	}

}
