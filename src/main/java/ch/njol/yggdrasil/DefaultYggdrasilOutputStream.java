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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;

import static ch.njol.yggdrasil.Tag.*;

public final class DefaultYggdrasilOutputStream extends YggdrasilOutputStream {

	private final static Charset UTF_8 = Charset.forName("UTF-8");

	private final OutputStream out;

	private final short version;

	public DefaultYggdrasilOutputStream(final Yggdrasil y, final OutputStream out) throws IOException {
		super(y);
		this.out = out;
		version = y.version;
		writeInt(Yggdrasil.MAGIC_NUMBER);
		writeShort(version);
	}

	// private

	private void write(final int b) throws IOException {
		out.write(b);
	}

	@Override
	protected void writeTag(final Tag t) throws IOException {
		out.write(t.tag);
	}

	private final HashMap<String, Integer> writtenShortStrings = new HashMap<>();
	int nextShortStringID = 0;

	/**
	 * Writes a class ID or Field name
	 */
	private void writeShortString(final String s) throws IOException {
		if (writtenShortStrings.containsKey(s)) {
			writeTag(T_REFERENCE);
			if (version <= 1)
				writeInt(writtenShortStrings.get(s));
			else
				writeUnsignedInt(writtenShortStrings.get(s));
		} else {
			if (nextShortStringID < 0)
				throw new YggdrasilException("Too many field names/class IDs (max: " + Integer.MAX_VALUE + ")");
			final byte[] d = s.getBytes(UTF_8);
			if (d.length >= (T_REFERENCE.tag & 0xFF))
				throw new YggdrasilException("Field name or Class ID too long: " + s);
			write(d.length);
			out.write(d);
			if (d.length > 4)
				writtenShortStrings.put(s, nextShortStringID++);
		}
	}

	// Primitives

	private void writeByte(final byte b) throws IOException {
		write(b & 0xFF);
	}

	private void writeShort(final short s) throws IOException {
		write((s >>> 8) & 0xFF);
		write(s & 0xFF);
	}

	private void writeUnsignedShort(final short s) throws IOException {
		assert s >= 0;
		if (s <= 0x7f)
			writeByte((byte) (0x80 | s));
		else
			writeShort(s);
	}

	private void writeInt(final int i) throws IOException {
		write((i >>> 24) & 0xFF);
		write((i >>> 16) & 0xFF);
		write((i >>> 8) & 0xFF);
		write(i & 0xFF);
	}

	private void writeUnsignedInt(final int i) throws IOException {
		assert i >= 0;
		if (i <= 0x7FFF)
			writeShort((short) (0x8000 | i));
		else
			writeInt(i);
	}

	private void writeLong(final long l) throws IOException {
		write((int) ((l >>> 56) & 0xFF));
		write((int) ((l >>> 48) & 0xFF));
		write((int) ((l >>> 40) & 0xFF));
		write((int) ((l >>> 32) & 0xFF));
		write((int) ((l >>> 24) & 0xFF));
		write((int) ((l >>> 16) & 0xFF));
		write((int) ((l >>> 8) & 0xFF));
		write((int) (l & 0xFF));
	}

	private void writeFloat(final float f) throws IOException {
		writeInt(Float.floatToIntBits(f));
	}

	private void writeDouble(final double d) throws IOException {
		writeLong(Double.doubleToLongBits(d));
	}

	private void writeChar(final char c) throws IOException {
		writeShort((short) c);
	}

	private void writeBoolean(final boolean b) throws IOException {
		write(b ? 1 : 0);
	}

	@Override
	protected void writePrimitive_(final Object o) throws IOException {
		switch (getPrimitiveFromWrapper(o.getClass())) {
			case T_BYTE:
				writeByte((Byte) o);
				break;
			case T_SHORT:
				writeShort((Short) o);
				break;
			case T_INT:
				writeInt((Integer) o);
				break;
			case T_LONG:
				writeLong((Long) o);
				break;
			case T_FLOAT:
				writeFloat((Float) o);
				break;
			case T_DOUBLE:
				writeDouble((Double) o);
				break;
			case T_CHAR:
				writeChar((Character) o);
				break;
			case T_BOOLEAN:
				writeBoolean((Boolean) o);
				break;
			//$CASES-OMITTED$
			default:
				throw new YggdrasilException("Invalid call to writePrimitive with argument " + o);
		}
	}

	@Override
	protected void writePrimitiveValue(final Object o) throws IOException {
		writePrimitive_(o);
	}

	// String

	@Override
	protected void writeStringValue(final String s) throws IOException {
		final byte[] d = s.getBytes(UTF_8);
		writeUnsignedInt(d.length);
		out.write(d);
	}

	// Array

	@Override
	protected void writeArrayComponentType(final Class<?> componentType) throws IOException {
		writeClass_(componentType);
	}

	@Override
	protected void writeArrayLength(final int length) throws IOException {
		writeUnsignedInt(length);
	}

	@Override
	protected void writeArrayEnd() throws IOException {
	}

	// Class

	@Override
	protected void writeClassType(final Class<?> c) throws IOException {
		writeClass_(c);
	}

	private void writeClass_(Class<?> c) throws IOException {
		while (c.isArray()) {
			writeTag(T_ARRAY);
			c = c.getComponentType();
		}
		final Tag t = getType(c);
		switch (t) {
			case T_OBJECT:
			case T_ENUM:
				writeTag(t);
				writeShortString(yggdrasil.getID(c));
				break;
			case T_BOOLEAN:
			case T_BOOLEAN_OBJ:
			case T_BYTE:
			case T_BYTE_OBJ:
			case T_CHAR:
			case T_CHAR_OBJ:
			case T_DOUBLE:
			case T_DOUBLE_OBJ:
			case T_FLOAT:
			case T_FLOAT_OBJ:
			case T_INT:
			case T_INT_OBJ:
			case T_LONG:
			case T_LONG_OBJ:
			case T_SHORT:
			case T_SHORT_OBJ:
			case T_CLASS:
			case T_STRING:
				writeTag(t);
				break;
			case T_NULL:
			case T_REFERENCE:
			case T_ARRAY:
			default:
				throw new YggdrasilException("" + c.getCanonicalName());
		}
	}

	// Enum

	@Override
	protected void writeEnumType(final String type) throws IOException {
		writeShortString(type);
	}

	@Override
	protected void writeEnumID(final String id) throws IOException {
		writeShortString(id);
	}

	// generic Object

	@Override
	protected void writeObjectType(final String type) throws IOException {
		writeShortString(type);
	}

	@Override
	protected void writeNumFields(final short numFields) throws IOException {
		writeUnsignedShort(numFields);
	}

	@Override
	protected void writeFieldID(final String id) throws IOException {
		writeShortString(id);
	}

	@Override
	protected void writeObjectEnd() throws IOException {
	}

	// Reference

	@Override
	protected void writeReferenceID(final int ref) throws IOException {
		writeUnsignedInt(ref);
	}

	// stream

	@Override
	public void flush() throws IOException {
		out.flush();
	}

	@Override
	public void close() throws IOException {
		out.close();
	}

}
