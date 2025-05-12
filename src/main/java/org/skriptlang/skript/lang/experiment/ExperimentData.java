package org.skriptlang.skript.lang.experiment;

import ch.njol.util.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Container for holding {@link Experiment}s that must be enabled or disabled to use.
 */
public class ExperimentData {

	private Experiment @Nullable [] required = null;
	private Experiment @Nullable [] disallowed = null;

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
