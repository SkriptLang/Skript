/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.yggdrasil;

import org.eclipse.jdt.annotation.Nullable;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;

/**
 * Utility to be able to save and load classes with Yggdrasil that the user has no control of, e.g. classes of an external API.
 *
 * @author Peter Güttinger
 */
public abstract class YggdrasilSerializer<T> implements ClassResolver {

	@Override
	@Nullable
	public abstract Class<? extends T> getClass(String id);

	/**
	 * Serialises the given object.
	 * <p>
	 * Use <tt>return new {@link Fields#Fields(Object) Fields}(this);</tt> to emulate the default behaviour.
	 *
	 * @param o The object to serialise
	 * @return A Fields object representing the object's fields to serialise. Must not be null.
	 * @throws NotSerializableException If this object could not be serialised
	 */
	public abstract Fields serialize(T o) throws NotSerializableException;

	/**
	 * Whether an instance of the given class can be dynamically created. If this method returns false, {@link #newInstance(Class)} and {@link #deserialize(Object, Fields)} will
	 * not be called for the given class, but {@link #deserialize(Class, Fields)} will be used instead, and having any reference to an object of the given class in its own fields'
	 * graph will cause Yggdrasil to throw an exception upon serialisation as no reference to the object will be available when deserialising the object. // TODO allow this
	 * <p>
	 * Please note that you must not change the return value of this function ever - it is not saved in the stream. // TODO clarify
	 *
	 * @param c The class to check
	 * @return true by default
	 */
	public boolean canBeInstantiated(final Class<? extends T> c) {
		return true;
	}

	/**
	 * Creates a new instance of the given class.
	 *
	 * @param c The class as read from stream
	 * @return A new instance of the given class. Must not be null if {@link #canBeInstantiated(Class)} returned true.
	 */
	@Nullable
	public abstract <E extends T> E newInstance(Class<E> c);

	/**
	 * Deserialises an object.
	 * <p>
	 * Use <tt>fields.{@link Fields#setFields(Object) setFields}(o);</tt> to emulate the default behaviour.
	 *
	 * @param o The object to deserialise as returned by {@link #newInstance(Class)}.
	 * @param fields The fields read from stream
	 * @throws StreamCorruptedException If deserialisation failed because the data read from stream is incomplete or invalid.
	 * @throws NotSerializableException
	 */
	public abstract void deserialize(T o, Fields fields) throws StreamCorruptedException, NotSerializableException;

	/**
	 * Deserialises an object.
	 *
	 * @param c The class to get an instance of
	 * @param fields The fields read from stream
	 * @return An object representing the read fields. Must not be null (throw an exception instead).
	 * @throws StreamCorruptedException If deserialisation failed because the data read from stream is incomplete or invalid.
	 * @throws NotSerializableException If the class is not serialisable
	 */
	@SuppressWarnings("unused")
	public <E extends T> E deserialize(final Class<E> c, final Fields fields) throws StreamCorruptedException, NotSerializableException {
		throw new YggdrasilException(getClass() + " does not override deserialize(Class, Fields)");
	}

}
