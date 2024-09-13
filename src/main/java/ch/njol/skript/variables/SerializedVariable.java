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

import org.jetbrains.annotations.Nullable;

/**
 * An instance of a serialized variable, contains the variable name
 * and the serialized value.
 */
public class SerializedVariable {

	/**
	 * The name of the variable.
	 */
	public final String name;

	/**
	 * The serialized value of the variable.
	 * <p>
	 * A value of {@code null} indicates the variable will be deleted.
	 */
	@Nullable
	public final Value value;

	/**
	 * Creates a new serialized variable with the given name and value.
	 *
	 * @param name the given name.
	 * @param value the given value, or {@code null} to indicate a deletion.
	 */
	public SerializedVariable(String name, @Nullable Value value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * A serialized value of a variable.
	 */
	public static final class Value {

		/**
		 * The type of this value.
		 */
		public final String type;

		/**
		 * The serialized value data.
		 */
		public final byte[] data;

		/**
		 * Creates a new serialized value.
		 * @param type the value type.
		 * @param data the serialized value data.
		 */
		public Value(String type, byte[] data) {
			this.type = type;
			this.data = data;
		}

	}

}
