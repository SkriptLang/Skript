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

import ch.njol.yggdrasil.Fields.FieldContext;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;
import org.eclipse.jdt.annotation.Nullable;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.NotSerializableException;
import java.lang.reflect.Array;
import java.util.IdentityHashMap;

import static ch.njol.yggdrasil.Tag.*;

public abstract class YggdrasilOutputStream implements Flushable, Closeable {

	protected final Yggdrasil yggdrasil;

	protected YggdrasilOutputStream(final Yggdrasil yggdrasil) {
		this.yggdrasil = yggdrasil;
	}

	// Tag

	protected abstract void writeTag(Tag t) throws IOException;

	// Null

	private final void writeNull() throws IOException {
		writeTag(T_NULL);
	}

	// Primitives

	protected abstract void writePrimitiveValue(Object o) throws IOException;

	protected abstract void writePrimitive_(Object o) throws IOException;

	private final void writePrimitive(final Object o) throws IOException {
		final Tag t = Tag.getType(o.getClass());
		assert t.isWrapper();
		final Tag p = t.getPrimitive();
		assert p != null;
		writeTag(p);
		writePrimitiveValue(o);
	}

	private final void writeWrappedPrimitive(final Object o) throws IOException {
		final Tag t = Tag.getType(o.getClass());
		assert t.isWrapper();
		writeTag(t);
		writePrimitiveValue(o);
	}

	// String

	protected abstract void writeStringValue(String s) throws IOException;

	private final void writeString(final String s) throws IOException {
		writeTag(T_STRING);
		writeStringValue(s);
	}

	// Array

	protected abstract void writeArrayComponentType(Class<?> componentType) throws IOException;

	protected abstract void writeArrayLength(int length) throws IOException;

	protected abstract void writeArrayEnd() throws IOException;

	private final void writeArray(final Object array) throws IOException {
		final int length = Array.getLength(array);
		final Class<?> ct = array.getClass().getComponentType();
		assert ct != null;
		writeTag(T_ARRAY);
		writeArrayComponentType(ct);
		writeArrayLength(length);
		if (ct.isPrimitive()) {
			for (int i = 0; i < length; i++) {
				final Object p = Array.get(array, i);
				assert p != null;
				writePrimitive_(p);
			}
			writeArrayEnd();
		} else {
			for (final Object o : (Object[]) array)
				writeObject(o);
			writeArrayEnd();
		}
	}

	// Enum

	protected abstract void writeEnumType(String type) throws IOException;

	protected abstract void writeEnumID(String id) throws IOException;

	private final void writeEnum(final Enum<?> o) throws IOException {
		writeTag(T_ENUM);
		final Class<?> c = o.getDeclaringClass();
		assert c != null;
		writeEnumType(yggdrasil.getID(c));
		writeEnumID(Yggdrasil.getID(o));
	}

	private final void writeEnum(final PseudoEnum<?> o) throws IOException {
		writeTag(T_ENUM);
		writeEnumType(yggdrasil.getID(o.getDeclaringClass()));
		writeEnumID(o.name());
	}

	// Class

	protected abstract void writeClassType(Class<?> c) throws IOException;

	private final void writeClass(final Class<?> c) throws IOException {
		writeTag(T_CLASS);
		writeClassType(c);
	}

	// Reference

	protected abstract void writeReferenceID(int ref) throws IOException;

	protected final void writeReference(final int ref) throws IOException {
		assert ref >= 0;
		writeTag(T_REFERENCE);
		writeReferenceID(ref);
	}

	// generic Objects

	protected abstract void writeObjectType(String type) throws IOException;

	protected abstract void writeNumFields(short numFields) throws IOException;

	protected abstract void writeFieldID(String id) throws IOException;

	protected abstract void writeObjectEnd() throws IOException;

	@SuppressWarnings({"rawtypes", "unchecked"})
	private final void writeGenericObject(final Object o, int ref) throws IOException {
		final Class<?> c = o.getClass();
		assert c != null;
		if (!yggdrasil.isSerializable(c))
			throw new NotSerializableException(c.getName());
		final Fields fields;
		final YggdrasilSerializer s = yggdrasil.getSerializer(c);
		if (s != null) {
			fields = s.serialize(o);
			if (!s.canBeInstantiated(c)) {
				ref = ~ref; // ~ instead of - to also get a negative value if ref is 0
				writtenObjects.put(o, ref);
			}
		} else if (o instanceof YggdrasilExtendedSerializable) {
			fields = ((YggdrasilExtendedSerializable) o).serialize();
		} else {
			fields = new Fields(o, yggdrasil);
		}
		if (fields.size() > Short.MAX_VALUE)
			throw new YggdrasilException("Class " + c.getCanonicalName() + " has too many fields (" + fields.size() + ")");

		writeTag(T_OBJECT);
		writeObjectType(yggdrasil.getID(c));
		writeNumFields((short) fields.size());
		for (final FieldContext f : fields) {
			writeFieldID(f.id);
			if (f.isPrimitive())
				writePrimitive(f.getPrimitive());
			else
				writeObject(f.getObject());
		}
		writeObjectEnd();

		if (ref < 0)
			writtenObjects.put(o, ~ref);
	}

	// any Objects

	private int nextObjectID = 0;
	private final IdentityHashMap<Object, Integer> writtenObjects = new IdentityHashMap<>();

	public final void writeObject(final @Nullable Object o) throws IOException {
		if (o == null) {
			writeNull();
			return;
		}
		if (writtenObjects.containsKey(o)) {
			final int ref = writtenObjects.get(o);
			if (ref < 0)
				throw new YggdrasilException("Uninstantiable object " + o + " is referenced in its fields' graph");
			writeReference(ref);
			return;
		}
		final int ref = nextObjectID;
		nextObjectID++;
		writtenObjects.put(o, ref);
		final Tag type = getType(o.getClass());
		if (type.isWrapper()) {
			writeWrappedPrimitive(o);
			return;
		}
		switch (type) {
			case T_ARRAY:
				writeArray(o);
				return;
			case T_STRING:
				writeString((String) o);
				return;
			case T_ENUM:
				if (o instanceof Enum)
					writeEnum((Enum<?>) o);
				else
					writeEnum((PseudoEnum<?>) o);
				return;
			case T_CLASS:
				writeClass((Class<?>) o);
				return;
			case T_OBJECT:
				writeGenericObject(o, ref);
				return;
			//$CASES-OMITTED$
			default:
				throw new YggdrasilException("unhandled type " + type);
		}
	}

}
