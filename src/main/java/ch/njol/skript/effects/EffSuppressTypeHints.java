package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Feature;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.experiment.ExperimentData;
import org.skriptlang.skript.lang.experiment.SimpleExperimentalSyntax;

@Name("Suppress Type Hints (Experimental)")
@Description({
	"An effect to suppress local variable type hint errors for the syntax lines that follow this effect.",
	"NOTE: Suppressing type hints also prevents syntax from providing new type hints." +
		" For example, with type hints suppressed, 'set {_x} to true' would not provide 'boolean' as a type hint for '{_x}'"
})
@Example("""
	start suppressing local variable type hints
	# potentially unsafe code goes here
	stop suppressing local variable type hints
""")
@Since("2.12")
public class EffSuppressTypeHints extends Effect implements SimpleExperimentalSyntax {

	private static final ExperimentData EXPERIMENT_DATA = ExperimentData.createSingularData(Feature.TYPE_HINTS);

	static {
		Skript.registerEffect(EffSuppressTypeHints.class,
				"[stop:un]suppress [local variable] type hints",
				"(start|:stop) suppressing [local variable] type hints");
	}

	private boolean stop;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		stop = parseResult.hasTag("stop");
		getParser().getHintManager().setActive(stop);
		return true;
	}

	@Override
	protected void execute(Event event) { }

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (stop ? "stop" : "start") + " suppressing type hints";
	}

	@Override
	public ExperimentData getExperimentData() {
		return EXPERIMENT_DATA;
	}

}
