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
package ch.njol.yggdrasil.xml;

import ch.njol.yggdrasil.Tag;
import ch.njol.yggdrasil.Yggdrasil;
import ch.njol.yggdrasil.YggdrasilInputStream;
import org.eclipse.jdt.annotation.Nullable;

import javax.xml.stream.*;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.lang.reflect.Array;
import java.util.NoSuchElementException;

/**
 * @deprecated XML has so many quirks that storing arbitrary data cannot be guaranteed.
 * @author Peter Güttinger
 */
@Deprecated
public final class YggXMLInputStream extends YggdrasilInputStream {

	private final XMLStreamReader in;
	private final InputStream is;

	@SuppressWarnings("unused")
	private final short version;

	@SuppressWarnings("null")
	public YggXMLInputStream(final Yggdrasil y, final InputStream in) throws IOException {
		super(y);
		is = in;
		try {
			this.in = XMLInputFactory.newFactory().createXMLStreamReader(in);
			while (this.in.next() != XMLStreamConstants.START_ELEMENT) {
			}
			if (!this.in.getLocalName().equals("yggdrasil"))
				throw new StreamCorruptedException("Not an Yggdrasil stream");
			final String v = getAttribute("version");
			short ver = 0;
			try {
				ver = Short.parseShort(v);
			} catch (final NumberFormatException e) {
			}
			if (ver <= 0 || ver > Yggdrasil.LATEST_VERSION)
				throw new StreamCorruptedException("Input was saved using a later version of Yggdrasil");
			version = ver;
		} catch (final XMLStreamException e) {
			throw new IOException(e);
		} catch (final FactoryConfigurationError e) {
			throw new IOException(e);
		}
	}

	// private

	private Class<?> getType(String s) throws StreamCorruptedException {
		int dim = 0;
		while (s.endsWith("[]")) {
			s = "" + s.substring(0, s.length() - 2);
			dim++;
		}
		Class<?> c;
		final Tag t = Tag.byName(s);
		if (t != null)
			c = t.c;
		else
			c = yggdrasil.getClass(s);
		if (c == null)
			throw new StreamCorruptedException("Invalid type " + s);
		if (dim == 0)
			return c;
		while (dim-- > 0)
			c = Array.newInstance(c, 0).getClass();
		return c;
	}

	private String getAttribute(final String name) throws StreamCorruptedException {
		final String s = in.getAttributeValue(null, name);
		if (s == null)
			throw new StreamCorruptedException("Missing attribute " + name + " for <" + in.getLocalName() + ">");
		return s;
	}

	// Tag

	@Nullable
	private Tag nextTag = null;

	@Override
	protected Tag readTag() throws IOException {
		if (nextTag != null) {
			final Tag t = nextTag;
			nextTag = null;
			return t;
		}
		try {
			while (in.next() != XMLStreamConstants.START_ELEMENT) {
			}
		} catch (final XMLStreamException e) {
			throw new StreamCorruptedException(e.getMessage());
		} catch (final NoSuchElementException e) {
			throw new EOFException();
		}
		@SuppressWarnings("null") final Tag t = Tag.byName(in.getLocalName());
		if (t == null)
			throw new StreamCorruptedException("Invalid tag " + in.getLocalName());
		return t;
	}

	// Primitives

	@Override
	protected Object readPrimitive(final Tag type) throws IOException {
		try {
			final String v = in.getElementText();
			switch (type) {
				case T_BYTE:
					return Byte.parseByte(v);
				case T_SHORT:
					return Short.parseShort(v);
				case T_INT:
					return Integer.parseInt(v);
				case T_LONG:
					return Long.parseLong(v);
				case T_FLOAT:
					return Float.parseFloat(v);
				case T_DOUBLE:
					return Double.parseDouble(v);
				case T_BOOLEAN:
					return Boolean.parseBoolean(v);
				case T_CHAR:
					if (v.length() > 1)
						throw new StreamCorruptedException();
					return v.charAt(0);
				//$CASES-OMITTED$
				default:
					throw new StreamCorruptedException();
			}
		} catch (final XMLStreamException e) {
			throw new StreamCorruptedException();
		} catch (final NumberFormatException e) {
			throw new StreamCorruptedException();
		}
	}

	@Nullable
	String primitiveData = null;
	int primitiveDataIndex = 0;

	@Override
	protected Object readPrimitive_(final Tag type) throws IOException {
		try {
			if (in.getEventType() == XMLStreamConstants.START_ELEMENT) {
				primitiveData = in.getElementText(); // advances stream to END_ELEMENT
				primitiveDataIndex = 0;
			}
			final String primitiveData = this.primitiveData;
			if (primitiveData == null)
				throw new StreamCorruptedException();
			assert in.getEventType() == XMLStreamConstants.END_ELEMENT;
			switch (type) {
				case T_BYTE:
					return (byte) Short.parseShort(primitiveData.substring(primitiveDataIndex, primitiveDataIndex += 2), 16);
				case T_SHORT:
					return (short) Integer.parseInt(primitiveData.substring(primitiveDataIndex, primitiveDataIndex += 4), 16);
				case T_INT:
					return (int) Long.parseLong(primitiveData.substring(primitiveDataIndex, primitiveDataIndex += 8), 16);
				case T_LONG:
					return Long.parseLong(primitiveData.substring(primitiveDataIndex, primitiveDataIndex += 8), 16) << 32 | Long.parseLong(primitiveData.substring(primitiveDataIndex, primitiveDataIndex += 8), 16);
				case T_FLOAT:
					return Float.intBitsToFloat((int) Long.parseLong(primitiveData.substring(primitiveDataIndex, primitiveDataIndex += 8), 16));
				case T_DOUBLE:
					return Double.longBitsToDouble(Long.parseLong(primitiveData.substring(primitiveDataIndex, primitiveDataIndex += 8), 16) << 32 | Long.parseLong(primitiveData.substring(primitiveDataIndex, primitiveDataIndex += 8), 16));
				case T_BOOLEAN:
					final char c = primitiveData.charAt(primitiveDataIndex++);
					if (c == '1')
						return true;
					else if (c == '0')
						return false;
					throw new StreamCorruptedException();
				case T_CHAR:
					return primitiveData.charAt(primitiveDataIndex++);
				//$CASES-OMITTED$
				default:
					throw new StreamCorruptedException();
			}
		} catch (final XMLStreamException e) {
			throw new StreamCorruptedException();
		} catch (final StringIndexOutOfBoundsException e) {
			throw new StreamCorruptedException();
		} catch (final NumberFormatException e) {
			throw new StreamCorruptedException();
		}
	}

	// String

	@SuppressWarnings("null")
	@Override
	protected String readString() throws IOException {
		try {
			return in.getElementText();
		} catch (final XMLStreamException e) {
			throw new StreamCorruptedException();
		}
	}

	// Array

	@Override
	protected Class<?> readArrayComponentType() throws IOException {
		return getType(getAttribute("componentType"));
	}

	@Override
	protected int readArrayLength() throws IOException {
		try {
			return Integer.parseInt(getAttribute("length"));
		} catch (final NumberFormatException e) {
			throw new StreamCorruptedException();
		}
	}

	// Enum

	@Override
	protected Class<?> readEnumType() throws IOException {
		return getType(getAttribute("type"));
	}

	@Override
	protected String readEnumID() throws IOException {
		try {
			return "" + in.getElementText();
		} catch (final XMLStreamException e) {
			throw new StreamCorruptedException();
		}
	}

	// Class

	@Override
	protected Class<?> readClass() throws IOException {
		try {
			return getType("" + in.getElementText());
		} catch (final XMLStreamException e) {
			throw new StreamCorruptedException();
		}
	}

	// Reference

	@Override
	protected int readReference() throws IOException {
		try {
			return Integer.parseInt(in.getElementText());
		} catch (final NumberFormatException e) {
			throw new StreamCorruptedException();
		} catch (final XMLStreamException e) {
			throw new StreamCorruptedException();
		}
	}

	// generic Object

	@Override
	protected Class<?> readObjectType() throws IOException {
		return getType(getAttribute("type"));
	}

	@Override
	protected short readNumFields() throws IOException {
		try {
			return Short.parseShort(getAttribute("numFields"));
		} catch (final NumberFormatException e) {
			throw new StreamCorruptedException();
		}
	}

	@Override
	protected String readFieldID() throws IOException {
		nextTag = readTag();
		return getAttribute("id");
	}

	// stream

	@Override
	public void close() throws IOException {
		try {
			// TODO error if not at EOF?
			in.close();
			is.close();
		} catch (final XMLStreamException e) {
			throw new IOException(e);
		}
	}

}
