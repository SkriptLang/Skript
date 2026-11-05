package org.skriptlang.skript.test.elements.conditions;

import ch.njol.skript.test.runner.TestMode;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Check JUnit")
@Description({
	"Returns true if the test runner is currently running a JUnit.",
	"Useful for the EvtTestCase of JUnit exclusive syntaxes registered from within the test packages."
})
@NoDoc
public class CondRunningJUnit extends Condition {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.CONDITION, SyntaxInfo.simple(CondRunningJUnit.class, CondRunningJUnit::new,
			"running junit"));
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public boolean check(Event event) {
		return TestMode.JUNIT;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "running JUnit";
	}

}
