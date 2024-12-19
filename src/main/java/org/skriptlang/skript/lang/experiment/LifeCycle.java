package org.skriptlang.skript.lang.experiment;

/**
 * The life cycle phase of an {@link Experiment}.
 */
public enum LifeCycle {
	/**
	 * A feature that is expected to be safe and (at least) semi-permanent.
	 * This can be used for long-term features that are kept behind toggles to prevent breaking changes.
	 */
	STABLE(false),
	/**
	 * An experimental, preview feature designed to be used with caution.
	 * Features in the experimental phase may be subject to changes or removal at short notice.
	 */
	EXPERIMENTAL(false),
	/**
	 * A feature at the end of its life cycle, being prepared for removal.
	 * Scripts will report a deprecation warning on load if a deprecated feature is used.
	 */
	DEPRECATED(true),
	/**
	 * Represents a feature that was previously opt-in (or experimental) but is now a part of the default set.
	 * I.e. it no longer needs to be enabled using a feature flag.
	 * This will provide a little note to the user on load informing them they no longer need to
	 * use this feature flag.
	 */
	MAINSTREAM(true),
	/**
	 * Represents an unregistered, unknown feature.
	 * This occurs when a user tags a script as {@code using X}, where {@code X} is not a registered
	 * feature provided by any addon or extension.
	 * Scripts will report a warning on load if an unknown feature is used, but this will not prevent
	 * the loading cycle.
	 */
	UNKNOWN(true);

	private final boolean warn;

	LifeCycle(boolean warn) {
		this.warn = warn;
	}

	/**
	 * @return Whether using a feature of this type will produce a warning on load.
	 */
	public boolean warn() {
		return warn;
	}

}
