package ch.njol.yggdrasil;

import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A class that acts as a "pseudo-enum", i.e. a class which only has immutable, (public,) static final instances,
 * which can be identified by their unique name. The instances don't even have to be defined in their class,
 * as they are registered in the constructor.
 * <p>
 * Please note that you cannot define a constant's id used for saving by annotating it with
 * {@link YggdrasilID @YggdrasilID}, as the field(s) of the constant may not be known, and
 * furthermore a constant can be assigned to any number of fields.
 * <p>
 * This class defines methods similar to those in {@link Enum} with minor differences, e.g. {@link #values()} returns
 * a {@link List} instead of an array.
 */
@ThreadSafe
public abstract class PseudoEnum<T extends PseudoEnum<T>> {
	
	private final String name;
	private final int ordinal;
	private final Info<T> info;
	
	/**
	 * @param name The unique name of this constant.
	 * @throws IllegalArgumentException If the given name is already in use.
	 */
	@SuppressWarnings("unchecked")
	protected PseudoEnum(String name) throws IllegalArgumentException {
		this.name = name;
		info = (Info<T>) getInfo(getClass());
		info.writeLock.lock();
		try {
			if (info.map.containsKey(name))
				throw new IllegalArgumentException("Duplicate name '" + name + "'");
			ordinal = info.values.size();
			info.values.add((T) this);
			info.map.put(name, (T) this);
		} finally {
			info.writeLock.unlock();
		}
	}
	
	/**
	 * Returns the unique name of this constant.
	 *
	 * @return The unique name of this constant.
	 * @see Enum#name()
	 */
	public final String name() {
		return name;
	}
	
	/**
	 * Returns {@link #name()}.
	 *
	 * @return {@link #name()}
	 * @see Enum#toString()
	 */
	@Override
	public String toString() {
		return name;
	}
	
	/**
	 * Returns the unique ID of this constant. This will not be used by Yggdrasil and can thus change freely across version, in particular reordering and inserting constants is
	 * permitted.
	 *
	 * @return The unique ID of this constant.
	 * @see Enum#ordinal()
	 */
	public final int ordinal() {
		return ordinal;
	}
	
	/**
	 * Returns {@link #ordinal()}, i.e. distinct hash codes for distinct constants.
	 */
	@Override
	public final int hashCode() {
		return ordinal;
	}
	
	/**
	 * Checks for reference equality (==).
	 */
	@Override
	public final boolean equals(@Nullable Object object) {
		return object == this;
	}
	
	/**
	 * Prevents cloning of pseudo-enums. If you want to make your enums cloneable, create a <tt>(name, constantToClone)</tt> constructor.
	 *
	 * @return newer returns normally
	 * @throws CloneNotSupportedException always
	 */
	@Override
	protected final Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	/**
	 * Returns this constant's pseudo-enum class, i.e. the first non-anonymous superclass of this constant.
	 * This class is the same for all constants inheriting from a common class
	 * independently of whether they define an anonymous subclass.
	 *
	 * @return This constant's pseudo-enum class.
	 * @see Enum#getDeclaringClass()
	 */
	@SuppressWarnings("unchecked")
	public final Class<T> getDeclaringClass() {
		return (Class<T>) getDeclaringClass(getClass());
	}
	
	/**
	 * Returns the common base class for constants of the given type, i.e. the first non-anonymous superclass of <tt>type</tt>.
	 *
	 * @return The pseudo-enum class of the given class.
	 * @see Enum#getDeclaringClass()
	 */
	public static <T extends PseudoEnum<T>> Class<? super T> getDeclaringClass(Class<T> type) {
		Class<? super T> superClass = type;
		while (superClass.isAnonymousClass())
			superClass = superClass.getSuperclass();
		return superClass;
	}
	
	/**
	 * Returns all constants registered so far, ordered by their {@link #ordinal() id} (i.e. <tt>c.values()[c.ordinal()] == c</tt> is true for any constant c).
	 * <p>
	 * The returned list is a copy of the internal list at the time this method was called.
	 * <p>
	 * Please note that you
	 *
	 * @return All constants registered so far.
	 * @see Enum#valueOf(Class, String)
	 */
	public final List<T> values() {
		return values(getDeclaringClass());
	}
	
	/**
	 * Returns all constants of the given class registered so far, ordered by their {@link #ordinal() id} (i.e. <tt>type.values()[type.ordinal()] == type</tt> is true for any constant
	 * type).
	 * <p>
	 * The returned list is a copy of the internal list at the time this method was called.
	 *
	 * @return All constants registered so far.
	 * @throws IllegalArgumentException If <tt>{@link #getDeclaringClass(Class) getDeclaringClass}(type) != type</tt> (i.e. if the given class is anonymous).
	 * @see Enum#valueOf(Class, String)
	 */
	public static <T extends PseudoEnum<T>> List<T> values(Class<T> type) throws IllegalArgumentException {
		if (type != getDeclaringClass(type))
			throw new IllegalArgumentException(type + " != " + getDeclaringClass(type));
		return values(getInfo(type));
	}
	
	private static <T extends PseudoEnum<T>> List<T> values(Info<T> info) {
		info.readLock.lock();
		try {
			return new ArrayList<>(info.values);
		} finally {
			info.readLock.unlock();
		}
	}
	
	/**
	 * Returns the constant with the given ID.
	 *
	 * @param id The constant's ID
	 * @return The constant with the given ID.
	 * @throws IndexOutOfBoundsException if ID is < 0 or >= {@link #numConstants()}
	 * @see #valueOf(String)
	 */
	public final T getConstant(int id) throws IndexOutOfBoundsException {
		info.readLock.lock();
		try {
			return info.values.get(id);
		} finally {
			info.readLock.unlock();
		}
	}
	
	/**
	 * @return How many constants are currently registered
	 */
	public final int numConstants() {
		info.readLock.lock();
		try {
			return info.values.size();
		} finally {
			info.readLock.unlock();
		}
	}
	
	/**
	 * @param name The name of the constant to find
	 * @return The constant with the given name, or null if no constant with that exact name was found.
	 * @see Enum#valueOf(Class, String)
	 */
	@Nullable
	public final T valueOf(String name) {
		info.readLock.lock();
		try {
			return info.map.get(name);
		} finally {
			info.readLock.unlock();
		}
	}
	
	/**
	 * @param type The class of the constant to find
	 * @param name The name of the constant to find
	 * @return The constant with the given name, or null if no constant with that exact name was found in the given class.
	 * @see Enum#valueOf(Class, String)
	 */
	@Nullable
	public static <T extends PseudoEnum<T>> T valueOf(Class<T> type, String name) {
		Info<T> info = getInfo(type);
		info.readLock.lock();
		try {
			return info.map.get(name);
		} finally {
			info.readLock.unlock();
		}
	}
	
	private static final class Info<T extends PseudoEnum<T>> {
		
		final List<T> values = new ArrayList<>();
		final Map<String, T> map = new HashMap<>();
		
		final ReadWriteLock lock = new ReentrantReadWriteLock(true);
		final Lock readLock = lock.readLock(), writeLock = lock.writeLock();
		
	}
	
	private static final Map<Class<? extends PseudoEnum<?>>, Info<?>> infos = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	private static <T extends PseudoEnum<T>> Info<T> getInfo(Class<T> type) {
		synchronized (infos) {
			Info<T> info = (Info<T>) infos.get(getDeclaringClass(type));
			if (info == null)
				infos.put(type, info = new Info<>());
			return info;
		}
	}
	
}
