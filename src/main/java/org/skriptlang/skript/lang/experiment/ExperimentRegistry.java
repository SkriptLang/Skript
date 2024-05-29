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
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.script.Script;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A manager for registering (and identifying) experimental feature flags.
 */
/*
* TODO
* 	This is designed to be (replaced by|refactored into) a proper registry when the registries rework PR
* 	is completed. The overall skeleton is designed to remain, so that there should be no breaking changes
* 	for anything using it. I.e. you will still be able to use Skript#experiments() and obtain 'this' class
* 	although these will just become helper methods for the proper registry behaviour.
* */
public class ExperimentRegistry implements Experimented {

	private final Skript skript;
	private final Set<Experiment> experiments;

	public ExperimentRegistry(Skript skript) {
		this.skript = skript;
		this.experiments = new LinkedHashSet<>();
	}

	/**
	 * Finds an experiment matching this name. If none exist, an 'unknown' one will be created.
	 *
	 * @param text The text provided by the user.
	 * @return An experiment.
	 */
	public @NotNull Experiment find(String text) {
		if (experiments.isEmpty())
			return Experiment.unknown(text);
		for (Experiment experiment : experiments) {
			if (experiment.matches(text))
				return experiment;
		}
		return Experiment.unknown(text);
	}

	/**
	 * @return All currently-registered experiments.
	 */
	public Experiment[] registered() {
		return experiments.toArray(new Experiment[0]);
	}

	/**
	 * Registers a new experimental feature flag, which will be available to scripts
	 * with the {@code using %name%} structure.
	 *
	 * @param addon The source of this feature.
	 * @param experiment The experimental feature flag.
	 */
	public void register(SkriptAddon addon, Experiment experiment) {
		// the addon instance is requested for now in case we need it in future (for error triage)
		this.experiments.add(experiment);
	}

	/**
	 * @see #register(SkriptAddon, Experiment)
	 */
	public void registerAll(SkriptAddon addon, Experiment... experiments) {
		for (Experiment experiment : experiments) {
			this.register(addon, experiment);
		}
	}

	/**
	 * Unregisters an experimental feature flag.
	 * Loaded scripts currently using the flag will not have it disabled.
	 *
	 * @param addon The source of this feature.
	 * @param experiment The experimental feature flag.
	 */
	public void unregister(SkriptAddon addon, Experiment experiment) {
		// the addon instance is requested for now in case we need it in future (for error triage)
		this.experiments.remove(experiment);
	}

	/**
	 * Creates (and registers) a new experimental feature flag, which will be available to scripts
	 * with the {@code using %name%} structure.
	 *
	 * @param addon The source of this feature.
	 * @param codeName The debug 'code name' of this feature.
	 * @param phase The stability of this feature.
	 * @param patterns What the user may write to match the feature. Defaults to the codename if not set.
	 * @return An experiment flag.
	 */
	public Experiment register(SkriptAddon addon, String codeName, LifeCycle phase, String... patterns) {
		Experiment experiment = Experiment.constant(codeName, phase, patterns);
		this.register(addon, experiment);
		return experiment;
	}

	@Override
	public boolean hasExperiment(Experiment experiment) {
		return experiments.contains(experiment);
	}

	@Override
	public boolean hasExperiment(String featureName) {
		return this.find(featureName).isKnown();
	}

	/**
	 * Whether a script is using an experiment.
	 * @param script The script to test
	 * @param experiment The experimental flag
	 * @return Whether the script declared itself as `using X`
	 */
	public boolean isUsing(Script script, Experiment experiment) {
		if (script == null)
			return false;
		@Nullable ExperimentSet set = script.getData(ExperimentSet.class);
		if (set == null)
			return false;
		return set.hasExperiment(experiment);
	}

	/**
	 * Whether a script is using an experiment.
	 * @param script The script to test
	 * @param featureName The experimental flag's name
	 * @return Whether the script declared itself as `using X`
	 */
	public boolean isUsing(Script script, String featureName) {
		if (script == null)
			return false;
		@Nullable ExperimentSet set = script.getData(ExperimentSet.class);
		if (set == null)
			return false;
		return set.hasExperiment(featureName);
	}

}
