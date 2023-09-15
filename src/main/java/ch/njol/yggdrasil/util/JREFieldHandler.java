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
package ch.njol.yggdrasil.util;

import ch.njol.yggdrasil.FieldHandler;
import ch.njol.yggdrasil.Fields.FieldContext;
import ch.njol.yggdrasil.YggdrasilException;

import java.io.StreamCorruptedException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Handles common JRE-related incompatible field types. This handler is not added by default and is merely a utility.
 *
 * @author Peter Güttinger
 */
public class JREFieldHandler implements FieldHandler {

	/**
	 * Not used
	 */
	@Override
	public boolean excessiveField(final Object o, final FieldContext field) {
		return false;
	}

	/**
	 * Not used
	 */
	@Override
	public boolean missingField(final Object o, final Field field) throws StreamCorruptedException {
		return false;
	}

	/**
	 * Converts collection types and non-primitive arrays
	 *
	 * @throws StreamCorruptedException
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public boolean incompatibleField(final Object o, final Field f, final FieldContext field) throws StreamCorruptedException {
		Object value = field.getObject();
		if (value instanceof Object[])
			value = Arrays.asList(value);
		if (value instanceof Collection) {
			final Collection v = (Collection) value;
			try {
				if (Collection.class.isAssignableFrom(f.getType())) {
					final Collection c = (Collection) f.get(o);
					if (c != null) {
						c.clear();
						c.addAll(v);
						return true;
					}
				} else if (Object[].class.isAssignableFrom(f.getType())) {
					Object[] array = (Object[]) f.get(o);
					if (array != null) {
						if (array.length < v.size())
							return false;
						final Class<?> ct = array.getClass().getComponentType();
						for (final Object x : v) {
							if (!ct.isInstance(x))
								return false;
						}
					} else {
						array = (Object[]) Array.newInstance(f.getType().getComponentType(), v.size());
						f.set(o, array);
					}
					final int l = array.length;
					int i = 0;
					for (final Object x : v)
						array[i++] = x;
					while (i < l)
						array[i++] = null;
				}
			} catch (final IllegalArgumentException e) {
				throw new YggdrasilException(e);
			} catch (final IllegalAccessException e) {
				throw new YggdrasilException(e);
			} catch (final UnsupportedOperationException e) {
				throw new YggdrasilException(e);
			} catch (final ClassCastException e) {
				throw new YggdrasilException(e);
			} catch (final NullPointerException e) {
				throw new YggdrasilException(e);
			} catch (final IllegalStateException e) {
				throw new YggdrasilException(e);
			}
		} else if (value instanceof Map) {
			if (!Map.class.isAssignableFrom(f.getType()))
				return false;
			try {
				final Map m = (Map) f.get(o);
				if (m != null) {
					m.clear();
					m.putAll((Map) value);
					return true;
				}
			} catch (final IllegalArgumentException e) {
				throw new YggdrasilException(e);
			} catch (final IllegalAccessException e) {
				throw new YggdrasilException(e);
			} catch (final UnsupportedOperationException e) {
				throw new YggdrasilException(e);
			} catch (final ClassCastException e) {
				throw new YggdrasilException(e);
			} catch (final NullPointerException e) {
				throw new YggdrasilException(e);
			}
		}
		return false;
	}

}
