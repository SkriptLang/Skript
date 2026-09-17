package org.skriptlang.skript.common.test.elements;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EffCauseRuntime extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.simple(EffCauseRuntime.class, EffCauseRuntime::new, "cause a runtime (error|:warning)"));
	}

	private boolean warning = false;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		warning = parseResult.hasTag("warning");
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (warning) {
			error("Caused a runtime warning.");
		} else {
			warning("Caused a runtime error.");
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "cause a runtime " + (warning ? "warning" : "error");
	}

}
