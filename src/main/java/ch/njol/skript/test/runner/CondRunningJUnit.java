package ch.njol.skript.test.runner;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Check JUnit")
@Description({
	"Returns true if the test runner is currently running a JUnit.",
	"Useful for the EvtTestCase of JUnit exclusive syntaxes registered from within the test packages."
})
@NoDoc
public class CondRunningJUnit extends Condition {

	static {
		Skript.registerCondition(CondRunningJUnit.class, "running junit");
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
