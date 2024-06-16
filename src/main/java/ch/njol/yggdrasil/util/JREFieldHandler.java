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
package ch.njol.yggdrasil.util;

import ch.njol.yggdrasil.FieldHandler;
import ch.njol.yggdrasil.Fields.FieldContext;
import ch.njol.yggdrasil.YggdrasilException;

import java.io.StreamCorruptedException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Handles common JRE-related incompatible field types.
 * This handler is not added by default and is merely a utility.
 */
@Deprecated
public class JREFieldHandler implements FieldHandler {
	
	@Override
	public boolean excessiveField(Object object, FieldContext field) {
		return false;
	}
	
	@Override
	public boolean missingField(Object object, Field field) {
		return false;
	}
	
	/**
	 * Converts collection types and non-primitive arrays
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public boolean incompatibleField(Object object, Field field, FieldContext context) throws StreamCorruptedException {
		Object value = context.getObject();
		if (value instanceof Object[])
			value = Collections.singletonList(value);
		if (value instanceof Collection) {
			Collection collection = (Collection) value;
			try {
				if (Collection.class.isAssignableFrom(field.getType())) {
					Collection c = (Collection) field.get(object);
					if (c != null) {
						c.clear();
						c.addAll(collection);
						return true;
					}
				} else if (Object[].class.isAssignableFrom(field.getType())) {
					Object[] array = (Object[]) field.get(object);
					if (array != null) {
						if (array.length < collection.size())
							return false;
						Class<?> ct = array.getClass().getComponentType();
						for (Object iterated : collection) {
							if (!ct.isInstance(iterated))
								return false;
						}
					} else {
						array = (Object[]) Array.newInstance(field.getType().getComponentType(), collection.size());
						field.set(object, array);
					}
					int length = array.length;
					int i = 0;
					for (Object iterated : collection)
						array[i++] = iterated;
					while (i < length)
						array[i++] = null;
				}
			} catch (
				IllegalArgumentException | NullPointerException | IllegalStateException |
				ClassCastException | UnsupportedOperationException | IllegalAccessException e
			) {
				throw new YggdrasilException(e);
			}
		} else if (value instanceof Map) {
			if (!Map.class.isAssignableFrom(field.getType()))
				return false;
			try {
				Map m = (Map) field.get(object);
				if (m != null) {
					m.clear();
					m.putAll((Map) value);
					return true;
				}
			} catch (
				IllegalArgumentException | IllegalAccessException | UnsupportedOperationException |
				ClassCastException | NullPointerException e
			) {
				throw new YggdrasilException(e);
			}
		}
		
		return false;
	}
	
}
