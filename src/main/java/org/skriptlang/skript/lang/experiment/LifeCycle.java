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
package org.skriptlang.skript.lang.experiment;

/**
 * The life cycle phase of an {@link Experiment}.
 */
public enum LifeCycle {
	/**
	 * A feature that is expected to be safe and (at least) semi-permanent.
	 * This can be used for long-term features that are kept behind toggles to prevent breaking changes.
	 */
	STABLE(false),
	/**
	 * An experimental, preview feature designed to be used with caution.
	 * Features in the experimental phase may be subject to changes or removal at short notice.
	 */
	EXPERIMENTAL(false),
	/**
	 * A feature at the end of its life cycle, being prepared for removal.
	 * Scripts will report a deprecation warning on load if a deprecated feature is used.
	 */
	DEPRECATED(true),
	/**
	 * Represents a feature that was previously opt-in (or experimental) but is now a part of the default set.
	 * I.e. it no longer needs to be enabled using a feature flag.
	 * This will provide a little note to the user on load informing them they no longer need to
	 * use this feature flag.
	 */
	MAINSTREAM(true),
	/**
	 * Represents an unregistered, unknown feature.
	 * This occurs when a user tags a script as {@code using X}, where {@code X} is not a registered
	 * feature provided by any addon or extension.
	 * Scripts will report a warning on load if an unknown feature is used, but this will not prevent
	 * the loading cycle.
	 */
	UNKNOWN(true);

	private final boolean warn;

	LifeCycle(boolean warn) {
		this.warn = warn;
	}

	/**
	 * @return Whether using a feature of this type will produce a warning on load.
	 */
	public boolean warn() {
		return warn;
	}

}
