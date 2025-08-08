package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@NoDoc
public class EffTestExprList extends Effect {

	static {
		Skript.registerEffect(EffTestExprList.class, "test expr list %number/booleans%");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		return true;
	}

	@Override
	protected void execute(Event event) {
		// do nothing
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "test expr list";
	}

}
