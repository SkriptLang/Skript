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
