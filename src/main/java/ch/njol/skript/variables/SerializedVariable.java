package ch.njol.skript.variables;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * An instance of a serialized variable, contains the variable name
 * and the serialized value.
 */
public class SerializedVariable {

	/**
	 * The name of the variable.
	 * <p>
	 * @deprecated Marked as internal, as this field will soon be private.
	 */
	@Deprecated
	@ApiStatus.Internal // Remove the internal status when the field is private.
	public final String name;

	/**
	 * The serialized value of the variable.
	 * <p>
	 * A value of {@code null} indicates the variable will be deleted.
	 * <p>
	 * @deprecated Marked as internal, as this field will soon be private.
	 */
	@Nullable
	@Deprecated
	@ApiStatus.Internal // Remove the internal status when the field is private.
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

	public SerializedVariable(String name, String type, byte[] value) {
		this(name, new Value(type, value));
	}

	@Nullable
	public String getType() {
		if (value == null)
			return null;
		return value.type;
	}

	@Nullable
	public byte[] getData() {
		if (value == null)
			return null;
		return value.data;
	}

	@Nullable
	public Value getValue() {
		return value;
	}

	public String getName() {
		return name;
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
