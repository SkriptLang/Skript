package org.skriptlang.skript.lang.experiment;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.Skript;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.util.Registry;
import org.skriptlang.skript.util.ViewProvider;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A manager for registering (and identifying) experimental feature flags.
 */
public class ExperimentRegistry implements Registry<Experiment>, ViewProvider<ExperimentRegistry>, Experimented {

	private final Skript skript;
	private final Set<Experiment> experiments;
	private final @Nullable ExperimentRegistry source;

	public ExperimentRegistry(Skript skript) {
		this.skript = skript;
		this.experiments = new LinkedHashSet<>();
		this.source = null;
	}

	/**
	 * Internal constructor for creating an unmodifiable view of an experiment registry.
	 */
	private ExperimentRegistry(ExperimentRegistry source) {
		this.skript = source.skript;
		this.experiments = source.experiments;
		this.source = source;
	}

	/**
	 * @deprecated Use {@link #ExperimentRegistry(Skript)}.
	 */
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
	public ExperimentRegistry(ch.njol.skript.Skript ignored) {
		this(ch.njol.skript.Skript.instance());
	}

	/**
	 * @return An unmodifiable view of this experiment registry.
	 */
	@Override
	public ExperimentRegistry unmodifiableView() {
		return new ExperimentRegistry(this);
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
	 * @return An unmodifiable set containing all currently-registered experiments.
	 */
	@Override
	public @Unmodifiable Set<Experiment> elements() {
		return Set.copyOf(experiments);
	}

	/**
	 * Registers a new experimental feature flag, which will be available to scripts
	 * with the {@code using %name%} structure.
	 *
	 * @param addon The source of this feature.
	 * @param experiment The experimental feature flag.
	 */
	public void register(SkriptAddon addon, Experiment experiment) {
		if (source != null) {
			throw new UnsupportedOperationException("Cannot register experiments using an unmodifiable registry.");
		}
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
		if (source != null) {
			throw new UnsupportedOperationException("Cannot unregister experiments using an unmodifiable registry.");
		}
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
