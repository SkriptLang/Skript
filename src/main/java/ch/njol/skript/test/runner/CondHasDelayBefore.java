package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@NoDoc
public class CondHasDelayBefore extends Condition {

	static {
		Skript.registerCondition(CondHasDelayBefore.class,
			"has delay before is[negated: not|negated:n't] (:true|:false|:unknown)");
	}

	private Kleenean expected;
	private boolean success;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(parseResult.hasTag("negated"));
		expected = Kleenean.valueOf(parseResult.tags.get(parseResult.tags.size() - 1).toUpperCase(Locale.ENGLISH));
		success = (getParser().getHasDelayBefore() == expected) ^ isNegated();
		return true;
	}

	@Override
	public boolean check(Event event) {
		return success;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "has delay before is " + (isNegated() ? "not " : "") + expected.name().toLowerCase(Locale.ENGLISH);
	}

}
