package ch.njol.yggdrasil;

import ch.njol.util.coll.CollectionUtils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static ch.njol.yggdrasil.Tag.T_ARRAY;
import static ch.njol.yggdrasil.Tag.T_REFERENCE;

//Naming conventions:
// x(): read info & data (e.g. content type, contents) [i.e. no tag]
// _x(): read data only (e.g. contents)
public final class DefaultYggdrasilInputStream extends YggdrasilInputStream {
	
	private final InputStream in;
	private final short version;
	
	public DefaultYggdrasilInputStream(Yggdrasil yggdrasil, InputStream in) throws IOException {
		super(yggdrasil);
		this.in = in;
		if (readInt() != Yggdrasil.MAGIC_NUMBER)
			throw new StreamCorruptedException("Not an Yggdrasil stream");
		version = readShort();
		if (version <= 0 || version > Yggdrasil.LATEST_VERSION)
			throw new StreamCorruptedException("Input was saved using a later version of Yggdrasil");
	}
	
	/**
	 * @throws EOFException If the end of the stream is reached
	 */
	private int read() throws IOException {
		int b = in.read();
		if (b < 0)
			throw new EOFException();
		return b;
	}
	
	private void readFully(byte[] buf) throws IOException {
		readFully(buf, buf.length);
	}
	
	private void readFully(byte[] buf, int startLength) throws IOException {
		int offset = 0;
		int length = startLength;
		while (length > 0) {
			int n = in.read(buf, offset, length);
			if (n < 0)
				throw new EOFException("Expected " + startLength + " bytes, but could only read " + (startLength - length));
			offset += n;
			length -= n;
		}
	}
	
	private final List<String> readShortStrings = new ArrayList<>();
	
	private String readShortString() throws IOException {
		int length = read();
		if (length == (T_REFERENCE.tag & 0xFF)) {
			int i = version <= 1 ? readInt() : readUnsignedInt();
			if (i < 0 || i > readShortStrings.size())
				throw new StreamCorruptedException("Invalid short string reference " + i);
			return "" + readShortStrings.get(i);
		}
		byte[] d = new byte[length];
		readFully(d);
		String s = new String(d, StandardCharsets.UTF_8);
		if (length > 4)
			readShortStrings.add(s);
		return s;
	}
	
	@Override
	protected Tag readTag() throws IOException {
		int t = read();
		Tag tag = Tag.byID(t);
		if (tag == null)
			throw new StreamCorruptedException("Invalid tag 0x" + Integer.toHexString(t));
		return tag;
	}
	
	private byte readByte() throws IOException {
		return (byte) read();
	}
	
	private short readShort() throws IOException {
		return (short) (read() << 8 | read());
	}
	
	private short readUnsignedShort() throws IOException {
		int b = read();
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
		int b = read();
		if ((b & 0x80) != 0)
			return (b & ~0x80) << 8 | read();
		return b << 24 | read() << 16 | read() << 8 | read();
	}
	
	private long readLong() throws IOException {
		return (long) in.read() << 56 | (long) in.read() << 48
				| (long) in.read() << 40 | (long) in.read() << 32
				| (long) in.read() << 24 | (long) in.read() << 16
				| (long) in.read() << 8 | read();
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
		int r = read();
		if (r == 0)
			return false;
		else if (r == 1)
			return true;
		throw new StreamCorruptedException("Invalid boolean value " + r);
	}
	
	@Override
	protected Object readPrimitive(Tag type) throws IOException {
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
			default:
				throw new YggdrasilException("Internal error; " + type);
		}
	}
	
	@Override
	protected Object readPrimitive_(Tag type) throws IOException {
		return readPrimitive(type);
	}
	
	@Override
	protected String readString() throws IOException {
		int length = readUnsignedInt();
		byte[] d = new byte[length];
		readFully(d);
		return new String(d, StandardCharsets.UTF_8);
	}
	
	@Override
	protected Class<?> readArrayComponentType() throws IOException {
		return readClass();
	}
	
	@Override
	protected int readArrayLength() throws IOException {
		return readUnsignedInt();
	}
	
	@Override
	protected Class<?> readEnumType() throws IOException {
		return yggdrasil.getClass(readShortString());
	}
	
	@Override
	protected String readEnumID() throws IOException {
		return readShortString();
	}
	
	@SuppressWarnings("null")
	@Override
	protected Class<?> readClass() throws IOException {
		Tag type;
		int dimensions = 0;
		while ((type = readTag()) == T_ARRAY)
			dimensions++;
		Class<?> clazz;
		switch (type) {
			case T_OBJECT:
			case T_ENUM:
				clazz = yggdrasil.getClass(readShortString());
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
				assert type.type != null;
				clazz = type.type;
				break;
			case T_NULL:
			case T_REFERENCE:
				throw new StreamCorruptedException("unexpected tag " + type);
			case T_ARRAY:
			default:
				throw new YggdrasilException("Internal error; " + type);
		}
		while (dimensions-- > 0)
			clazz = CollectionUtils.arrayType(clazz);
		return clazz;
	}
	
	@Override
	protected int readReference() throws IOException {
		return readUnsignedInt();
	}
	
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
	
	@Override
	public void close() throws IOException {
		try {
			read();
			throw new StreamCorruptedException("Stream still has data, at least " + (1 + in.available()) + " bytes remain");
		} catch (EOFException ignored) {} finally {
			in.close();
		}
	}
	
}
