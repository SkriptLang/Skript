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

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JRESerializer extends YggdrasilSerializer<Object> {
	
	private static final List<Class<?>> SUPPORTED_CLASSES = ImmutableList.of(
			ArrayList.class, LinkedList.class, HashSet.class, HashMap.class, UUID.class);
	
	@Override
	@Nullable
	public Class<?> getClass(String id) {
		for (Class<?> type : SUPPORTED_CLASSES)
			if (type.getSimpleName().equals(id))
				return type;
		return null;
	}
	
	@Override
	@Nullable
	public String getID(Class<?> type) {
		if (SUPPORTED_CLASSES.contains(type))
			return type.getSimpleName();
		return null;
	}
	
	@Override
	public Fields serialize(Object object) {
		if (!SUPPORTED_CLASSES.contains(object.getClass()))
			throw new IllegalArgumentException();
		Fields fields = new Fields();
		if (object instanceof Collection) {
			Collection<?> collection = ((Collection<?>) object);
			fields.putObject("values", collection.toArray());
		} else if (object instanceof Map) {
			Map<?, ?> map = ((Map<?, ?>) object);
			fields.putObject("keys", map.keySet().toArray());
			fields.putObject("values", map.values().toArray());
		} else if (object instanceof UUID) {
			fields.putPrimitive("mostSigBits", ((UUID) object).getMostSignificantBits());
			fields.putPrimitive("leastSigBits", ((UUID) object).getLeastSignificantBits());
		}
		assert fields.size() > 0 : object;
		return fields;
	}
	
	@Override
	public boolean canBeInstantiated(Class<?> type) {
		return type != UUID.class;
	}
	
	@Override
	@Nullable
	public <T> T newInstance(Class<T> type) {
		try {
			//noinspection deprecation
			return type.newInstance();
		} catch (InstantiationException e) { // all collections handled here have public nullary constructors
			e.printStackTrace();
			assert false;
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			assert false;
			return null;
		}
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void deserialize(Object object, Fields fields) throws StreamCorruptedException {
		try {
			if (object instanceof Collection) {
				Collection<?> collection = ((Collection<?>) object);
				Object[] values = fields.getObject("values", Object[].class);
				if (values == null)
					throw new StreamCorruptedException();
				collection.addAll((Collection) Arrays.asList(values));
				return;
			} else if (object instanceof Map) {
				Map<?, ?> map = ((Map<?, ?>) object);
				Object[] keys = fields.getObject("keys", Object[].class), values = fields.getObject("values", Object[].class);
				if (keys == null || values == null || keys.length != values.length)
					throw new StreamCorruptedException();
				for (int i = 0; i < keys.length; i++)
					((Map) map).put(keys[i], values[i]);
				return;
			}
		} catch (Exception e) {
			throw new StreamCorruptedException(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
		throw new StreamCorruptedException();
	}
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public <E> E deserialize(Class<E> type, Fields fields) throws StreamCorruptedException, NotSerializableException {
		if (type == UUID.class)
			return (E) new UUID(
					fields.getPrimitive("mostSigBits", Long.TYPE),
					fields.getPrimitive("leastSigBits", Long.TYPE));
		
		throw new StreamCorruptedException();
	}
	
}
