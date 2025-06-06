package ch.njol.skript.conditions;

import org.apache.commons.lang.StringUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Alphanumeric")
@Description({"Checks if the given string is alphanumeric."})
@Examples({"if the argument is not alphanumeric:",
		"	send \"Invalid name!\""})
@Since("2.4")
public class CondAlphanumeric extends Condition {
	
	static {
		Skript.registerCondition(CondAlphanumeric.class,
				"%strings% (is|are) alphanumeric",
				"%strings% (isn't|is not|aren't|are not) alphanumeric");
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
	public String toString(@Nullable Event e, boolean debug) {
		return strings.toString(e, debug) + " is" + (isNegated() ? "n't" : "") + " alphanumeric";
	}
	
}
