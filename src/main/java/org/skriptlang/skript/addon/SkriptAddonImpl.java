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
package org.skriptlang.skript.addon;

import org.skriptlang.skript.localization.Localizer;
import org.skriptlang.skript.registration.SyntaxRegistry;

class SkriptAddonImpl {

	static class UnmodifiableAddon implements SkriptAddon {

		private final SkriptAddon addon;
		private final SyntaxRegistry unmodifiableRegistry;
		private final Localizer unmodifiableLocalizer;

		UnmodifiableAddon(SkriptAddon addon) {
			this.addon = addon;
			this.unmodifiableRegistry = SyntaxRegistry.unmodifiableView(addon.registry());
			this.unmodifiableLocalizer = Localizer.unmodifiableView(addon.localizer());
		}

		@Override
		public String name() {
			return addon.name();
		}

		@Override
		public SyntaxRegistry registry() {
			return unmodifiableRegistry;
		}

		@Override
		public Localizer localizer() {
			return unmodifiableLocalizer;
		}

	}

}
