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
import org.skriptlang.skript.lang.script.Annotated;
import org.skriptlang.skript.lang.structure.Structure;

@Name("Has Annotations")
@Description({
	"Returns true if any annotations are visible to this line."
})
@NoDoc
public class CondHasAnnotations extends Condition {

	static {
		if (TestMode.ENABLED)
			Skript.registerCondition(CondHasAnnotations.class,
				"annotation %string% [not:not] present [structural:on [the] structure]");
	}

	private Expression<?> pattern;
	private boolean result, negated, structural;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		String pattern;
		this.pattern = exprs[0];
		this.structural = parseResult.hasTag("structural");
		if (this.pattern instanceof VariableString) {
			VariableString string = (VariableString) exprs[0];
			if (!string.isSimple())
				return false;
			pattern = string.toString(null);
		} else {
			pattern = exprs[0].toString(null, false);
		}
		Annotated annotated = structural ? this.getParser().getCurrentStructure() : this.getParser();

		assert annotated != null;
		this.result = (negated = parseResult.hasTag("not")) ^ annotated.hasAnnotationMatching(pattern);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return result;
	}

	@Override
	public String toString(Event event, boolean debug) {
		return "annotation " + pattern
			+ (negated ? " not " : " ") + "present"
			+ (structural ? " on structure" : "");
	}

}
