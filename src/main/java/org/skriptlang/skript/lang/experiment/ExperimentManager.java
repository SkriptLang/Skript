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
import ch.njol.skript.SkriptAddon;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A manager for registering (and identifying) experimental feature flags.
 *
 * @author moderocky
 */
public class ExperimentManager {

	private final Skript skript;
	private final Set<Experiment> experiments;

	public ExperimentManager(Skript skript) {
		this.skript = skript;
		this.experiments = new LinkedHashSet<>();
	}

	public Experiment find(String text) {
		if (experiments.isEmpty())
			return Experiment.unknown(text);
		for (Experiment experiment : experiments) {
			if (experiment.matches(text)) return experiment;
		}
		return Experiment.unknown(text);
	}

	public Experiment[] registered() {
		return experiments.toArray(new Experiment[0]);
	}

	/**
	 * Registers a new experimental feature flag, which will be available to scripts
	 * with the {@code using %name%} structure.
	 * @param addon The source of this feature.
	 * @param experiment The experimental feature flag.
	 */
	public void register(SkriptAddon addon, Experiment experiment) {
		// the addon instance is requested for now in case we need it in future (for error triage)
		this.experiments.add(experiment);
	}

}
