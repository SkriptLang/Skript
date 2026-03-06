package ch.njol.skript.variables;

import ch.njol.skript.SkriptAddon;

/**
 * Represents an unloaded storage type for variables.
 * This class stores all the data from register time to be used if this database is selected.
 */
public class UnloadedStorage {

	private final Class<? extends VariableStorage> storage;
	private final SkriptAddon source;
	private final String[] names;

	/**
	 * Construct an unloaded storage that contains the register data of a storage type.
	 * 
	 * @param source The SkriptAddon that is registering this storage type.
	 * @param storage The class of the actual VariableStorage to initalize with.
	 * @param names The possible user input names from the config.sk to match this storage.
	 */
	public UnloadedStorage(SkriptAddon source, Class<? extends VariableStorage> storage, String... names) {
		this.storage = storage;
		this.source = source;
		this.names = names;
	}

	/**
	 * @return the storage class
	 */
	public Class<? extends VariableStorage> getStorageClass() {
		return storage;
	}

	/**
	 * @return the SkriptAddon source that registered this storage.
	 */
	public SkriptAddon getSource() {
		return source;
	}

	/**
	 * @return the possible user input names
	 */
	public String[] getNames() {
		return names;
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
