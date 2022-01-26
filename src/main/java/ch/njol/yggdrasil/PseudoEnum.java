/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.yggdrasil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.concurrent.ThreadSafe;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A class that acts as a "pseudo-enum", i.e. a class which only has immutable, (public,) final static instances, which can be identified by their unique name. The instances don't
 * even have to be defined in their class, as they are registered in the constructor.
 * <p>
 * Please note that you cannot define a constant's id used for saving by annotating it with {@link YggdrasilID @YggdrasilID}, as the field(s) of the constant may not be known, and
 * furthermore a constant can be assigned to any number of fields.
 * <p>
 * This class defines methods similar to those in {@link Enum} with minor differences, e.g. {@link #values()} returns a {@link List} instead of an array.
 *
 * @author Peter Güttinger
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
	protected PseudoEnum(final String name) throws IllegalArgumentException {
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
	public final boolean equals(@Nullable final Object obj) {
		return obj == this;
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
	 * Returns this constant's pseudo-enum class, i.e. the first non-anonymous superclass of this constant. This class is the same for all constants inheriting from a common class
	 * independently from whether they define an anonymous subclass.
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
	public static <T extends PseudoEnum<T>> Class<? super T> getDeclaringClass(final Class<T> type) {
		Class<? super T> c = type;
		while (c.isAnonymousClass())
			c = c.getSuperclass();
		return c;
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
		return values(getDeclaringClass(), info);
	}
	
	/**
	 * Returns all constants of the given class registered so far, ordered by their {@link #ordinal() id} (i.e. <tt>c.values()[c.ordinal()] == c</tt> is true for any constant
	 * c).
	 * <p>
	 * The returned list is a copy of the internal list at the time this method was called.
	 *
	 * @return All constants registered so far.
	 * @throws IllegalArgumentException If <tt>{@link #getDeclaringClass(Class) getDeclaringClass}(c) != c</tt> (i.e. if the given class is anonymous).
	 * @see Enum#valueOf(Class, String)
	 */
	public static <T extends PseudoEnum<T>> List<T> values(final Class<T> c) throws IllegalArgumentException {
		if (c != getDeclaringClass(c))
			throw new IllegalArgumentException(c + " != " + getDeclaringClass(c));
		return values(c, getInfo(c));
	}
	
	private static <T extends PseudoEnum<T>> List<T> values(final Class<T> c, final Info<T> info) {
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
	@SuppressWarnings("null")
	public final T getConstant(final int id) throws IndexOutOfBoundsException {
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
	public final T valueOf(final String name) {
		info.readLock.lock();
		try {
			return info.map.get(name);
		} finally {
			info.readLock.unlock();
		}
	}
	
	/**
	 * @param c The class of the constant to find
	 * @param name The name of the constant to find
	 * @return The constant with the given name, or null if no constant with that exact name was found in the given class.
	 * @see Enum#valueOf(Class, String)
	 */
	@Nullable
	public static <T extends PseudoEnum<T>> T valueOf(final Class<T> c, final String name) {
		final Info<T> info = getInfo(c);
		info.readLock.lock();
		try {
			return info.map.get(name);
		} finally {
			info.readLock.unlock();
		}
	}
	
	@SuppressWarnings("null")
	private final static class Info<T extends PseudoEnum<T>> {
		final List<T> values = new ArrayList<>();
		final Map<String, T> map = new HashMap<>();
		
		final ReadWriteLock lock = new ReentrantReadWriteLock(true);
		final Lock readLock = lock.readLock(), writeLock = lock.writeLock();
		
		public Info() {}
	}
	
	/**
	 * Must be synchronised
	 */
	private final static Map<Class<? extends PseudoEnum<?>>, Info<?>> infos = new HashMap<>();
	
	private static <T extends PseudoEnum<T>> Info<T> getInfo(final Class<T> c) {
		synchronized (infos) {
			Info<T> info = (Info<T>) infos.get(getDeclaringClass(c));
			if (info == null)
				infos.put(c, info = new Info<>());
			return info;
		}
	}
	
}
