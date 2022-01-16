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

import ch.njol.skript.Skript;
import ch.njol.skript.variables.SerializedVariable.Value;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This {@link PersistentDataType} is used for list variables.
 * In this case, a list variable is any variable containing "::" (the separator)
 * The map's key is the variable's index and the map's value is the index's value.
 * With this {@link PersistentDataType}, the NamespacedKey's key is the rest of the list variable.
 * e.g. {one::two::three} where "one//two" would be the {@link org.bukkit.NamespacedKey}'s key and "three" the key for the map.
 * @see PersistentDataUtils#getNamespacedKey(String)
 * @see PersistentDataUtils
 * @author APickledWalrus
 */
public final class ListVariablePersistentDataType implements PersistentDataType<byte[], Map<String, Value>> {

	// Charset used for converting bytes and Strings
	private static final Charset SERIALIZED_CHARSET = StandardCharsets.UTF_8;

	@Override
	@NotNull
	public Class<byte[]> getPrimitiveType() {
		return byte[].class;
	}

	@Override
	@NotNull
	@SuppressWarnings("unchecked")
	public Class<Map<String, Value>> getComplexType() {
		return (Class<Map<String, Value>>) (Class<?>) Map.class;
	}

	@Override
	public byte[] toPrimitive(Map<String, Value> complex, PersistentDataAdapterContext context) {
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		DataOutputStream outputStream = new DataOutputStream(byteArray);

		for (Entry<String, Value> entry : complex.entrySet()) {
			byte[] indexBytes = entry.getKey().getBytes(SERIALIZED_CHARSET);
			byte[] typeBytes = entry.getValue().type.getBytes(SERIALIZED_CHARSET);

			try {
				outputStream.writeInt(indexBytes.length);
				outputStream.write(indexBytes);

				outputStream.writeInt(typeBytes.length);
				outputStream.write(typeBytes);

				byte[] data = entry.getValue().data;
				outputStream.writeInt(data.length);
				outputStream.write(data);
			} catch (IOException e) { // This is very bad
				throw Skript.exception(e);
			}
		}

		return byteArray.toByteArray();
	}

	@Override
	@NotNull
	public Map<String, Value> fromPrimitive(byte[] primitive, PersistentDataAdapterContext context) {
		ByteBuffer bb = ByteBuffer.wrap(primitive);

		HashMap<String, Value> values = new HashMap<>();

		while (bb.hasRemaining()) {
			int indexLength = bb.getInt();
			byte[] indexBytes = new byte[indexLength];
			bb.get(indexBytes, 0, indexLength);
			String index = new String(indexBytes, SERIALIZED_CHARSET);

			int typeLength = bb.getInt();
			byte[] typeBytes = new byte[typeLength];
			bb.get(typeBytes, 0, typeLength);
			String type = new String(typeBytes, SERIALIZED_CHARSET);

			int dataLength = bb.getInt();
			byte[] dataBytes = new byte[dataLength];
			bb.get(dataBytes, 0, dataLength);

			values.put(index, new Value(type, dataBytes));
		}

		return values;
	}

}
