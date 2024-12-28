package ch.njol.skript.registrations;

import ch.njol.skript.SkriptAddon;
import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.patterns.SkriptPattern;
import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.lang.experiment.ExperimentRegistry;
import org.skriptlang.skript.lang.experiment.LifeCycle;

/**
 * Experimental feature toggles as provided by Skript itself.
 */
public enum Feature implements Experiment {
	FOR_EACH_LOOPS("for loop", LifeCycle.EXPERIMENTAL, "for [each] loop[s]")
	;

	private final String codeName;
	private final LifeCycle phase;
	private final SkriptPattern compiledPattern;

	Feature(String codeName, LifeCycle phase, String... patterns) {
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
				this.compiledPattern = PatternCompiler.compile('(' + String.join("|", patterns) + ')');
				break;
		}
	}

	Feature(String codeName, LifeCycle phase) {
		this(codeName, phase, codeName);
	}

	public static void registerAll(SkriptAddon addon, ExperimentRegistry manager) {
		for (Feature value : values()) {
			manager.register(addon, value);
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

}
