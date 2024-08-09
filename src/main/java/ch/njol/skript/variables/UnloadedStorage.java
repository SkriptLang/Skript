/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.variables;

import ch.njol.skript.SkriptAddon;

/**
 * Represents an unloaded storage type for variables.
 * This class stores all the data from register time to be used if this database is selected.
 */
public class UnloadedStorage {

	private final Class<? extends VariablesStorage> storage;
	private final SkriptAddon source;
	private final String[] names;

	/**
	 * Construct an unloaded storage that contains the register data of a storage type.
	 * 
	 * @param source The SkriptAddon that is registering this storage type.
	 * @param storage The class of the actual VariableStorage to initalize with.
	 * @param names The possible user input names from the config.sk to match this storage.
	 */
	public UnloadedStorage(SkriptAddon source, Class<? extends VariablesStorage> storage, String... names) {
		this.storage = storage;
		this.source = source;
		this.names = names;
	}

	/**
	 * @return the storage class
	 */
	public Class<? extends VariablesStorage> getStorageClass() {
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
