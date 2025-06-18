package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Suppress Type Hints")
@Description({
	"An effect to suppress local variable type hint errors for the syntax lines that follow this effect.",
	"NOTE: Suppressing type hints also prevents them from being collected."
})
@Example("""
	start suppressing variable type hints
	# unsafe code goes here
	stop suppressing variable type hints
""")
@Since("INSERT VERSION")
public class EffSuppressTypeHints extends Effect {

	static {
		Skript.registerEffect(EffSuppressTypeHints.class,
				"[stop:un]suppress [[local] variable] type hints",
				"(start|:stop) suppressing [[local] variable] type hints");
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

}
