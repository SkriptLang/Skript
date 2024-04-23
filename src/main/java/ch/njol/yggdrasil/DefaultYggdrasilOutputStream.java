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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static ch.njol.yggdrasil.Tag.T_ARRAY;
import static ch.njol.yggdrasil.Tag.T_REFERENCE;
import static ch.njol.yggdrasil.Tag.getPrimitiveFromWrapper;
import static ch.njol.yggdrasil.Tag.getType;

public final class DefaultYggdrasilOutputStream extends YggdrasilOutputStream {
	
	private final OutputStream out;
	private final short version;
	
	public DefaultYggdrasilOutputStream(Yggdrasil yggdrasil, OutputStream out) throws IOException {
		super(yggdrasil);
		this.out = out;
		version = yggdrasil.version;
		writeInt(Yggdrasil.MAGIC_NUMBER);
		writeShort(version);
	}
	
	private void write(int b) throws IOException {
		out.write(b);
	}
	
	@Override
	protected void writeTag(Tag tag) throws IOException {
		out.write(tag.tag);
	}
	
	private final Map<String, Integer> writtenShortStrings = new HashMap<>();
	int nextShortStringID = 0;
	
	/**
	 * Writes a class ID or Field name
	 */
	private void writeShortString(String string) throws IOException {
		if (writtenShortStrings.containsKey(string)) {
			writeTag(T_REFERENCE);
			if (version <= 1)
				writeInt(writtenShortStrings.get(string));
			else
				writeUnsignedInt(writtenShortStrings.get(string));
		} else {
			if (nextShortStringID < 0)
				throw new YggdrasilException("Too many field names/class IDs (max: " + Integer.MAX_VALUE + ")");
			byte[] d = string.getBytes(StandardCharsets.UTF_8);
			if (d.length >= (T_REFERENCE.tag & 0xFF))
				throw new YggdrasilException("Field name or Class ID too long: " + string);
			write(d.length);
			out.write(d);
			if (d.length > 4)
				writtenShortStrings.put(string, nextShortStringID++);
		}
	}
	
	private void writeByte(byte b) throws IOException {
		write(b & 0xFF);
	}
	
	private void writeShort(short s) throws IOException {
		write((s >>> 8) & 0xFF);
		write(s & 0xFF);
	}
	
	private void writeUnsignedShort(short s) throws IOException {
		assert s >= 0;
		if (s <= 0x7f)
			writeByte((byte) (0x80 | s));
		else
			writeShort(s);
	}
	
	private void writeInt(int i) throws IOException {
		write((i >>> 24) & 0xFF);
		write((i >>> 16) & 0xFF);
		write((i >>> 8) & 0xFF);
		write(i & 0xFF);
	}
	
	private void writeUnsignedInt(int i) throws IOException {
		assert i >= 0;
		if (i <= 0x7FFF)
			writeShort((short) (0x8000 | i));
		else
			writeInt(i);
	}
	
	private void writeLong(long l) throws IOException {
		write((int) ((l >>> 56) & 0xFF));
		write((int) ((l >>> 48) & 0xFF));
		write((int) ((l >>> 40) & 0xFF));
		write((int) ((l >>> 32) & 0xFF));
		write((int) ((l >>> 24) & 0xFF));
		write((int) ((l >>> 16) & 0xFF));
		write((int) ((l >>> 8) & 0xFF));
		write((int) (l & 0xFF));
	}
	
	private void writeFloat(float f) throws IOException {
		writeInt(Float.floatToIntBits(f));
	}
	
	private void writeDouble(double d) throws IOException {
		writeLong(Double.doubleToLongBits(d));
	}
	
	private void writeChar(char c) throws IOException {
		writeShort((short) c);
	}
	
	private void writeBoolean(boolean b) throws IOException {
		write(b ? 1 : 0);
	}
	
	@Override
	protected void writePrimitive_(Object object) throws IOException {
		switch (getPrimitiveFromWrapper(object.getClass())) {
			case T_BYTE:
				writeByte((Byte) object);
				break;
			case T_SHORT:
				writeShort((Short) object);
				break;
			case T_INT:
				writeInt((Integer) object);
				break;
			case T_LONG:
				writeLong((Long) object);
				break;
			case T_FLOAT:
				writeFloat((Float) object);
				break;
			case T_DOUBLE:
				writeDouble((Double) object);
				break;
			case T_CHAR:
				writeChar((Character) object);
				break;
			case T_BOOLEAN:
				writeBoolean((Boolean) object);
				break;
			//$CASES-OMITTED$
			default:
				throw new YggdrasilException("Invalid call to writePrimitive with argument " + object);
		}
	}
	
	@Override
	protected void writePrimitiveValue(Object object) throws IOException {
		writePrimitive_(object);
	}
	
	@Override
	protected void writeStringValue(String string) throws IOException {
		byte[] d = string.getBytes(StandardCharsets.UTF_8);
		writeUnsignedInt(d.length);
		out.write(d);
	}
	
	@Override
	protected void writeArrayComponentType(Class<?> componentType) throws IOException {
		writeClass_(componentType);
	}
	
	@Override
	protected void writeArrayLength(int length) throws IOException {
		writeUnsignedInt(length);
	}
	
	@Override
	protected void writeArrayEnd() {}
	
	@Override
	protected void writeClassType(Class<?> type) throws IOException {
		writeClass_(type);
	}
	
	private void writeClass_(Class<?> type) throws IOException {
		while (type.isArray()) {
			writeTag(T_ARRAY);
			type = type.getComponentType();
		}
		Tag tag = getType(type);
		switch (tag) {
			case T_OBJECT:
			case T_ENUM:
				writeTag(tag);
				writeShortString(yggdrasil.getID(type));
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
				writeTag(tag);
				break;
			case T_NULL:
			case T_REFERENCE:
			case T_ARRAY:
			default:
				throw new YggdrasilException("" + type.getCanonicalName());
		}
	}
	
	@Override
	protected void writeEnumType(String type) throws IOException {
		writeShortString(type);
	}
	
	@Override
	protected void writeEnumID(String id) throws IOException {
		writeShortString(id);
	}
	
	@Override
	protected void writeObjectType(String type) throws IOException {
		writeShortString(type);
	}
	
	@Override
	protected void writeNumFields(short numFields) throws IOException {
		writeUnsignedShort(numFields);
	}
	
	@Override
	protected void writeFieldID(String id) throws IOException {
		writeShortString(id);
	}
	
	@Override
	protected void writeObjectEnd() {}
	
	@Override
	protected void writeReferenceID(int reference) throws IOException {
		writeUnsignedInt(reference);
	}
	
	@Override
	public void flush() throws IOException {
		out.flush();
	}
	
	@Override
	public void close() throws IOException {
		out.close();
	}
	
}
