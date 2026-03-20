package org.skriptlang.skript.lang.parsing.sites;

import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.parsing.constraints.Constraints;

/**
 * Base class for {@link ParsingSite} implementations that need lazy-cached constraints.
 * Subclasses implement {@link #buildConstraints()} and call {@link #invalidateConstraints()}
 * in any setter that affects the result.
 */
public abstract class AbstractParsingSite implements ParsingSite {

	private @Nullable Constraints cachedConstraints;

	@Override
	public final Constraints constraints() {
		if (cachedConstraints == null) {
			cachedConstraints = buildConstraints();
		}
		return cachedConstraints;
	}

	/**
	 * Builds the {@link Constraints} for this site. Called lazily and cached until invalidated.
	 */
	protected abstract Constraints buildConstraints();

	/**
	 * Clears the cached constraints, forcing a rebuild on the next call to {@link #constraints()}.
	 * Must be called by subclass setters that affect the result of {@link #buildConstraints()}.
	 */
	protected void invalidateConstraints() {
		cachedConstraints = null;
	}

}
