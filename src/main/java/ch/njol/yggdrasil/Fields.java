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

import ch.njol.yggdrasil.Fields.FieldContext;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilRobustSerializable;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@NotThreadSafe
public final class Fields implements Iterable<FieldContext> {
	
	/**
	 * Holds a field's name and value, and throws {@link StreamCorruptedException}s if primitives or objects are used incorrectly.
	 * 
	 * @author Peter Güttinger
	 */
	@NotThreadSafe
	public final static class FieldContext {
		
		final String id;
		
		/** not null if this {@link #isPrimitiveValue is a primitive} */
		@Nullable
		private Object value;
		
		private boolean isPrimitiveValue;
		
		FieldContext(final String id) {
			this.id = id;
		}
		
		FieldContext(final Field f, final Object o) throws IllegalArgumentException, IllegalAccessException {
			id = Yggdrasil.getID(f);
			value = f.get(o);
			isPrimitiveValue = f.getType().isPrimitive();
		}
		
		public String getID() {
			return id;
		}
		
		public boolean isPrimitive() {
			return isPrimitiveValue;
		}
		
		@Nullable
		public Class<?> getType() {
			final Object value = this.value;
			if (value == null)
				return null;
			final Class<?> c = value.getClass();
			assert c != null;
			return isPrimitiveValue ? Tag.getPrimitiveFromWrapper(c).c : c;
		}
		
		@Nullable
		public Object getObject() throws StreamCorruptedException {
			if (isPrimitiveValue)
				throw new StreamCorruptedException("field " + id + " is a primitive, but expected an object");
			return value;
		}
		
		@SuppressWarnings("unchecked")
		@Nullable
		public <T> T getObject(final Class<T> expectedType) throws StreamCorruptedException {
			if (isPrimitiveValue)
				throw new StreamCorruptedException("field " + id + " is a primitive, but expected " + expectedType);
			final Object value = this.value;
			if (value != null && !expectedType.isInstance(value))
				throw new StreamCorruptedException("Field " + id + " of " + value.getClass() + ", but expected " + expectedType);
			return (T) value;
		}
		
		public Object getPrimitive() throws StreamCorruptedException {
			if (!isPrimitiveValue)
				throw new StreamCorruptedException("field " + id + " is not a primitive, but expected one");
			assert value != null;
			return value;
		}
		
		@SuppressWarnings("unchecked")
		public <T> T getPrimitive(final Class<T> expectedType) throws StreamCorruptedException {
			if (!isPrimitiveValue)
				throw new StreamCorruptedException("field " + id + " is not a primitive, but expected " + expectedType);
			assert expectedType.isPrimitive() || Tag.isWrapper(expectedType);
			final Object value = this.value;
			assert value != null;
			if (!(expectedType.isPrimitive() ? Tag.getWrapperClass(expectedType).isInstance(value) : expectedType.isInstance(value)))
				throw new StreamCorruptedException("Field " + id + " of " + value.getClass() + ", but expected " + expectedType);
			return (T) value;
		}
		
		public void setObject(final @Nullable Object value) {
			this.value = value;
			isPrimitiveValue = false;
		}
		
		public void setPrimitive(final Object value) {
			assert value != null && Tag.isWrapper(value.getClass());
			this.value = value;
			isPrimitiveValue = true;
		}
		
		public void setField(final Object o, final Field f, final Yggdrasil y) throws StreamCorruptedException {
			if (Modifier.isStatic(f.getModifiers()))
				throw new StreamCorruptedException("The field " + id + " of " + f.getDeclaringClass() + " is static");
			if (Modifier.isTransient(f.getModifiers()))
				throw new StreamCorruptedException("The field " + id + " of " + f.getDeclaringClass() + " is transient");
			if (f.getType().isPrimitive() != isPrimitiveValue)
				throw new StreamCorruptedException("The field " + id + " of " + f.getDeclaringClass() + " is " + (f.getType().isPrimitive() ? "" : "not ") + "primitive");
			try {
				f.setAccessible(true);
				f.set(o, value);
			} catch (final IllegalArgumentException e) {
				if (!(o instanceof YggdrasilRobustSerializable) || !((YggdrasilRobustSerializable) o).incompatibleField(f, this))
					y.incompatibleField(o, f, this);
			} catch (final IllegalAccessException e) {
				assert false;
			}
		}
		
		@Override
		public int hashCode() {
			return id.hashCode();
		}
		
		@Override
		public boolean equals(final @Nullable Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof FieldContext))
				return false;
			final FieldContext other = (FieldContext) obj;
			return id.equals(other.id);
		}
		
	}
	
	@Nullable
	private final Yggdrasil yggdrasil;
	
	private final Map<String, FieldContext> fields = new HashMap<>();
	
	/**
	 * Creates an empty Fields object.
	 */
	public Fields() {
		yggdrasil = null;
	}
	
	public Fields(final @Nullable Yggdrasil yggdrasil) {
		this.yggdrasil = yggdrasil;
	}
	
	/**
	 * Creates a fields object and initialises it with all non-transient and non-static fields of the given class and its superclasses.
	 * 
	 * @param c Some class
	 * @throws NotSerializableException If a field occurs more than once (i.e. if a class has a field with the same name as a field in one of its superclasses)
	 */
	public Fields(final Class<?> c, final @Nullable Yggdrasil yggdrasil) throws NotSerializableException {
		this.yggdrasil = yggdrasil;
		for (final Field f : getFields(c)) {
			assert f != null;
			final String id = Yggdrasil.getID(f);
			fields.put(id, new FieldContext(id));
		}
	}
	
	/**
	 * Creates a fields object and initialises it with all non-transient and non-static fields of the given object.
	 * 
	 * @param o Some object
	 * @throws NotSerializableException If a field occurs more than once (i.e. if a class has a field with the same name as a field in one of its superclasses)
	 */
	public Fields(final Object o) throws NotSerializableException {
		this(o, null);
	}
	
	/**
	 * Creates a fields object and initialises it with all non-transient and non-static fields of the given object.
	 * 
	 * @param o Some object
	 * @throws NotSerializableException If a field occurs more than once (i.e. if a class has a field with the same name as a field in one of its superclasses)
	 */
	public Fields(final Object o, @Nullable final Yggdrasil yggdrasil) throws NotSerializableException {
		this.yggdrasil = yggdrasil;
		final Class<?> c = o.getClass();
		assert c != null;
		for (final Field f : getFields(c)) {
			assert f != null;
			try {
				fields.put(Yggdrasil.getID(f), new FieldContext(f, o));
			} catch (final IllegalArgumentException e) {
				assert false;
			} catch (final IllegalAccessException e) {
				assert false;
			}
		}
	}
	
	private final static Map<Class<?>, Collection<Field>> cache = new HashMap<>();
	
	/**
	 * Gets all serialisable fields of the provided class, including superclasses.
	 * 
	 * @param c The class to get the fields of
	 * @return All non-static and non-transient fields of the given class and its superclasses
	 * @throws NotSerializableException If a field occurs more than once (i.e. if a class has a field with the same name as a field in one of its superclasses)
	 */
	public static Collection<Field> getFields(final Class<?> c) throws NotSerializableException {
		Collection<Field> fields = cache.get(c);
		if (fields != null)
			return fields;
		fields = new ArrayList<>();
		final Set<String> ids = new HashSet<>();
		for (Class<?> sc = c; sc != null; sc = sc.getSuperclass()) {
			final Field[] fs = sc.getDeclaredFields();
			for (final Field f : fs) {
				final int m = f.getModifiers();
				if (Modifier.isStatic(m) || Modifier.isTransient(m))
					continue;
				final String id = Yggdrasil.getID(f);
				if (ids.contains(id))
					throw new NotSerializableException(c + "/" + sc + ": duplicate field id '" + id + "'");
				f.setAccessible(true);
				fields.add(f);
				ids.add(id);
			}
		}
		fields = Collections.unmodifiableCollection(fields);
		assert fields != null;
		cache.put(c, fields);
		return fields;
	}
	
	/**
	 * Sets all fields of the given Object to the values stored in this Fields object.
	 * 
	 * @param o The object whose fields should be set
	 * @throws StreamCorruptedException
	 * @throws NotSerializableException
	 * @throws YggdrasilException If this was called on a Fields object not created by Yggdrasil itself
	 */
	public void setFields(final Object o) throws StreamCorruptedException, NotSerializableException {
		final Yggdrasil y = yggdrasil;
		if (y == null)
			throw new YggdrasilException("");
		final Set<FieldContext> excessive = new HashSet<>(fields.values());
		final Class<?> oc = o.getClass();
		assert oc != null;
		for (final Field f : getFields(oc)) {
			assert f != null;
			final String id = Yggdrasil.getID(f);
			final FieldContext c = fields.get(id);
			if (c == null) {
				if (!(o instanceof YggdrasilRobustSerializable) || !((YggdrasilRobustSerializable) o).missingField(f))
					y.missingField(o, f);
			} else {
				c.setField(o, f, y);
			}
			excessive.remove(c);
		}
		for (final FieldContext f : excessive) {
			assert f != null;
			if (!(o instanceof YggdrasilRobustSerializable) || !((YggdrasilRobustSerializable) o).excessiveField(f))
				y.excessiveField(o, f);
		}
	}
	
	@Deprecated
	public void setFields(final Object o, final Yggdrasil y) throws StreamCorruptedException, NotSerializableException {
		assert yggdrasil == y;
		setFields(o);
	}
	
	/**
	 * @return The number of fields defined
	 */
	public int size() {
		return fields.size();
	}
	
	public void putObject(final String fieldID, final @Nullable Object value) {
		FieldContext c = fields.get(fieldID);
		if (c == null)
			fields.put(fieldID, c = new FieldContext(fieldID));
		c.setObject(value);
	}
	
	public void putPrimitive(final String fieldID, final Object value) {
		FieldContext c = fields.get(fieldID);
		if (c == null)
			fields.put(fieldID, c = new FieldContext(fieldID));
		c.setPrimitive(value);
	}
	
	/**
	 * @param fieldID A field's id
	 * @return Whether the field is defined
	 */
	public boolean contains(final String fieldID) {
		return fields.containsKey(fieldID);
	}
	
	public boolean hasField(String fieldID) {
	    return this.fields.containsKey(fieldID);
	}
	
	@Nullable
	public Object getObject(final String field) throws StreamCorruptedException {
		final FieldContext c = fields.get(field);
		if (c == null)
			throw new StreamCorruptedException("Nonexistent field " + field);
		return c.getObject();
	}
	
	@Nullable
	public <T> T getObject(final String fieldID, final Class<T> expectedType) throws StreamCorruptedException {
		assert !expectedType.isPrimitive();
		final FieldContext c = fields.get(fieldID);
		if (c == null)
			throw new StreamCorruptedException("Nonexistent field " + fieldID);
		return c.getObject(expectedType);
	}
	
	public Object getPrimitive(final String fieldID) throws StreamCorruptedException {
		final FieldContext c = fields.get(fieldID);
		if (c == null)
			throw new StreamCorruptedException("Nonexistent field " + fieldID);
		return c.getPrimitive();
	}
	
	public <T> T getPrimitive(final String fieldID, final Class<T> expectedType) throws StreamCorruptedException {
		assert expectedType.isPrimitive() || Tag.getPrimitiveFromWrapper(expectedType).isPrimitive();
		final FieldContext c = fields.get(fieldID);
		if (c == null)
			throw new StreamCorruptedException("Nonexistent field " + fieldID);
		return c.getPrimitive(expectedType);
	}
	
	@Nullable
	public <T> T getAndRemoveObject(final String field, final Class<T> expectedType) throws StreamCorruptedException {
		final T t = getObject(field, expectedType);
		removeField(field);
		return t;
	}
	
	public <T> T getAndRemovePrimitive(final String field, final Class<T> expectedType) throws StreamCorruptedException {
		final T t = getPrimitive(field, expectedType);
		removeField(field);
		return t;
	}
	
	/**
	 * Removes a field and its value from this Fields object.
	 * 
	 * @param fieldID The id of the field to remove
	 * @return Whether a field with the given name was actually defined
	 */
	public boolean removeField(final String fieldID) {
		return fields.remove(fieldID) != null;
	}
	
	@SuppressWarnings("null")
	@Override
	public @NotNull Iterator<FieldContext> iterator() {
		return fields.values().iterator();
	}
	
}
