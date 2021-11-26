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
package ch.njol.skript.util;

import ch.njol.skript.variables.SerializedVariable.Value;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * This {@link PersistentDataType} is used for single variables.
 * The {@link org.bukkit.NamespacedKey}'s key should be the variable's name.
 * {hello} -> "hello" and the {@link Value} is the variable's serialized value.
 * @see PersistentDataUtils#getNamespacedKey(String)
 * @see PersistentDataUtils
 * @author APickledWalrus
 */
public final class SingleVariablePersistentDataType implements PersistentDataType<byte[], Value> {

	// An int is 4 bytes
	private static final int INT_LENGTH = 4;
	// Charset used for converting bytes and Strings
	private static final Charset SERIALIZED_CHARSET = StandardCharsets.UTF_8;

	@Override
	@NotNull
	public Class<byte[]> getPrimitiveType() {
		return byte[].class;
	}

	@Override
	@NotNull
	public Class<Value> getComplexType() {
		return Value.class;
	}

	@Override
	public byte[] toPrimitive(Value complex, PersistentDataAdapterContext context) {
		byte[] type = complex.type.getBytes(SERIALIZED_CHARSET);

		ByteBuffer bb = ByteBuffer.allocate(INT_LENGTH + type.length + complex.data.length);
		bb.putInt(type.length);
		bb.put(type);
		bb.put(complex.data);

		return bb.array();
	}

	@Override
	@NotNull
	public Value fromPrimitive(byte[] primitive, PersistentDataAdapterContext context) {
		ByteBuffer bb = ByteBuffer.wrap(primitive);

		int typeLength = bb.getInt();
		byte[] typeBytes = new byte[typeLength];
		bb.get(typeBytes, 0, typeLength);
		String type = new String(typeBytes, SERIALIZED_CHARSET);

		byte[] data = new byte[bb.remaining()];
		bb.get(data);

		return new Value(type, data);
	}

}
