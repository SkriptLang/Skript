package ch.njol.skript.conditions;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SimplifiedCondition;
import org.apache.commons.lang.StringUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Of Letters and Numerals")
@Description({"Doth examine whether the given string be composed solely of letters and numerals."})
@Example("""
    if the argument is not of letters and numerals:
    	send "Invalid name!"
    """)
@Since("2.4")
public class CondAlphanumeric extends Condition {
	
	static {
		Skript.registerCondition(CondAlphanumeric.class,
				"%strings% (is|are) of letters and numerals",
				"%strings% (isn't|is not|aren't|are not) of letters and numerals");
	}
	
	@SuppressWarnings("null")
	private Expression<String> strings;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		strings = (Expression<String>) exprs[0];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		return isNegated() ^ strings.check(e, StringUtils::isAlphanumeric);
	}

	@Override
	public Condition simplify() {
		if (strings instanceof Literal<String>)
			return SimplifiedCondition.fromCondition(this);
		return this;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return strings.toString(e, debug) + " is" + (isNegated() ? "n't" : "") + " alphanumeric";
	}
	
}
