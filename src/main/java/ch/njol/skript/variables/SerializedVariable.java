package ch.njol.skript.variables;

import org.jetbrains.annotations.Nullable;

/**
	- Holds a variable change that needs to be sent to storage.

	- {@link Variables} creates these on the server thread and puts them in a
	queue for {@link VariablesStorage}. Database workers use the variable name
	and serialized data instead of keeping the actual Bukkit objects around.

	- A non-null {@link #value} means the variable has a serialized value. A null
	value means the variable should be deleted. This makes sure a failed
	serialization is not accidentally treated as a deletion.

	- The fields cannot be changed, but the byte array is not copied. Producers
	and consumers should treat it as read-only after it has been handed over.

	- The optional MySQL backend uses this same object for its queue, pending
	changes, and recovery journal.

	- @see ch.njol.skript.registrations.Classes#serialize(Object)
	- @see MySQLJournal
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
		 * The registered ClassInfo code name used by Classes.deserialize.
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
