package org.skriptlang.skript.lang.experiment;

import ch.njol.skript.Skript;
import ch.njol.util.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Container for holding {@link Experiment}s that must be enabled or disabled to use.
 */
public class ExperimentData {

	private final Set<Experiment> required = new HashSet<>();
	private final Set<Experiment> disallowed = new HashSet<>();
	private @Nullable String errorMessage = null;

	public ExperimentData() {}

	/**
	 * Set the {@link Experiment}s that must be enabled in order to use.
	 * @param required The {@link Experiment}s
	 * @return This {@link ExperimentData}.
	 */
	public ExperimentData required(Experiment... required) {
		this.required.addAll(Arrays.stream(required).toList());
		return this;
	}

	/**
	 * Set the {@link Experiment}s that must be disabled in order to use.
	 * @param disallowed The {@link Experiment}s
	 * @return This {@link ExperimentData}.
	 */
	public ExperimentData disallowed(Experiment... disallowed) {
		this.disallowed.addAll(Arrays.stream(disallowed).toList());
		return this;
	}

	/**
	 * Set the error message to be printed if the requirements of this {@link ExperimentData} are not met.
	 * @param errorMessage The error message.
	 * @return This {@link ExperimentData}.
	 */
	public ExperimentData errorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
		return this;
	}

	/**
	 * Get the {@link Experiment}s that must be enabled in order to use.
	 */
	public Set<Experiment> required() {
		return required;
	}

	/**
	 * Get the {@link Experiment}s that must be disabled in order to use.
	 */
	public Set<Experiment> disallowed() {
		return disallowed;
	}

	/**
	 * Check if this {@link ExperimentData} is valid.
	 * @return {@code True} if valid.
	 */
	public boolean isValid() {
		return !required.isEmpty() || !disallowed.isEmpty();
	}

	/**
	 * Check if the requirements of this {@link ExperimentData} are met.
	 * @param experiments The current enabled {@link Experiment}s.
	 * @return {@code True} if the requirements were met.
	 */
	public boolean checkRequirements(ExperimentSet experiments) {
		if (!isValid())
			throw new IllegalArgumentException("An ExperimentData must have required and/or disallowed Experiements");
		if (!required.isEmpty()) {
			for (Experiment experiment : required) {
				if (!experiments.hasExperiment(experiment))
					return false;
			}
		}
		if (!disallowed.isEmpty()) {
			for (Experiment experiment : disallowed) {
				if (experiments.hasExperiment(experiment))
					return false;
			}
		}
		return true;
	}

	/**
	 * Check if the requirements of this {@link ExperimentData} are met.
	 * If the requirements are not met, will produce a {@link Skript#error(String)} using {@link #errorMessage()}.
	 * @param experiments The current enabled {@link Experiment}s.
	 * @return {@code True} if the requirements were met.
	 */
	public boolean checkRequirementsAndError(ExperimentSet experiments) {
		if (!checkRequirements(experiments)) {
			Skript.error(errorMessage());
			return false;
		}
		return true;
	}

	/**
	 * Get the error message to be printed when the requirements of this {@link ExperimentData} are not met.
	 * If {@link #errorMessage} is {@code null}, will construct an error message via {@link #constructError()}.
	 */
	public String errorMessage() {
		return errorMessage != null ? errorMessage : constructError();
	}

	/**
	 * Construct a {@link String} combining what {@link Experiment}s need to be enabled and/or disabled in order to use.
	 */
	public String constructError() {
		StringBuilder builder = new StringBuilder();
		builder.append("This element is experimental. To use this, ");
		if (!required.isEmpty()) {
			builder.append("enable ");
			builder.append(StringUtils.join(
				required.stream()
					.map(experiment -> "'" + experiment.codeName() + "'")
					.toArray(),
				", "));
			if (!disallowed.isEmpty()) {
				builder.append(" and disable ");
				builder.append(StringUtils.join(
					disallowed.stream()
						.map(experiment -> "'" + experiment.codeName() + "'")
						.toArray(),
					", "));
			}
			builder.append(".");
		} else {
			assert !disallowed.isEmpty();
			builder.append("disable ");
			builder.append(StringUtils.join(
				disallowed.stream()
					.map(experiment -> "'" + experiment.codeName() + "'")
					.toArray(),
				", "));
		}
		return builder.toString();
	}

}
