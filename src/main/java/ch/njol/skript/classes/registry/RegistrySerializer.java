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
import org.jetbrains.annotations.NotNull;

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
	public @NotNull Fields serialize(R o) {
		Fields fields = new Fields();
		fields.putObject("name", o.getKey().toString());
		return fields;
	}

	@Override
	protected R deserialize(Fields fields) throws StreamCorruptedException {
		String name = fields.getAndRemoveObject("name", String.class);
		assert name != null;
		NamespacedKey namespacedKey;
		if (!name.contains(":")) {
			// Old variables
			namespacedKey = NamespacedKey.minecraft(name);
		} else {
			namespacedKey = NamespacedKey.fromString(name);
		}
		if (namespacedKey == null)
			throw new StreamCorruptedException("Invalid namespacedkey: " + name);
		R object = registry.get(namespacedKey);
		if (object == null)
			throw new StreamCorruptedException("Invalid object from registry: " + namespacedKey);
		return object;
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
