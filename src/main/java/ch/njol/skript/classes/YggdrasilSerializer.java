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

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;

import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializable;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;

/**
 * Serializer that allows Yggdrasil to automatically serialize classes that extend YggdrasilSerializable.
 */
public class YggdrasilSerializer<T extends YggdrasilSerializable> extends Serializer<T> {
	
	@Override
	public final Fields serialize(final T o) throws NotSerializableException {
		if (o instanceof YggdrasilExtendedSerializable)
			return ((YggdrasilExtendedSerializable) o).serialize();
		return new Fields(o);
	}
	
	@Override
	public final void deserialize(final T o, final Fields f) throws StreamCorruptedException, NotSerializableException {
		if (o instanceof YggdrasilExtendedSerializable)
			((YggdrasilExtendedSerializable) o).deserialize(f);
		else
			f.setFields(o);
	}
	
	/**
	 * Deserialises an object from a string returned by this serializer or an earlier version thereof.
	 * <p>
	 * This method should only return null if the input is invalid (i.e. not produced by {@link #serialize(Object)} or an older version of that method)
	 * <p>
	 * This method must only be called from Bukkit's main thread if {@link #mustSyncDeserialization()} returned true.
	 * 
	 * @param s
	 * @return The deserialised object or null if the input is invalid. An error message may be logged to specify the cause.
	 */
	@Deprecated
	@Override
	public T deserialize(String s) {
		return null;
	}
	
	@Override
	public boolean mustSyncDeserialization() {
		return false;
	}
	
	@Override
	public boolean canBeInstantiated() {
		return true;
	}
	
}
