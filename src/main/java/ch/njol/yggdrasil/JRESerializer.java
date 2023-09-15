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

import org.eclipse.jdt.annotation.Nullable;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.*;

public class JRESerializer extends YggdrasilSerializer<Object> {

	private final static Class<?>[] supportedClasses = {
		ArrayList.class, LinkedList.class,
		HashSet.class,
		HashMap.class,
		UUID.class
	};

	private final static Set<Class<?>> set = new HashSet<>(Arrays.asList(supportedClasses));

	@Override
	@Nullable
	public Class<?> getClass(final String id) {
		for (final Class<?> c : supportedClasses)
			if (c.getSimpleName().equals(id))
				return c;
		return null;
	}

	@Override
	@Nullable
	public String getID(final Class<?> c) {
		if (set.contains(c))
			return c.getSimpleName();
		return null;
	}

	@Override
	public Fields serialize(final Object o) {
		if (!set.contains(o.getClass()))
			throw new IllegalArgumentException();
		final Fields f = new Fields();
		if (o instanceof Collection) {
			final Collection<?> c = ((Collection<?>) o);
			f.putObject("values", c.toArray());
		} else if (o instanceof Map) {
			final Map<?, ?> m = ((Map<?, ?>) o);
			f.putObject("keys", m.keySet().toArray());
			f.putObject("values", m.values().toArray());
		} else if (o instanceof UUID) {
			f.putPrimitive("mostSigBits", Long.valueOf(((UUID) o).getMostSignificantBits()));
			f.putPrimitive("leastSigBits", Long.valueOf(((UUID) o).getLeastSignificantBits()));
		}
		assert f.size() > 0 : o;
		return f;
	}

	@Override
	public boolean canBeInstantiated(Class<? extends Object> c) {
		return c != UUID.class;
	}

	@Override
	@Nullable
	public <T> T newInstance(final Class<T> c) {
		try {
			return c.newInstance();
		} catch (final InstantiationException e) { // all collections handled here have public nullary constructors
			e.printStackTrace();
			assert false;
			return null;
		} catch (final IllegalAccessException e) {
			e.printStackTrace();
			assert false;
			return null;
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void deserialize(final Object o, final Fields fields) throws StreamCorruptedException {
		try {
			if (o instanceof Collection) {
				final Collection<?> c = ((Collection<?>) o);
				final Object[] values = fields.getObject("values", Object[].class);
				if (values == null)
					throw new StreamCorruptedException();
				c.addAll((Collection) Arrays.asList(values));
				return;
			} else if (o instanceof Map) {
				final Map<?, ?> m = ((Map<?, ?>) o);
				final Object[] keys = fields.getObject("keys", Object[].class), values = fields.getObject("values", Object[].class);
				if (keys == null || values == null || keys.length != values.length)
					throw new StreamCorruptedException();
				for (int i = 0; i < keys.length; i++)
					((Map) m).put(keys[i], values[i]);
				return;
			}
		} catch (final Exception e) {
			throw new StreamCorruptedException(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
		throw new StreamCorruptedException();
	}

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public <E> E deserialize(Class<E> c, Fields fields) throws StreamCorruptedException, NotSerializableException {
		if (c == UUID.class) {
			return (E) new UUID(fields.getPrimitive("mostSigBits", Long.TYPE).longValue(), fields.getPrimitive("leastSigBits", Long.TYPE).longValue());
		}
		throw new StreamCorruptedException();
	}

}
