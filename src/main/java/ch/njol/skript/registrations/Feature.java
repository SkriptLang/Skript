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
	EXAMPLES("examples", LifeCycle.STABLE),
	QUEUES("queues", LifeCycle.EXPERIMENTAL),
	FOR_EACH_LOOPS("for loop", LifeCycle.EXPERIMENTAL, "for [each] loop[s]"),
	SCRIPT_REFLECTION("reflection", LifeCycle.EXPERIMENTAL, "[script] reflection"),
	;

	private final String codeName;
	private final LifeCycle phase;
	private final SkriptPattern compiledPattern;

	Feature(String codeName, LifeCycle phase, String... patterns) {
		this.codeName = codeName;
		this.phase = phase;
		this.compiledPattern = switch (patterns.length) {
			case 0 -> PatternCompiler.compile(codeName);
			case 1 -> PatternCompiler.compile(patterns[0]);
			default -> PatternCompiler.compile('(' + String.join("|", patterns) + ')');
		};
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
