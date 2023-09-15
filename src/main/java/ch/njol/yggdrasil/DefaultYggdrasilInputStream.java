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

import org.eclipse.jdt.annotation.NonNull;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static ch.njol.yggdrasil.Tag.T_ARRAY;
import static ch.njol.yggdrasil.Tag.T_REFERENCE;

//Naming conventions:
// x(): read info & data (e.g. content type, contents) [i.e. no tag]
// _x(): read data only (e.g. contents)

public final class DefaultYggdrasilInputStream extends YggdrasilInputStream {

	@SuppressWarnings("null")
	private final static Charset UTF_8 = Charset.forName("UTF-8");

	private final short version;

	final InputStream in;

	public DefaultYggdrasilInputStream(final Yggdrasil y, final InputStream in) throws IOException {
		super(y);
		this.in = in;
		final int m = readInt();
		if (m != Yggdrasil.MAGIC_NUMBER)
			throw new StreamCorruptedException("Not an Yggdrasil stream");
		version = readShort();
		if (version <= 0 || version > Yggdrasil.LATEST_VERSION)
			throw new StreamCorruptedException("Input was saved using a later version of Yggdrasil");
	}

	// private

	/**
	 * @throws EOFException If the end of the stream is reached
	 */
	private int read() throws IOException {
		final int b = in.read();
		if (b < 0)
			throw new EOFException();
		return b;
	}

	private void readFully(final byte[] buf) throws IOException {
		readFully(buf, 0, buf.length);
	}

	private void readFully(final byte[] buf, int off, final int len) throws IOException {
		int l = len;
		while (l > 0) {
			final int n = in.read(buf, off, l);
			if (n < 0)
				throw new EOFException("Expected " + len + " bytes, but could only read " + (len - l));
			off += n;
			l -= n;
		}
	}

	private final List<String> readShortStrings = new ArrayList<>();

	private String readShortString() throws IOException {
		final int length = read();
		if (length == (T_REFERENCE.tag & 0xFF)) {
			final int i = version <= 1 ? readInt() : readUnsignedInt();
			if (i < 0 || i > readShortStrings.size())
				throw new StreamCorruptedException("Invalid short string reference " + i);
			return "" + readShortStrings.get(i);
		}
		final byte[] d = new byte[length];
		readFully(d);
		final String s = new String(d, UTF_8);
		if (length > 4)
			readShortStrings.add(s);
		return s;
	}

	// Tag

	@Override
	protected Tag readTag() throws IOException {
		final int t = read();
		final Tag tag = Tag.byID(t);
		if (tag == null)
			throw new StreamCorruptedException("Invalid tag 0x" + Integer.toHexString(t));
		return tag;
	}

	// Primitives

	private byte readByte() throws IOException {
		return (byte) read();
	}

	private short readShort() throws IOException {
		return (short) (read() << 8 | read());
	}

	private short readUnsignedShort() throws IOException {
		final int b = read();
		if ((b & 0x80) != 0)
			return (short) (b & ~0x80);
		return (short) (b << 8 | read());
	}

	private int readInt() throws IOException {
		return read() << 24
			| read() << 16
			| read() << 8
			| read();
	}

	private int readUnsignedInt() throws IOException {
		final int b = read();
		if ((b & 0x80) != 0)
			return (b & ~0x80) << 8 | read();
		return b << 24 | read() << 16 | read() << 8 | read();
	}

	private long readLong() throws IOException {
		return (long) read() << 56
			| (long) read() << 48
			| (long) read() << 40
			| (long) read() << 32
			| (long) read() << 24
			| read() << 16
			| read() << 8
			| read();
	}

	private float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	private double readDouble() throws IOException {
		return Double.longBitsToDouble(readLong());
	}

	private char readChar() throws IOException {
		return (char) readShort();
	}

	private boolean readBoolean() throws IOException {
		final int r = read();
		if (r == 0)
			return false;
		else if (r == 1)
			return true;
		throw new StreamCorruptedException("Invalid boolean value " + r);
	}

	@Override
	protected Object readPrimitive(final Tag type) throws IOException {
		switch (type) {
			case T_BYTE:
				return readByte();
			case T_SHORT:
				return readShort();
			case T_INT:
				return readInt();
			case T_LONG:
				return readLong();
			case T_FLOAT:
				return readFloat();
			case T_DOUBLE:
				return readDouble();
			case T_CHAR:
				return readChar();
			case T_BOOLEAN:
				return readBoolean();
			//$CASES-OMITTED$
			default:
				throw new YggdrasilException("Internal error; " + type);
		}
	}

	@Override
	protected Object readPrimitive_(final Tag type) throws IOException {
		return readPrimitive(type);
	}

	// String

	@Override
	protected String readString() throws IOException {
		final int length = readUnsignedInt();
		final byte[] d = new byte[length];
		readFully(d);
		return new String(d, UTF_8);
	}

	// Array

	@Override
	protected Class<?> readArrayComponentType() throws IOException {
		return readClass();
	}

	@Override
	protected int readArrayLength() throws IOException {
		return readUnsignedInt();
	}

	// Enum

	@Override
	protected Class<?> readEnumType() throws IOException {
		return yggdrasil.getClass(readShortString());
	}

	@Override
	protected String readEnumID() throws IOException {
		return readShortString();
	}

	// Class

	@SuppressWarnings("null")
	@Override
	protected Class<?> readClass() throws IOException {
		Tag type;
		int dim = 0;
		while ((type = readTag()) == T_ARRAY)
			dim++;
		@NonNull
		Class<?> c;
		switch (type) {
			case T_OBJECT:
			case T_ENUM:
				c = yggdrasil.getClass(readShortString());
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
				c = type.c;
				assert c != null;
				break;
			case T_NULL:
			case T_REFERENCE:
				throw new StreamCorruptedException("unexpected tag " + type);
			case T_ARRAY:
			default:
				throw new YggdrasilException("Internal error; " + type);
		}
		while (dim-- > 0)
			c = Array.newInstance(c, 0).getClass();
		return c;
	}

	// Reference

	@Override
	protected int readReference() throws IOException {
		return readUnsignedInt();
	}

	// generic Object

	@Override
	protected Class<?> readObjectType() throws IOException {
		return yggdrasil.getClass(readShortString());
	}

	@Override
	protected short readNumFields() throws IOException {
		return readUnsignedShort();
	}

	@Override
	protected String readFieldID() throws IOException {
		return readShortString();
	}

	// stream

	@Override
	public void close() throws IOException {
		try {
			read();
			throw new StreamCorruptedException("Stream still has data, at least " + (1 + in.available()) + " bytes remain");
		} catch (final EOFException e) {
		} finally {
			in.close();
		}
	}

}
