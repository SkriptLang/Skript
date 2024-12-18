package ch.njol.skript.conditions;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;

@Name("Starts/Ends With")
@Description("Checks if a text starts or ends with another.")
@Examples({"if the argument starts with \"test\" or \"debug\":",
	"\tsend \"Stop!\""})
@Since("2.2-dev36, 2.5.1 (multiple strings support)")
public class CondStartsEndsWith extends Condition {
	
	static {
		Skript.registerCondition(CondStartsEndsWith.class,
			"%strings% (start|1¦end)[s] with %strings%",
			"%strings% (doesn't|does not|do not|don't) (start|1¦end) with %strings%");
	}
	
	@SuppressWarnings("null")
	private Expression<String> strings;
	@SuppressWarnings("null")
	private Expression<String> affix;
	private boolean usingEnds;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		strings = (Expression<String>) exprs[0];
		affix = (Expression<String>) exprs[1];
		usingEnds = parseResult.mark == 1;
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		String[] affixes = this.affix.getAll(e);
		if (affixes.length < 1)
			return false;
		return strings.check(e,
			string -> {
				if (usingEnds) { // Using 'ends with'
					if (this.affix.getAnd()) {
						for (String str : affixes) {
							if (!string.endsWith(str))
								return false;
						}
						return true;
					} else {
						for (String str : affixes) {
							if (string.endsWith((str)))
								return true;
						}
					}
				} else { // Using 'starts with'
					if (this.affix.getAnd()) {
						for (String str : affixes) {
							if (!string.startsWith(str))
								return false;
						}
						return true;
					} else {
						for (String str : affixes) {
							if (string.startsWith((str)))
								return true;
						}
					}
				}
				return false;
			},
			isNegated());
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (isNegated())
			return strings.toString(e, debug) + " doesn't " + (usingEnds ? "end" : "start") + " with " + affix.toString(e, debug);
		else
			return strings.toString(e, debug) + (usingEnds ? " ends" : " starts") + " with " + affix.toString(e, debug);
	}
	
}
