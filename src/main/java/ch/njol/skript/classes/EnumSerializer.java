package ch.njol.skript.classes;

import ch.njol.yggdrasil.ClassResolver;
import ch.njol.yggdrasil.Fields;
import org.jetbrains.annotations.Nullable;

import java.io.StreamCorruptedException;

/**
 * Mainly kept for backwards compatibility, but also serves as {@link ClassResolver} for enums.
 * This won't be used for saving to output stream, but is useful when saving to {@link Fields} representations.
 */
public class EnumSerializer<T extends Enum<T>> extends Serializer<T> {
	
	private final Class<T> c;
	
	public EnumSerializer(Class<T> c) {
		this.c = c;
	}
	
	/**
	 * Enum serialization has been using String serialization since Skript (2.7)
	 */
	@Override
	@Deprecated(since = "2.3.0", forRemoval = true)
	@Nullable
	public T deserialize(String s) {
		try {
			return Enum.valueOf(c, s);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}
	
	@Override
	public boolean mustSyncDeserialization() {
		return false;
	}
	
	@Override
	public boolean canBeInstantiated() {
		return false;
	}
	
	@Override
	public Fields serialize(T e) {
		Fields fields = new Fields();
		fields.putObject("name", e.name());
		return fields;
	}
	
	@Override
	public T deserialize(Fields fields) {
		try {
			String name = fields.getAndRemoveObject("name", String.class);
			if (name == null)
				return null;
			return Enum.valueOf(c, name);
		} catch (IllegalArgumentException | StreamCorruptedException e) {
			return null;
		}
	}
	
	@Override
	public void deserialize(T o, Fields f) {
		assert false;
	}
	
}
