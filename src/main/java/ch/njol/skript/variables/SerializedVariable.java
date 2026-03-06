package ch.njol.skript.variables;

import org.jetbrains.annotations.Nullable;

/**
 * An instance of a serialized variable, contains the variable name
 * and the serialized value.
 *
 * @param name  The name of the variable.
 * @param value The serialized value of the variable.
 *              A value of {@code null} indicates the variable will be deleted.
 */
public record SerializedVariable(String name, @Nullable Value value) {

	public SerializedVariable(String name, String type, byte[] data) {
		this(name, new Value(type, data));
	}

	/**
	 * A serialized value of a variable.
	 *
	 * @param type The type of this value.
	 * @param data The serialized value data.
	 */
	public record Value(String type, byte[] data) {
	}

}
