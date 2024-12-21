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
package ch.njol.skript.classes;

import java.io.StreamCorruptedException;

import org.jetbrains.annotations.Nullable;

import ch.njol.yggdrasil.ClassResolver;
import ch.njol.yggdrasil.Fields;

/**
 * Mainly kept for backwards compatibility, but also serves as {@link ClassResolver} for enums.
 */
public class EnumSerializer<T extends Enum<T>> extends Serializer<T> {
	
	private final Class<T> c;
	
	public EnumSerializer(Class<T> c) {
		this.c = c;
	}
	
	/**
	 * Enum serialization has been using String serialization since Skript (2.7)
	 */
	@Override
	@Deprecated
	@Nullable
	public T deserialize(String s) {
		try {
			return Enum.valueOf(c, s);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}
	
	@Override
	public boolean mustSyncDeserialization() {
		return false;
	}
	
	@Override
	public boolean canBeInstantiated() {
		assert false;
		return false;
	}
	
	@Override
	public Fields serialize(T e) {
		Fields fields = new Fields();
		fields.putPrimitive("name", e.name());
		return fields;
	}
	
	@Override
	public T deserialize(Fields fields) {
		try {
			return Enum.valueOf(c, fields.getAndRemovePrimitive("name", String.class));
		} catch (IllegalArgumentException | StreamCorruptedException e) {
			return null;
		}
	}
	
	@Override
	public void deserialize(T o, Fields f) {
		assert false;
	}
	
}
