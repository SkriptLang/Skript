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

import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;
import org.eclipse.jdt.annotation.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static ch.njol.yggdrasil.Tag.*;

public abstract class YggdrasilInputStream implements Closeable {

	protected final Yggdrasil yggdrasil;

	protected YggdrasilInputStream(final Yggdrasil yggdrasil) {
		this.yggdrasil = yggdrasil;
	}

	// Tag

	protected abstract Tag readTag() throws IOException;

	// Primitives

	protected abstract Object readPrimitive(Tag type) throws IOException;

	protected abstract Object readPrimitive_(Tag type) throws IOException;

	// String

	protected abstract String readString() throws IOException;

	// Array

	protected abstract Class<?> readArrayComponentType() throws IOException;

	protected abstract int readArrayLength() throws IOException;

	private final void readArrayContents(final Object array) throws IOException {
		if (array.getClass().getComponentType().isPrimitive()) {
			final int length = Array.getLength(array);
			final Tag type = getType(array.getClass().getComponentType());
			for (int i = 0; i < length; i++) {
				Array.set(array, i, readPrimitive_(type));
			}
		} else {
			for (int i = 0; i < ((Object[]) array).length; i++) {
				((Object[]) array)[i] = readObject();
			}
		}
	}

	// Enum

	protected abstract Class<?> readEnumType() throws IOException;

	protected abstract String readEnumID() throws IOException;

	@SuppressWarnings({"unchecked", "rawtypes"})
	private final Object readEnum() throws IOException {
		final Class<?> c = readEnumType();
		final String id = readEnumID();
		if (Enum.class.isAssignableFrom(c)) {
			return Yggdrasil.getEnumConstant((Class) c, id);
		} else if (PseudoEnum.class.isAssignableFrom(c)) {
			final Object o = PseudoEnum.valueOf((Class) c, id);
			if (o != null)
				return o;
//			if (YggdrasilRobustPseudoEnum.class.isAssignableFrom(c)) {
//				// TODO create this and a handler (for Enums as well)
//			}
			throw new StreamCorruptedException("Enum constant " + id + " does not exist in " + c);
		} else {
			throw new StreamCorruptedException(c + " is not an enum type");
		}
	}

	// Class

	protected abstract Class<?> readClass() throws IOException;

	// Reference

	protected abstract int readReference() throws IOException;

	// generic Object

	protected abstract Class<?> readObjectType() throws IOException;

	protected abstract short readNumFields() throws IOException;

	protected abstract String readFieldID() throws IOException;

	private final Fields readFields() throws IOException {
		final Fields fields = new Fields(yggdrasil);
		final short numFields = readNumFields();
		for (int i = 0; i < numFields; i++) {
			final String id = readFieldID();
			final Tag t = readTag();
			if (t.isPrimitive())
				fields.putPrimitive(id, readPrimitive(t));
			else
				fields.putObject(id, readObject(t));
		}
		return fields;
	}

	// any Objects

	private final List<Object> readObjects = new ArrayList<>();

	@Nullable
	public final Object readObject() throws IOException {
		final Tag t = readTag();
		return readObject(t);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public final <T> T readObject(final Class<T> expectedType) throws IOException {
		final Tag t = readTag();
		final Object o = readObject(t);
		if (o != null && !expectedType.isInstance(o))
			throw new StreamCorruptedException("Object " + o + " is of " + o.getClass() + " but expected " + expectedType);
		return (T) o;
	}

	@SuppressWarnings({"rawtypes", "unchecked", "null", "unused"})
	@Nullable
	private final Object readObject(final Tag t) throws IOException {
		if (t == T_NULL)
			return null;
		if (t == T_REFERENCE) {
			final int ref = readReference();
			if (ref < 0 || ref >= readObjects.size())
				throw new StreamCorruptedException("Invalid reference " + ref + ", " + readObjects.size() + " object(s) read so far");
			final Object o = readObjects.get(ref);
			if (o == null)
				throw new StreamCorruptedException("Reference to uninstantiable object: " + ref);
			return o;
		}
		final Object o;
		switch (t) {
			case T_ARRAY: {
				final Class<?> c = readArrayComponentType();
				o = Array.newInstance(c, readArrayLength());
				assert o != null;
				readObjects.add(o);
				readArrayContents(o);
				return o;
			}
			case T_CLASS:
				o = readClass();
				break;
			case T_ENUM:
				o = readEnum();
				break;
			case T_STRING:
				o = readString();
				break;
			case T_OBJECT: {
				final Class<?> c = readObjectType();
				final YggdrasilSerializer s = yggdrasil.getSerializer(c);
				if (s != null && !s.canBeInstantiated(c)) {
					final int ref = readObjects.size();
					readObjects.add(null);
					final Fields fields = readFields();
					o = s.deserialize(c, fields);
					if (o == null)
						throw new YggdrasilException("YggdrasilSerializer " + s + " returned null from deserialize(" + c + "," + fields + ")");
					readObjects.set(ref, o);
				} else {
					o = yggdrasil.newInstance(c);
					if (o == null)
						throw new StreamCorruptedException();
					readObjects.add(o);
					final Fields fields = readFields();
					if (s != null) {
						s.deserialize(o, fields);
					} else if (o instanceof YggdrasilExtendedSerializable) {
						((YggdrasilExtendedSerializable) o).deserialize(fields);
					} else {
						fields.setFields(o);
					}
				}
				return o;
			}
			case T_BOOLEAN_OBJ:
			case T_BYTE_OBJ:
			case T_CHAR_OBJ:
			case T_DOUBLE_OBJ:
			case T_FLOAT_OBJ:
			case T_INT_OBJ:
			case T_LONG_OBJ:
			case T_SHORT_OBJ:
				final Tag p = t.getPrimitive();
				assert p != null;
				o = readPrimitive(p);
				break;
			case T_BYTE:
			case T_BOOLEAN:
			case T_CHAR:
			case T_DOUBLE:
			case T_FLOAT:
			case T_INT:
			case T_LONG:
			case T_SHORT:
				throw new StreamCorruptedException();
			case T_REFERENCE:
			case T_NULL:
			default:
				assert false;
				throw new StreamCorruptedException();
		}
		readObjects.add(o);
		return o;
	}

//	private final static class Validation implements Comparable<Validation> {
//		private final ObjectInputValidation v;
//		private final int prio;
//		
//		public Validation(final ObjectInputValidation v, final int prio) {
//			this.v = v;
//			this.prio = prio;
//		}
//		
//		private void validate() throws InvalidObjectException {
//			v.validateObject();
//		}
//		
//		@Override
//		public int compareTo(final Validation o) {
//			return o.prio - prio;
//		}
//	}
//	
//	private final SortedSet<Validation> validations = new TreeSet<>();
//	
//	public void registerValidation(final ObjectInputValidation v, final int prio) throws NotActiveException, InvalidObjectException {
//		if (depth == 0)
//			throw new NotActiveException("stream inactive");
//		if (v == null)
//			throw new InvalidObjectException("null callback");
//		validations.add(new Validation(v, prio));
//	}
//	
//	private void validate() throws InvalidObjectException {
//		for (final Validation v : validations)
//			v.validate();
//		validations.clear(); // if multiple objects are written to the stream this method will be called multiple times
//	}

}
