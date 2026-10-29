package org.skriptlang.skript.test.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;

@Name("Objectives")
@Description("An effect to setup required objectives for JUnit tests to complete.")
@NoDoc
public class EffObjectives extends Effect {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EFFECT, SyntaxInfo.simple(EffObjectives.class, EffObjectives::new,
			"ensure [[junit] test] %string% completes [(objective|trigger)[s]] %strings%",
			"complete [(objective|trigger)[s]] %strings% (for|on) [[junit] test] %string%"));
	}

	private Expression<String> junit, objectives;
	private boolean setup;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		objectives = (Expression<String>) exprs[matchedPattern ^ 1];
		junit = (Expression<String>) exprs[matchedPattern];
		setup = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		String junit = this.junit.getSingle(event);
		assert junit != null;
		String[] objectives = this.objectives.getArray(event);
		assert objectives.length > 0;
		if (setup) {
			SkriptJUnitTest.requirements.putAll(junit, Arrays.asList(objectives));
		} else {
			SkriptJUnitTest.completeness.putAll(junit, Arrays.asList(objectives));
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (setup)
			return "ensure junit test " + junit.toString(event, debug) + " completes objectives " + objectives.toString(event, debug);
		return "complete objectives " + objectives.toString(event, debug) + " on junit test " + junit.toString(event, debug);
	}

}
