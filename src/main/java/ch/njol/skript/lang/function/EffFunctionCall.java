package ch.njol.skript.lang.function;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class EffFunctionCall extends Effect {

	private final org.skriptlang.skript.lang.function.FunctionReference<?> function;

	public EffFunctionCall(org.skriptlang.skript.lang.function.FunctionReference<?> function) {
		this.function = function;
	}

	public static EffFunctionCall parse(final String line) {
		org.skriptlang.skript.lang.function.FunctionReference<?> function = new SkriptParser(line, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT).parseFunctionReference();
		if (function != null)
			return new EffFunctionCall(function);
		return null;
	}

	@Override
	protected void execute(final Event event) {
		function.execute(event);
		if (function.function() != null)
			function.function().resetReturnValue(); // Function might have return value that we're ignoring
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return function.toString(event, debug);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		assert false;
		return false;
	}

}
