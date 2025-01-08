package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@NoDoc
public class EffExceptionDebug extends Effect {
	
	static {
		Skript.registerEffect(EffExceptionDebug.class, "cause exception");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}
	

	@Override
	protected void execute(Event event) {
		Skript.exception("Created by a script (debugging)...");
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "cause exception";
	}
	
}
