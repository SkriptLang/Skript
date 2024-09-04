package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.VariableString;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Has Annotations")
@Description({
	"Returns true if any annotations are visible to this line."
})
@NoDoc
public class CondHasAnnotations extends Condition {

	static {
		if (TestMode.ENABLED)
			Skript.registerCondition(CondHasAnnotations.class, "annotation %string% [not:not] present");
	}

	private Expression<?> pattern;
	private boolean result, negated;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		String pattern;
		this.pattern = exprs[0];
		if (this.pattern instanceof VariableString) {
			VariableString string = (VariableString) exprs[0];
			if (!string.isSimple())
				return false;
			pattern = string.toString(null);
		} else {
			pattern = exprs[0].toString(null, false);
		}
		this.result = (negated = parseResult.hasTag("not")) ^ this.getParser().hasAnnotationMatching(pattern);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return result;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "annotation" + pattern + (negated ? " not " : " ") + "present";
	}

}
