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
package org.skriptlang.skript;

import ch.njol.skript.SkriptAPIException;
import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.localization.Localizer;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.registration.SyntaxRegistry.ChildSyntaxRegistry;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

final class SkriptImpl implements Skript {

	final SkriptAddon addon;
	private final SkriptAddon unmodifiableAddon;

	SkriptImpl(String name) {
		addon = new SkriptAddonImpl(name, SyntaxRegistry.empty(), Localizer.of(this));
		unmodifiableAddon = SkriptAddon.unmodifiableView(addon);
	}

	//
	// SkriptAddon Management
	//

	private static final Set<SkriptAddon> addons = new HashSet<>();

	@Override
	public SkriptAddon registerAddon(String name) {
		// make sure an addon is not already registered with this name
		for (SkriptAddon addon : addons) {
			if (name.equals(addon.name())) {
				throw new SkriptAPIException(
					"An addon (provided by '" + addon.getClass().getName() + "') with the name '" + name + "' is already registered"
				);
			}
		}

		SkriptAddon addon = new SkriptAddonImpl(name, ChildSyntaxRegistry.of(this.addon.registry(), SyntaxRegistry.empty()), null);
		addons.add(addon);
		return addon;
	}

	@Override
	@Unmodifiable
	public Collection<SkriptAddon> addons() {
		return ImmutableSet.copyOf(addons);
	}

	//
	// SkriptAddon Implementation
	//

	@Override
	public String name() {
		return unmodifiableAddon.name();
	}

	@Override
	@UnmodifiableView
	public SyntaxRegistry registry() {
		return unmodifiableAddon.registry();
	}

	@Override
	public Localizer localizer() {
		return unmodifiableAddon.localizer();
	}

	private static final class SkriptAddonImpl implements SkriptAddon {

		private final String name;
		private final SyntaxRegistry registry;
		private final Localizer localizer;

		SkriptAddonImpl(String name, SyntaxRegistry registry, @Nullable Localizer localizer) {
			this.name = name;
			this.registry = registry;
			this.localizer = localizer == null ? Localizer.of(this) : localizer;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public SyntaxRegistry registry() {
			return registry;
		}

		@Override
		public Localizer localizer() {
			return localizer;
		}

	}

}
