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

import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.patterns.SkriptPattern;

import java.util.Objects;

/**
 * An optional, potentially-experimental feature enabled per-script with the {@code using X} syntax.
 * Experiments provided by Skript itself are found in {@link Feature}.
 * This can also represent an unknown experiment 'used' by a script that was not declared or registered
 * by Skript or any of its addons.
 */
public interface Experiment {

	static Experiment unknown(String text) {
		return new UnmatchedExperiment(text);
	}

	/**
	 * @return The patterns that can be written in the `using` structure.
	 */
	default String[] patterns() {
		return new String[] {this.codeName()};
	}

	/**
	 * A simple, printable code-name for this pattern for warnings and debugging.
	 * Ideally, this should be matched by one of the {@link #patterns()} entries.
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

	SkriptPattern compiledPattern();

	default boolean matches(String text) {
		return this.compiledPattern().match(text) != null;
	}

}

/**
 * The dummy class for an unmatched experiment.
 * This is something that was 'used' by a file but was never registered with Skript.
 * These are kept so that they *can* be tested for (e.g. by a third-party extension that uses a post-registration
 * experiment system).
 */
class UnmatchedExperiment implements Experiment {

	private final String codeName;
	private final String[] patterns;
	private final SkriptPattern compiledPattern;

	UnmatchedExperiment(String codeName) {
		this.codeName = codeName;
		this.patterns = new String[] {codeName};
		this.compiledPattern = PatternCompiler.compile(codeName);
	}

	@Override
	public String codeName() {
		return codeName;
	}

	@Override
	public String[] patterns() {
		return patterns;
	}

	@Override
	public LifeCycle phase() {
		return LifeCycle.UNKNOWN;
	}

	@Override
	public SkriptPattern compiledPattern() {
		return compiledPattern;
	}

	@Override
	public boolean isKnown() {
		return false;
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
