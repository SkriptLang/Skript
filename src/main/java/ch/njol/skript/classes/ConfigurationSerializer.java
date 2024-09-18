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

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.Nullable;

import ch.njol.yggdrasil.Fields;

/**
 * Uses strings for serialisation because the whole ConfigurationSerializable interface is badly documented, and especially DelegateDeserialization doesn't work well with
 * Yggdrasil.
 * 
 * @author Peter Güttinger
 */
public class ConfigurationSerializer<T extends ConfigurationSerializable> extends Serializer<T> {
	
	@Override
	public Fields serialize(final T o) throws NotSerializableException {
		final Fields f = new Fields();
		f.putObject("value", serializeCS(o));
		return f;
	}
	
	@Override
	public boolean mustSyncDeserialization() {
		return false;
	}
	
	@Override
	public boolean canBeInstantiated() {
		return false;
	}

	@Override
	protected T deserialize(final Fields fields) throws StreamCorruptedException {
		final String val = fields.getObject("value", String.class);
		if (val == null)
			throw new StreamCorruptedException();
		final ClassInfo<? extends T> info = this.info;
		assert info != null;
		final T t = deserializeCS(val, info.getC());
		if (t == null)
			throw new StreamCorruptedException();
		return t;
	}
	
	public static String serializeCS(final ConfigurationSerializable o) {
		final YamlConfiguration y = new YamlConfiguration();
		y.set("value", o);
		return "" + y.saveToString();
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public static <T extends ConfigurationSerializable> T deserializeCS(final String s, final Class<T> c) {
		final YamlConfiguration y = new YamlConfiguration();
		try {
			y.loadFromString(s);
		} catch (final InvalidConfigurationException e) {
			return null;
		}
		final Object o = y.get("value");
		if (!c.isInstance(o))
			return null;
		return (T) o;
	}
	
	@Override
	@Nullable
	public <E extends T> E newInstance(final Class<E> c) {
		assert false;
		return null;
	}
	
	@Override
	public void deserialize(final T o, final Fields fields) throws StreamCorruptedException {
		assert false;
	}
	
	@Override
	@Deprecated
	@Nullable
	public T deserialize(final String s) {
		final ClassInfo<? extends T> info = this.info;
		assert info != null;
		return deserializeCSOld(s, info.getC());
	}
	
	@SuppressWarnings("unchecked")
	@Deprecated
	@Nullable
	public static <T extends ConfigurationSerializable> T deserializeCSOld(final String s, final Class<T> c) {
		final YamlConfiguration y = new YamlConfiguration();
		try {
			y.loadFromString(s.replace("\uFEFF", "\n"));
		} catch (final InvalidConfigurationException e) {
			return null;
		}
		final Object o = y.get("value");
		if (!c.isInstance(o))
			return null;
		return (T) o;
	}
	
}
