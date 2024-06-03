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

import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.script.ScriptData;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * A container for storing and testing experiments.
 */
public class ExperimentSet extends LinkedHashSet<Experiment> implements ScriptData, Experimented {

	public ExperimentSet(@NotNull Collection<? extends Experiment> collection) {
		super(collection);
	}

	public ExperimentSet() {
		super();
	}

	@Override
	public boolean hasExperiment(Experiment experiment) {
		return this.contains(experiment);
	}

	@Override
	public boolean hasExperiment(String featureName) {
		for (Experiment experiment : this) {
			if (experiment.matches(featureName))
				return true;
		}
		return false;
	}

}
