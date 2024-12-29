
package ch.njol.skript.conditions;

import java.util.Arrays;
import java.util.regex.Pattern;

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

@Name("Matches")
@Description("Checks whether the defined strings match the input regexes (Regular expressions).")
@Examples({"on chat:",
	"\tif message partially matches \"\\d\":",
	"\t\tsend \"Message contains a digit!\"",
	"\tif message doesn't match \"[A-Za-z]+\":",
	"\t\tsend \"Message doesn't only contain letters!\""})
@Since("2.5.2")
public class CondMatches extends Condition {
	
	static {
		Skript.registerCondition(CondMatches.class,
			"%strings% (1¦match[es]|2¦do[es](n't| not) match) %strings%",
			"%strings% (1¦partially match[es]|2¦do[es](n't| not) partially match) %strings%");
	}
	
	@SuppressWarnings("null")
	Expression<String> strings;
	@SuppressWarnings("null")
	Expression<String> regex;
	
	boolean partial;
	
	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		strings = (Expression<String>) exprs[0];
		regex = (Expression<String>) exprs[1];
		partial = matchedPattern == 1;
		setNegated(parseResult.mark == 1);
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		String[] txt = strings.getAll(e);
		String[] regexes = regex.getAll(e);
		if (txt.length < 1 || regexes.length < 1) return false;
		boolean result;
		boolean stringAnd = strings.getAnd();
		boolean regexAnd = regex.getAnd();
		if (stringAnd) {
			if (regexAnd) {
				result = Arrays.stream(txt).allMatch((str) -> Arrays.stream(regexes).parallel().map(Pattern::compile).allMatch((pattern -> matches(str, pattern))));
			} else {
				result = Arrays.stream(txt).allMatch((str) -> Arrays.stream(regexes).parallel().map(Pattern::compile).anyMatch((pattern -> matches(str, pattern))));
			}
		} else if (regexAnd) {
			result = Arrays.stream(txt).anyMatch((str) -> Arrays.stream(regexes).parallel().map(Pattern::compile).allMatch((pattern -> matches(str, pattern))));
		} else {
			result = Arrays.stream(txt).anyMatch((str) -> Arrays.stream(regexes).parallel().map(Pattern::compile).anyMatch((pattern -> matches(str, pattern))));
		}
		return result == isNegated();
	}
	
	public boolean matches(String str, Pattern pattern) {
		return partial ? pattern.matcher(str).find() : str.matches(pattern.pattern());
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return strings.toString(e, debug) + " " + (isNegated() ? "doesn't match" : "matches") + " " + regex.toString(e, debug);
	}
	
}
