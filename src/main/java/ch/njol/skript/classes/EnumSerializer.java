package ch.njol.skript.classes;

import java.io.StreamCorruptedException;

import org.jetbrains.annotations.Nullable;

import ch.njol.yggdrasil.ClassResolver;
import ch.njol.yggdrasil.Fields;

/**
 * Mainly kept for backwards compatibility, but also serves as {@link ClassResolver} for enums.
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
	@Deprecated
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
		assert false;
		return false;
	}
	
	@Override
	public Fields serialize(T e) {
		Fields fields = new Fields();
		fields.putPrimitive("name", e.name());
		return fields;
	}
	
	@Override
	public T deserialize(Fields fields) {
		try {
			return Enum.valueOf(c, fields.getAndRemovePrimitive("name", String.class));
		} catch (IllegalArgumentException | StreamCorruptedException e) {
			return null;
		}
	}
	
	@Override
	public void deserialize(T o, Fields f) {
		assert false;
	}
	
}
