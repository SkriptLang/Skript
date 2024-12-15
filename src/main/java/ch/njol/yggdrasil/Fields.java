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
import org.jetbrains.annotations.Nullable;

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
	 * Holds a field's name and value, and throws {@link StreamCorruptedException}s
	 * if primitives or objects are used incorrectly.
	 */
	@NotThreadSafe
	public static final class FieldContext {
		
		final String id;
		
		/** not null if this {@link #isPrimitiveValue is a primitive} */
		@Nullable
		private Object value;
		private boolean isPrimitiveValue;
		
		FieldContext(String id) {
			this.id = id;
		}
		
		FieldContext(Field field, Object object) throws IllegalArgumentException, IllegalAccessException {
			id = Yggdrasil.getID(field);
			value = field.get(object);
			isPrimitiveValue = field.getType().isPrimitive();
		}
		
		public String getID() {
			return id;
		}
		
		public boolean isPrimitive() {
			return isPrimitiveValue;
		}
		
		@Nullable
		public Class<?> getType() {
			Object value = this.value;
			if (value == null)
				return null;
			Class<?> type = value.getClass();
			assert type != null;
			return isPrimitiveValue ? Tag.getPrimitiveFromWrapper(type).type : type;
		}
		
		@Nullable
		public Object getObject() throws StreamCorruptedException {
			if (isPrimitiveValue)
				throw new StreamCorruptedException("field " + id + " is a primitive, but expected an object");
			return value;
		}
		
		@SuppressWarnings("unchecked")
		@Nullable
		public <T> T getObject(Class<T> expectedType) throws StreamCorruptedException {
			if (isPrimitiveValue)
				throw new StreamCorruptedException("field " + id + " is a primitive, but expected " + expectedType);
			Object value = this.value;
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
		public <T> T getPrimitive(Class<T> expectedType) throws StreamCorruptedException {
			if (!isPrimitiveValue)
				throw new StreamCorruptedException("field " + id + " is not a primitive, but expected " + expectedType);
			assert expectedType.isPrimitive() || Tag.isWrapper(expectedType);
			Object value = this.value;
			assert value != null;
			if (!(expectedType.isPrimitive() ? Tag.getWrapperClass(expectedType).isInstance(value) : expectedType.isInstance(value)))
				throw new StreamCorruptedException("Field " + id + " of " + value.getClass() + ", but expected " + expectedType);
			return (T) value;
		}
		
		public void setObject(@Nullable Object value) {
			this.value = value;
			isPrimitiveValue = false;
		}
		
		public void setPrimitive(Object value) {
			assert Tag.isWrapper(value.getClass());
			this.value = value;
			isPrimitiveValue = true;
		}
		
		public void setField(Object object, Field field, Yggdrasil yggdrasil) throws StreamCorruptedException {
			if (Modifier.isStatic(field.getModifiers()))
				throw new StreamCorruptedException("The field " + id + " of " + field.getDeclaringClass() + " is static");
			if (Modifier.isTransient(field.getModifiers()))
				throw new StreamCorruptedException("The field " + id + " of " + field.getDeclaringClass() + " is transient");
			if (field.getType().isPrimitive() != isPrimitiveValue)
				throw new StreamCorruptedException("The field " + id + " of " + field.getDeclaringClass() + " is " + (field.getType().isPrimitive() ? "" : "not ") + "primitive");
			try {
				field.setAccessible(true);
				field.set(object, value);
			} catch (IllegalArgumentException e) {
				if (!(object instanceof YggdrasilRobustSerializable) || !((YggdrasilRobustSerializable) object).incompatibleField(field, this))
					yggdrasil.incompatibleField(object, field, this);
			} catch (IllegalAccessException e) {
				assert false;
			}
		}
		
		@Override
		public int hashCode() {
			return id.hashCode();
		}
		
		@Override
		public boolean equals(@Nullable Object object) {
			if (this == object)
				return true;
			if (object == null)
				return false;
			if (!(object instanceof FieldContext))
				return false;
			FieldContext other = (FieldContext) object;
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
	
	public Fields(Yggdrasil yggdrasil) {
		this.yggdrasil = yggdrasil;
	}
	
	/**
	 * Creates a fields object and initialises it with all non-transient and
	 * non-static fields of the given class and its superclasses.
	 * 
	 * @param type Some class
	 * @throws NotSerializableException If a field occurs more than once (i.e. if a class has a
	 *                                  field with the same name as a field in one of its superclasses)
	 */
	public Fields(Class<?> type, Yggdrasil yggdrasil) throws NotSerializableException {
		this.yggdrasil = yggdrasil;
		for (Field field : getFields(type)) {
			assert field != null;
			String id = Yggdrasil.getID(field);
			fields.put(id, new FieldContext(id));
		}
	}
	
	/**
	 * Creates a fields object and initialises it with all non-transient
	 * and non-static fields of the given object.
	 * 
	 * @param object Some object
	 * @throws NotSerializableException If a field occurs more than once (i.e. if a class
	 *                                  has a field with the same name as a field in one of its superclasses)
	 */
	public Fields(Object object) throws NotSerializableException {
		this(object, null);
	}
	
	/**
	 * Creates a fields object and initialises it with all non-transient
	 * and non-static fields of the given object.
	 * 
	 * @param object Some object
	 * @throws NotSerializableException If a field occurs more than once (i.e. if a class
	 *                                  has a field with the same name as a field in one of its superclasses)
	 */
	public Fields(Object object, @Nullable Yggdrasil yggdrasil) throws NotSerializableException {
		this.yggdrasil = yggdrasil;
		Class<?> type = object.getClass();
		assert type != null;
		for (Field field : getFields(type)) {
			assert field != null;
			try {
				fields.put(Yggdrasil.getID(field), new FieldContext(field, object));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				assert false;
			}
		}
	}
	
	private static final Map<Class<?>, Collection<Field>> cache = new HashMap<>();
	
	/**
	 * Gets all serializable fields of the provided class, including superclasses.
	 * 
	 * @param type The class to get the fields of
	 * @return All non-static and non-transient fields of the given class and its superclasses
	 * @throws NotSerializableException If a field occurs more than once (i.e. if a class has a
	 *                                  field with the same name as a field in one of its superclasses)
	 */
	public static Collection<Field> getFields(Class<?> type) throws NotSerializableException {
		Collection<Field> fields = cache.get(type);
		if (fields != null)
			return fields;
		fields = new ArrayList<>();
		Set<String> ids = new HashSet<>();
		for (Class<?> superClass = type; superClass != null; superClass = superClass.getSuperclass()) {
			Field[] declaredFields = superClass.getDeclaredFields();
			for (Field field : declaredFields) {
				int modifiers = field.getModifiers();
				if (field.isSynthetic() || Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers))
					continue;
				String id = Yggdrasil.getID(field);
				if (ids.contains(id))
					throw new NotSerializableException(type + "/" + superClass + ": duplicate field id '" + id + "'");
				field.setAccessible(true);
				fields.add(field);
				ids.add(id);
			}
		}
		fields = Collections.unmodifiableCollection(fields);
		cache.put(type, fields);
		return fields;
	}
	
	/**
	 * Sets all fields of the given Object to the values stored in this Fields object.
	 * 
	 * @param object The object whose fields should be set
	 * @throws YggdrasilException If this was called on a Fields object not created by Yggdrasil itself
	 */
	public void setFields(Object object) throws StreamCorruptedException, NotSerializableException {
		Yggdrasil yggdrasil = this.yggdrasil;
		if (yggdrasil == null)
			throw new YggdrasilException("");
		Set<FieldContext> excessive = new HashSet<>(fields.values());
		Class<?> type = object.getClass();
		assert type != null;
		for (Field field : getFields(type)) {
			assert field != null;
			String id = Yggdrasil.getID(field);
			FieldContext context = fields.get(id);
			if (context == null) {
				if (!(object instanceof YggdrasilRobustSerializable) || !((YggdrasilRobustSerializable) object).missingField(field))
					yggdrasil.missingField(object, field);
			} else {
				context.setField(object, field, yggdrasil);
			}
			excessive.remove(context);
		}
		for (FieldContext context : excessive) {
			assert context != null;
			if (!(object instanceof YggdrasilRobustSerializable) || !((YggdrasilRobustSerializable) object).excessiveField(context))
				yggdrasil.excessiveField(object, context);
		}
	}
	
	@Deprecated
	public void setFields(Object object, Yggdrasil yggdrasil) throws StreamCorruptedException, NotSerializableException {
		assert this.yggdrasil == yggdrasil;
		setFields(object);
	}
	
	/**
	 * @return The number of fields defined
	 */
	public int size() {
		return fields.size();
	}
	
	public void putObject(String fieldID, @Nullable Object value) {
		FieldContext context = fields.get(fieldID);
		if (context == null)
			fields.put(fieldID, context = new FieldContext(fieldID));
		context.setObject(value);
	}
	
	public void putPrimitive(String fieldID, Object value) {
		FieldContext context = fields.get(fieldID);
		if (context == null)
			fields.put(fieldID, context = new FieldContext(fieldID));
		context.setPrimitive(value);
	}
	
	/**
	 * @param fieldID A field's id
	 * @return Whether the field is defined
	 */
	public boolean contains(String fieldID) {
		return fields.containsKey(fieldID);
	}
	
	public boolean hasField(String fieldID) {
	    return fields.containsKey(fieldID);
	}
	
	@Nullable
	public Object getObject(String field) throws StreamCorruptedException {
		FieldContext context = fields.get(field);
		if (context == null)
			throw new StreamCorruptedException("Nonexistent field " + field);
		return context.getObject();
	}
	
	@Nullable
	public <T> T getObject(String fieldID, Class<T> expectedType) throws StreamCorruptedException {
		assert !expectedType.isPrimitive();
		FieldContext context = fields.get(fieldID);
		if (context == null)
			throw new StreamCorruptedException("Nonexistent field " + fieldID);
		return context.getObject(expectedType);
	}
	
	public Object getPrimitive(String fieldID) throws StreamCorruptedException {
		FieldContext context = fields.get(fieldID);
		if (context == null)
			throw new StreamCorruptedException("Nonexistent field " + fieldID);
		return context.getPrimitive();
	}
	
	public <T> T getPrimitive(String fieldID, Class<T> expectedType) throws StreamCorruptedException {
		assert expectedType.isPrimitive() || Tag.getPrimitiveFromWrapper(expectedType).isPrimitive();
		FieldContext context = fields.get(fieldID);
		if (context == null)
			throw new StreamCorruptedException("Nonexistent field " + fieldID);
		return context.getPrimitive(expectedType);
	}
	
	@Nullable
	public <T> T getAndRemoveObject(String field, Class<T> expectedType) throws StreamCorruptedException {
		T object = getObject(field, expectedType);
		removeField(field);
		return object;
	}
	
	public <T> T getAndRemovePrimitive(String field, Class<T> expectedType) throws StreamCorruptedException {
		T object = getPrimitive(field, expectedType);
		removeField(field);
		return object;
	}
	
	/**
	 * Removes a field and its value from this Fields object.
	 * 
	 * @param fieldID The id of the field to remove
	 * @return Whether a field with the given name was actually defined
	 */
	public boolean removeField(String fieldID) {
		return fields.remove(fieldID) != null;
	}
	
	@SuppressWarnings("null")
	@Override
	public Iterator<FieldContext> iterator() {
		return fields.values().iterator();
	}
	
}
