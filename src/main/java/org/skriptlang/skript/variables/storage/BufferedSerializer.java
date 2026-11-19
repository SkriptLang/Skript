package org.skriptlang.skript.variables.storage;

import ch.njol.skript.variables.SerializedVariable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Serializes values into a buffer it reuses, for the refresh, which serializes every variable but saves only the
 * few that changed. Only the changed ones need copying out of the buffer, so checking the rest allocates nothing.
 * <p>
 * The result is only valid until the next {@link #serialize(Object)}. Not thread safe.
 */
@ApiStatus.Internal
public interface BufferedSerializer {

	/**
	 * Serializes a value into the buffer.
	 *
	 * @param value the value
	 * @return whether it could be serialized. If not, the buffer holds nothing valid
	 */
	boolean serialize(Object value);

	/**
	 * @return the type of the last serialized value
	 */
	String type();

	/**
	 * @return the buffer holding the last serialized value, from {@link #offset()} for {@link #length()} bytes
	 */
	byte[] data();

	/**
	 * @return where the last serialized value starts in {@link #data()}
	 */
	int offset();

	/**
	 * @return how long the last serialized value is
	 */
	int length();

	/**
	 * @return a copy of the last serialized value, to keep after the next {@link #serialize(Object)}
	 */
	default SerializedVariable.Value toValue() {
		return new SerializedVariable.Value(type(), Arrays.copyOfRange(data(), offset(), offset() + length()));
	}

	/**
	 * Wraps a serializer that returns a new value each time, which then isn't buffered.
	 *
	 * @param serializer serializes a value, or returns null if it can't
	 * @return a buffered serializer that uses it
	 */
	static BufferedSerializer of(Function<@Nullable Object, SerializedVariable.@Nullable Value> serializer) {
		return new BufferedSerializer() {
			private SerializedVariable.@Nullable Value last;

			@Override
			public boolean serialize(Object value) {
				last = serializer.apply(value);
				return last != null;
			}

			private SerializedVariable.Value last() {
				if (last == null)
					throw new IllegalStateException("nothing was serialized");
				return last;
			}

			@Override
			public String type() {
				return last().type;
			}

			@Override
			public byte[] data() {
				return last().data;
			}

			@Override
			public int offset() {
				return 0;
			}

			@Override
			public int length() {
				return last().data.length;
			}

			@Override
			public SerializedVariable.Value toValue() {
				return last(); // already a new value of its own
			}
		};
	}

}
