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
package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.patterns.SkriptPattern;
import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.lang.experiment.ExperimentRegistry;
import org.skriptlang.skript.lang.experiment.LifeCycle;

/**
 * Features available only in test scripts.
 */
public enum TestFeatures implements Experiment {
	EXAMPLE_FEATURE("example feature", LifeCycle.STABLE),
	DEPRECATED_FEATURE("deprecated feature", LifeCycle.DEPRECATED),
	TEST_FEATURE("test", LifeCycle.EXPERIMENTAL, "test[ing]", "fizz[ ]buzz")
	;

	private final String codeName;
	private final LifeCycle phase;
	private final String[] patterns;
	private final SkriptPattern compiledPattern;

	TestFeatures(String codeName, LifeCycle phase, String... patterns) {
		this.codeName = codeName;
		this.phase = phase;
		this.patterns = patterns;
		switch (patterns.length) {
			case 0:
				this.compiledPattern = PatternCompiler.compile(codeName);
				break;
			case 1:
				this.compiledPattern = PatternCompiler.compile(patterns[0]);
				break;
			default:
				this.compiledPattern = PatternCompiler.compile('(' + String.join("|", patterns) + ')');
				break;
		}
	}

	TestFeatures(String codeName, LifeCycle phase) {
		this(codeName, phase, codeName);
	}

	public static void registerAll(SkriptAddon addon, ExperimentRegistry manager) {
		for (TestFeatures value : values()) {
			manager.register(addon, value);
		}
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
		return phase;
	}

	@Override
	public SkriptPattern compiledPattern() {
		return compiledPattern;
	}

	static {
		registerAll(Skript.getAddonInstance(), Skript.experiments());
	}

}
