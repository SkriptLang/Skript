package org.skriptlang.skript.lang.experiment;

import ch.njol.skript.Skript;
import ch.njol.util.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Container for holding {@link Experiment}s that must be enabled or disabled to use.
 */
public class ExperimentData {

	private Experiment @Nullable [] required = null;
	private Experiment @Nullable [] disallowed = null;
	private @Nullable String errorMessage = null;

	public ExperimentData() {}

	/**
	 * Set the {@link Experiment}s that must be enabled in order to use.
	 * @param required The {@link Experiment}s
	 * @return This {@link ExperimentData}.
	 */
	public ExperimentData required(Experiment... required) {
		this.required = required;
		return this;
	}

	/**
	 * Set the {@link Experiment}s that must be disabled in order to use.
	 * @param disallowed The {@link Experiment}s
	 * @return This {@link ExperimentData}.
	 */
	public ExperimentData disallowed(Experiment... disallowed) {
		this.disallowed = disallowed;
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
	public Experiment @Nullable [] getRequired() {
		return required;
	}

	/**
	 * Get the {@link Experiment}s that must be disabled in order to use.
	 */
	public Experiment @Nullable [] getDisallowed() {
		return disallowed;
	}

	/**
	 * Check if this {@link ExperimentData} is valid.
	 * @return {@code True} if valid.
	 */
	public boolean isValid() {
		return required != null || disallowed != null;
	}

	/**
	 * Check if the requirements of this {@link ExperimentData} are met.
	 * @param experiments The current enabled {@link Experiment}s.
	 * @return {@code True} if the requirements were met.
	 */
	public boolean checkRequirements(ExperimentSet experiments) {
		if (!isValid())
			throw new IllegalArgumentException("An ExperimentalData must have required and/or disallowed Experiements");
		if (required != null) {
			for (Experiment experiment : required) {
				if (!experiments.hasExperiment(experiment)) {
					Skript.error(getErrorMessage());
					return false;
				}
			}
		}
		if (disallowed != null) {
			for (Experiment experiment : disallowed) {
				if (experiments.hasExperiment(experiment)) {
					Skript.error(getErrorMessage());
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Get the error message to be printed when the requirements of this {@link ExperimentData} are not met.
	 * If {@link #errorMessage} is {@code null}, will construct an error message via {@link #constructError()}.
	 */
	public String getErrorMessage() {
		return errorMessage != null ? errorMessage : constructError();
	}

	/**
	 * Construct a {@link String} combining what {@link Experiment}s need to be enabled and/or disabled in order to use.
	 */
	public String constructError() {
		StringBuilder builder = new StringBuilder();
		builder.append("This element is experimental. To use this, ");
		if (required != null) {
			builder.append("enable ");
			builder.append(StringUtils.join(
					Arrays.stream(required)
						.map(experiment -> "'" + experiment.codeName() + "'")
						.toArray(),
				", "));
			if (disallowed != null) {
				builder.append(" and disable ");
				builder.append(StringUtils.join(
					Arrays.stream(disallowed)
						.map(experiment -> "'" + experiment.codeName() + "'")
						.toArray(),
				", "));
			}
			builder.append(".");
		} else {
			assert disallowed != null;
			builder.append("disable ");
			builder.append(StringUtils.join(
				Arrays.stream(disallowed)
					.map(experiment -> "'" + experiment.codeName() + "'")
					.toArray(),
				", "));
		}
		return builder.toString();
	}

}
