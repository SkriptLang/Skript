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
import ch.njol.skript.localization.Language;
import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.localization.Localizer;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

final class SkriptImpl implements Skript {

	private final Localizer localizer;

	private final SyntaxRegistry registry = SyntaxRegistry.createInstance();
	private final SyntaxRegistry unmodifiableRegistry = SyntaxRegistry.unmodifiableView(registry);

	SkriptImpl(Localizer localizer, AddonModule... modules) {
		this.localizer = localizer;
		this.registerAddon(this, modules);
	}

	@Override
	public SyntaxRegistry registry() {
		return unmodifiableRegistry;
	}

	//
	// SkriptAddon Management
	//

	private static final Set<SkriptAddon> addons = new HashSet<>();

	@Override
	public void registerAddon(SkriptAddon addon, AddonModule... modules) {
		registerAddon(addon, Arrays.asList(modules));
	}

	@Override
	public void registerAddon(SkriptAddon addon, Collection<? extends AddonModule> modules) {
		// make sure an addon is not already registered with this name
		if (addons.stream().anyMatch(otherAddon -> addon.name().equals(otherAddon.name()))) {
			throw new SkriptAPIException(
				"An addon (provided by '" + addon.getClass().getName() + "') with the name '" + addon.name() + "' is already registered"
			);
		}

		Language.loadDefault(addon); // Language will abort if no localizer is present
		// load and register the addon
		for (AddonModule module : modules) {
			module.load(addon, registry);
		}
		addons.add(addon);
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
	@NotNull
	public Localizer localizer() {
		return localizer;
	}

}
