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
package ch.njol.skript.classes.registry;

import ch.njol.skript.classes.Serializer;
import ch.njol.yggdrasil.Fields;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

import java.io.StreamCorruptedException;

/**
 * Serializer for {@link RegistryClassInfo}
 *
 * @param <R> Registry class
 */
public class RegistrySerializer<R extends Keyed> extends Serializer<R> {

	private final Registry<R> registry;

	public RegistrySerializer(Registry<R> registry) {
		this.registry = registry;
	}

	@Override
	public Fields serialize(R o) {
		Fields fields = new Fields();
		fields.putPrimitive("name", o.getKey().toString());
		return null;
	}

	@Override
	protected R deserialize(Fields fields) {
		try {
			String name = fields.getAndRemovePrimitive("name", String.class);
			NamespacedKey namespacedKey;
			if (!name.contains(":")) {
				// Old variables
				namespacedKey = NamespacedKey.minecraft(name);
			} else {
				namespacedKey = NamespacedKey.fromString(name);
			}
			if (namespacedKey == null)
				return null;
			return registry.get(namespacedKey);
		} catch (StreamCorruptedException e) {
			return null;
		}
	}

	@Override
	public boolean mustSyncDeserialization() {
		return false;
	}

	@Override
	protected boolean canBeInstantiated() {
		return false;
	}

	@Override
	public void deserialize(R o, Fields f) {
		throw new UnsupportedOperationException();
	}

}
