package ch.njol.skript.variables;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.skriptlang.skript.addon.SkriptAddon;

import java.util.Arrays;
import java.util.function.BiFunction;

/**
 * Represents an unloaded storage type for variables.
 * This class stores all the data from register time to be used if this database is selected.
 *
 * @param source The SkriptAddon that is registering this storage type.
 * @param storage The class of the actual VariableStorage to initalize with.
 * @param names The possible user input names from the config.sk to match this storage.
 */
@ApiStatus.Internal
record UnloadedStorage<T extends VariableStorage>(SkriptAddon source, Class<T> storage,
		BiFunction<SkriptAddon, String, T> constructor, String... names) {

	/**
	 * Creates new variable storage instance of this type.
	 *
	 * @param addon addon that created this storage instance
	 * @param name the database type i.e. CSV.
	 * @return variable storage of this type
	 */
	public T create(SkriptAddon addon, String name) {
		return constructor.apply(addon, name);
	}

	@Override
	@Contract(value = " -> new", pure = true)
	public String @NonNull [] names() {
		return Arrays.copyOf(names, names.length);
	}

	/**
	 * Checks if a user input matches this storage input names.
	 *
	 * @param input The name to check against.
	 * @return true if this storage matches the user input, otherwise false.
	 */
	public boolean matches(String input) {
		for (String name : names) {
			if (name.equalsIgnoreCase(input))
				return true;
		}
		return false;
	}

}
