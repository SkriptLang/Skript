package org.skriptlang.skript.lang.experiment;

import ch.njol.skript.Skript;
import ch.njol.util.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Container for holding {@link Experiment}s that must be enabled or disabled to use.
 */
public class ExperimentData {

	/**
	 * Create and return a new {@link Builder}.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Create a new {@link ExperimentData} with only a singular required {@link Experiment}
	 * @param experiment The required {@link Experiment}.
	 * @return {@link ExperimentData}
	 */
	public static ExperimentData createSingularData(Experiment experiment) {
		return builder().required(experiment).build();
	}

	private final Set<Experiment> required;
	private final Set<Experiment> disallowed;
	private final String errorMessage;

	/**
	 * @param required {@link Set} of {@link Experiment}s required to be enabled.
	 * @param disallowed {@link Set} of {@link Experiment}s required to be disabled.
	 * @param errorMessage {@link String} used to error in {@link #checkRequirementsAndError(ExperimentSet)}
	 *                     If {@code null}, will use {@link #constructError()}.
	 */
	private ExperimentData(Set<Experiment> required, Set<Experiment> disallowed, @Nullable String errorMessage) {
		this.required = required;
		this.disallowed = disallowed;
		this.errorMessage = errorMessage != null ? errorMessage : constructError();
	}

	/**
	 * Get the {@link Experiment}s that must be enabled in order to use.
	 */
	public @UnmodifiableView Set<Experiment> getRequired() {
		return Collections.unmodifiableSet(required);
	}

	/**
	 * Get the {@link Experiment}s that must be disabled in order to use.
	 */
	public @UnmodifiableView Set<Experiment> getDisallowed() {
		return Collections.unmodifiableSet(disallowed);
	}

	/**
	 * Get the {@link String} used to error when {@link #checkRequirementsAndError(ExperimentSet)} fails.
	 * If the {@link #errorMessage} was not manually set when building {@link Builder}, uses the message from
	 * {@link #constructError()}.
	 */
	public String getErrorMessage()  {
		return errorMessage;
	}

	/**
	 * Check if the requirements of this {@link ExperimentData} are met.
	 * @param experiments The current enabled {@link Experiment}s.
	 * @return {@code True} if the requirements were met.
	 */
	public boolean checkRequirements(ExperimentSet experiments) {
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
	 * If the requirements are not met, will produce a {@link Skript#error(String)} using {@link #errorMessage}.
	 * @param experiments The current enabled {@link Experiment}s.
	 * @return {@code True} if the requirements were met.
	 */
	public boolean checkRequirementsAndError(ExperimentSet experiments) {
		if (!checkRequirements(experiments)) {
			Skript.error(errorMessage);
			return false;
		}
		return true;
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

	public static class Builder {

		private Set<Experiment> required = new HashSet<>();
		private Set<Experiment> disallowed = new HashSet<>();
		private @Nullable String errorMessage = null;

		public Builder() {}

		/**
		 * Set the {@link Experiment}s that must be enabled in order to use.
		 * @param required The {@link Experiment}s
		 * @return This {@link Builder}.
		 */
		public Builder required(Experiment... required) {
			this.required = Arrays.stream(required).collect(Collectors.toSet());
			return this;
		}

		/**
		 * Get the {@link Experiment}s that must be enabled in order to use.
		 */
		public Set<Experiment> required() {
			return required;
		}

		/**
		 * Set the {@link Experiment}s that must be disabled in order to use.
		 * @param disallowed The {@link Experiment}s
		 * @return This {@link Builder}.
		 */
		public Builder disallowed(Experiment... disallowed) {
			this.disallowed = Arrays.stream(disallowed).collect(Collectors.toSet());
			return this;
		}

		/**
		 * Get the {@link Experiment}s that must be disabled in order to use.
		 */
		public Set<Experiment> disallowed() {
			return disallowed;
		}

		/**
		 * Set the error message to be printed if the requirements are not met.
		 * @param errorMessage The error message.
		 * @return This {@link Builder}.
		 */
		public Builder errorMessage(@Nullable String errorMessage) {
			this.errorMessage = errorMessage;
			return this;
		}

		/**
		 * Get the error message to be printed when the requirements are not met.
		 */
		public @Nullable String errorMessage() {
			return errorMessage;
		}

		/**
		 * Finalize this {@link Builder} and get the built {@link ExperimentData}.
		 */
		public ExperimentData build() {
			if (required.isEmpty() && disallowed.isEmpty()) {
				throw new IllegalArgumentException("Must have required and/or disallowed Experiments.");
			} else if (!required.isEmpty() && !disallowed.isEmpty()) {
				for (Experiment req : required) {
					if (disallowed.contains(req))
						throw new IllegalArgumentException("An Experiment can not be both required and disallowed: '" + req + "'");
				}
			}
			return new ExperimentData(required, disallowed, errorMessage);
		}

	}

}
