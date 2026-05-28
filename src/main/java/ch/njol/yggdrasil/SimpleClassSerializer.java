package ch.njol.yggdrasil;

import org.jetbrains.annotations.Nullable;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;

/**
 * A simple serializer for a single class. Useful for registering serializers for external classes that should not
 * have their own classinfos, and that are not handled by {@link ch.njol.skript.classes.ConfigurationSerializer}
 * @param <T> the type of the class to serialize
 */
public abstract class SimpleClassSerializer<T> extends YggdrasilSerializer<T> {

	protected final Class<T> type;
	protected final String id;

	public SimpleClassSerializer(Class<T> type, String id) {
		this.type = type;
		this.id = id;
	}

	@Override
	public @Nullable Class<? extends T> getClass(String id) {
		return this.id.equals(id) ? this.type : null;
	}

	@Override
	public @Nullable String getID(Class<?> clazz) {
		return this.type.equals(clazz) ? this.id : null;
	}

	/**
	 * A simple serializer for classes that cannot be instantiated (e.g. abstract classes or interfaces).
	 * The same as {@link SimpleClassSerializer}, but overrides instantiation methods to prevent instantiation.
	 * Only deserialization via {@link #deserialize(Fields)} is supported. {@link #deserialize(Class, Fields)} will
	 * call that method internally. {@link #deserialize(Object, Fields)} will throw an exception.
	 * <br>
	 * {@link #newInstance(Class)} will always return null, and {@link #canBeInstantiated(Class)} will always
	 * return false.
	 *
	 * @param <T> the type of the class to serialize
	 */
	public static abstract class NonInstantiableClassSerializer<T> extends SimpleClassSerializer<T> {

		public NonInstantiableClassSerializer(Class<T> type, String id) {
			super(type, id);
		}

		@Override
		public final boolean canBeInstantiated(Class<? extends T> type) {
			return false;
		}

		@Override
		public final <E extends T> @Nullable E newInstance(Class<E> c) {
			return null;
		}

		@Override
		public final void deserialize(T object, Fields fields) throws StreamCorruptedException, NotSerializableException {
			throw new UnsupportedOperationException("This class cannot be instantiated");
		}

		@Override
		@SuppressWarnings("unchecked")
		public <E extends T> E deserialize(Class<E> type, Fields fields) throws StreamCorruptedException, NotSerializableException {
			assert this.type.equals(type);
			return (E) deserialize(fields);
		}

		/**
		 * Used to deserialize objects that cannot be instantiated.
		 *
		 * @param fields The Fields object that holds the information about the serialised object
		 * @return The deserialized object. Must not be null (throw an exception instead).
		 */
		abstract protected T deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException;

	}

}
