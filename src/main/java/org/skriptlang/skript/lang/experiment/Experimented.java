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

import ch.njol.skript.Skript;

/**
 * Something that can have experimental features enabled for.
 * The only intended implementation of this is the {@link org.skriptlang.skript.lang.script.Script},
 * however it is left open for configuration files, etc. that may use this functionality in the future.
 */
@FunctionalInterface
public interface Experimented {

	/**
	 * @param experiment The experimental feature to test.
	 * @return Whether this uses the given feature.
	 */
	boolean hasExperiment(Experiment experiment);

	/**
	 * @param featureName The name of the experimental feature to test.
	 * @return Whether this has a feature with the given name.
	 */
	default boolean hasExperiment(String featureName) {
		return Skript.experiments().find(featureName).isKnown();
	}

}
