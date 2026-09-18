package ch.njol.skript.classes;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;

import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializable;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;

/**
 * Serializer that allows Yggdrasil to automatically serialize classes that extend YggdrasilSerializable.
 */
public class YggdrasilSerializer<T extends YggdrasilSerializable> extends Serializer<T> {

	/**
		- Creates a serializer that lets Yggdrasil handle each subtype separately
		instead of using the parent ClassInfo ID.
		- This is useful for types like Color, where the actual type can be
		SkriptColor or ColorRGB
		- Most types should just use a normal YggdrasilSerializer
		
		- Each subtype needs to be registered with Yggdrasil and meet its usual
		requirements for enums, constructors, or custom serializers.
		
		- Variable storage keeps the full header so the actual type and whether it
		is an enum or normal object can be restored correctly.
	*/
	public static <T extends YggdrasilSerializable> YggdrasilSerializer<T> delegatingToSubtypeSerializers() {
		return new YggdrasilSerializer<>() {
			@Override
			public String getID(Class<?> type) {
				assert info != null;
				return type == info.getC() ? info.getCodeName() : null;
			}

			@Override
			public boolean usesClassInfoHeader() {
				return false;
			}
		};
	}
	
	@Override
	public final Fields serialize(final T o) throws NotSerializableException {
		if (o instanceof YggdrasilExtendedSerializable)
			return ((YggdrasilExtendedSerializable) o).serialize();
		return new Fields(o);
	}
	
	@Override
	public final void deserialize(final T o, final Fields f) throws StreamCorruptedException, NotSerializableException {
		if (o instanceof YggdrasilExtendedSerializable)
			((YggdrasilExtendedSerializable) o).deserialize(f);
		else
			f.setFields(o);
	}
	
	/**
	 * Deserialises an object from a string returned by this serializer or an earlier version thereof.
	 * <p>
	 * This method should only return null if the input is invalid (i.e. not produced by {@link #serialize(Object)} or an older version of that method)
	 * <p>
	 * This method must only be called from Bukkit's main thread if {@link #mustSyncDeserialization()} returned true.
	 * 
	 * @param s
	 * @return The deserialised object or null if the input is invalid. An error message may be logged to specify the cause.
	 */
	@Deprecated(since = "2.3.0", forRemoval = true)
	@Override
	public T deserialize(String s) {
		return null;
	}
	
	@Override
	public boolean mustSyncDeserialization() {
		return false;
	}
	
	@Override
	public boolean canBeInstantiated() {
		return true;
	}
	
}
