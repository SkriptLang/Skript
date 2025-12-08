package org.skriptlang.skript.lang.experiment;

import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.patterns.SkriptPattern;
import ch.njol.skript.registrations.Feature;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

/**
 * An optional, potentially-experimental feature enabled per-script with the {@code using X} syntax.
 * Experiments provided by Skript itself are found in {@link Feature}.
 * This can also represent an unknown experiment 'used' by a script that was not declared or registered
 * by Skript or any of its addons.
 */
public interface Experiment {

	@ApiStatus.Internal
	static Experiment unknown(String text) {
		return new UnmatchedExperiment(text);
	}

	/**
	 * A constant experiment provider (designed for the use of addons).
	 * @param codeName The debug 'code name' of this feature.
	 * @param phase The stability of this feature.
	 * @param patterns What the user may write to match the feature. Defaults to the codename if not set.
	 * @return An experiment flag.
	 */
	static Experiment constant(String codeName, LifeCycle phase, String... patterns) {
		return new ConstantExperiment(codeName, phase, patterns);
	}

	/**
	 * A simple, printable code-name for this pattern for warnings and debugging.
	 * Ideally, this should be matched by one of the {@link #pattern()} entries.
	 *
	 * @return The code name of this experiment.
	 */
	String codeName();

	/**
	 * @return The safety phase of this feature.
	 */
	LifeCycle phase();

	/**
	 * @return Whether this feature was declared by Skript or a real extension.
	 */
	default boolean isKnown() {
		return this.phase() != LifeCycle.UNKNOWN;
	}

	/**
	 * @return The compiled matching pattern for this experiment
	 */
	SkriptPattern pattern();

	/**
	 * @return Whether the usage pattern of this experiment matches the input text
	 */
	default boolean matches(String text) {
		return this.pattern().match(text) != null;
	}

}

/**
 * A class for constant experiments.
 */
class ConstantExperiment implements Experiment {

	private final String codeName;
	private final SkriptPattern compiledPattern;
	private final LifeCycle phase;

	ConstantExperiment(String codeName, LifeCycle phase) {
		this(codeName, phase, new String[0]);
	}

	ConstantExperiment(String codeName, LifeCycle phase, String... patterns) {
		this.codeName = codeName;
		this.phase = phase;
		switch (patterns.length) {
			case 0:
				this.compiledPattern = PatternCompiler.compile(codeName);
				break;
			case 1:
				this.compiledPattern = PatternCompiler.compile(patterns[0]);
				break;
			default:
				this.compiledPattern = PatternCompiler.compile(String.join("|", patterns));
				break;
		}
	}

	@Override
	public String codeName() {
		return codeName;
	}

	@Override
	public LifeCycle phase() {
		return phase;
	}

	@Override
	public SkriptPattern pattern() {
		return compiledPattern;
	}

	@Override
	public boolean matches(String text) {
		return codeName.equals(text);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Experiment that = (Experiment) o;
		return Objects.equals(this.codeName(), that.codeName());
	}

	@Override
	public int hashCode() {
		return codeName.hashCode();
	}

}

/**
 * The dummy class for an unmatched experiment.
 * This is something that was 'used' by a file but was never registered with Skript.
 * These are kept so that they *can* be tested for (e.g. by a third-party extension that uses a post-registration
 * experiment system).
 */
class UnmatchedExperiment extends ConstantExperiment {

	UnmatchedExperiment(String codeName) {
		super(codeName, LifeCycle.UNKNOWN);
	}

	@Override
	public LifeCycle phase() {
		return LifeCycle.UNKNOWN;
	}

	@Override
	public boolean isKnown() {
		return false;
	}

}
