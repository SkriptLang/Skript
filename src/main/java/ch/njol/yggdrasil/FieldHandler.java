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

import ch.njol.yggdrasil.Fields.FieldContext;

import java.io.StreamCorruptedException;
import java.lang.reflect.Field;

public interface FieldHandler {

	/**
	 * Called when a loaded field doesn't exist.
	 *
	 * @param o The object whose filed is missing
	 * @param field The field read from stream
	 * @return Whether this Handler handled the request
	 */
	public boolean excessiveField(Object o, FieldContext field) throws StreamCorruptedException;

	/**
	 * Called if a field was not found in the stream.
	 *
	 * @param o The object whose filed is missing
	 * @param field The field that didn't occur in the stream
	 * @return Whether this Handler handled the request
	 */
	public boolean missingField(Object o, Field field) throws StreamCorruptedException;

	/**
	 * Called when a loaded value is not compatible with the type of a field.
	 *
	 * @param o The object the field belongs to
	 * @param f The field to set
	 * @param field The field read from stream
	 * @return Whether this Handler handled the request
	 */
	public boolean incompatibleField(Object o, Field f, FieldContext field) throws StreamCorruptedException;

}
