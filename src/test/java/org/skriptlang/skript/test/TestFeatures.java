package org.skriptlang.skript.test;

import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.patterns.SkriptPattern;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.lang.experiment.ExperimentRegistry;
import org.skriptlang.skript.lang.experiment.LifeCycle;

/**
 * Features available only in test scripts.
 */
public enum TestFeatures implements Experiment {

	EXAMPLE_FEATURE("example feature", LifeCycle.STABLE),

	DEPRECATED_FEATURE("deprecated feature", LifeCycle.DEPRECATED),

	TEST_FEATURE("test", LifeCycle.EXPERIMENTAL, "test[ing]", "fizz[ ]buzz");

	private final String codeName;
	private final LifeCycle phase;
	private final SkriptPattern compiledPattern;

	TestFeatures(String codeName, LifeCycle phase, String... patterns) {
		this.codeName = codeName;
		this.phase = phase;
		this.compiledPattern = PatternCompiler.compile(switch (patterns.length) {
			case 0 -> codeName;
			case 1 -> patterns[0];
			default -> '(' + String.join("|", patterns) + ')';
		});
	}

	TestFeatures(String codeName, LifeCycle phase) {
		this(codeName, phase, codeName);
	}

	public static void register(SkriptAddon addon) {
		addon.registry(ExperimentRegistry.class).registerAll(addon, values());
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

}
