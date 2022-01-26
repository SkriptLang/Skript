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
package ch.njol.util;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class EnumTypeAdapter<T extends Enum<T>> extends TypeAdapter<T> {
	public static final TypeAdapterFactory factory = new TypeAdapterFactory() {
		@SuppressWarnings({"rawtypes", "unchecked"})
		@Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
			Class<? super T> rawType = typeToken.getRawType();
			if (!Enum.class.isAssignableFrom(rawType) || rawType == Enum.class) {
				return null;
			}
			if (!rawType.isEnum()) {
				rawType = rawType.getSuperclass(); // handle anonymous subclasses
			}
			return (TypeAdapter<T>) new EnumTypeAdapter(rawType);
		}
	};
	private final Map<String, T> nameToConstant = new HashMap<String, T>();
	private final Map<T, String> constantToName = new HashMap<T, String>();
	
	public EnumTypeAdapter(Class<T> classOfT) {
		for (T constant : classOfT.getEnumConstants()) {
			String name = constant.name();
			
			try {
				SerializedName annotation = classOfT.getField(name).getAnnotation(SerializedName.class);
				if (annotation != null) {
					name = annotation.value();
					for (String alternate : annotation.alternate()) {
						nameToConstant.put(alternate, constant);
					}
				}
			} catch (NoSuchFieldException e) {}
			
			nameToConstant.put(name, constant);
			constantToName.put(constant, name);
		}
	}
	@Override public T read(JsonReader in) throws IOException {
		if (in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		return nameToConstant.get(in.nextString());
	}
	
	@Override public void write(JsonWriter out, T value) throws IOException {
		out.value(value == null ? null : constantToName.get(value));
	}
}