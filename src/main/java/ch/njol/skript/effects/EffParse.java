package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.ExprParseError;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Parse")
@Description("Parses a string or a list of strings as a type. If \"try to\" is used, the value won't be deleted if the parse fails.")
@Examples({"set {_a::*} to \"1\", \"2a\", \"3\", \"4c\", \"5\"",
			"parse {_a::*} as integer",
			"send {_a::*} # would send 1, 3 and 5",
			"send last parse errors   # would print errors about 2a and 4c"
})
@Since("INSERT VERSION")
public class EffParse extends Effect {

	static {
		Skript.registerEffect(EffParse.class, "[try:try to] parse %~strings% as %*classinfo%");
	}

	private Expression<String> toParse;
	private ClassInfo<?> classInfo;
	private boolean tryTo;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		tryTo = parseResult.hasTag("try");
		toParse = (Expression<String>) expressions[0];
		classInfo = ((Literal<ClassInfo<?>>) expressions[1]).getSingle();

		if (!toParse.getAnd()) {
			Skript.error("Can't use 'or' lists in a parse effect");
			return false;
		}

		if (classInfo.getC() == String.class) {
			Skript.error("Parsing as text is useless as only things that are already text may be parsed");
			return false;
		}

		Parser<?> parser = classInfo.getParser();
		if (parser == null || !parser.canParse(ParseContext.PARSE)) {
			Skript.error("Text cannot be parsed as " + classInfo.getName().withIndefiniteArticle());
			return false;
		}

		if (toParse instanceof ExpressionList<String> toParseExpressions) {
			for (int i = 0; i < toParseExpressions.getAllExpressions().size(); i++) {
				Expression<String> expression = (Expression<String>) toParseExpressions.getAllExpressions().get(i);
				if (!ChangerUtils.acceptsChange(expression, ChangeMode.SET, classInfo.getC())) {
					Skript.error(toParse + " can't be set to " + classInfo.getName().withIndefiniteArticle());
					return false;
				}
			}
		} else if (!ChangerUtils.acceptsChange(toParse, ChangeMode.SET, classInfo.getC())) {
			Skript.error(toParse + " can't be set to " + classInfo.getName().withIndefiniteArticle());
			return false;
		}

		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void execute(Event event) {
		Parser<?> parser = classInfo.getParser();
		assert parser != null; // checked in init()
		ExprParseError.clearErrors();

		ParseLogHandler parseLogHandler = new ParseLogHandler().start();
		try {
			if (toParse instanceof ExpressionList<String> toParseExpressions) {
				for (int i = 0; i < toParseExpressions.getAllExpressions().size(); i++) {
					Expression<String> expression = (Expression<String>) toParseExpressions.getAllExpressions().get(i);
					expression.changeInPlace(event, stringToParse -> {
						Object parsed = parser.parse(stringToParse, ParseContext.PARSE);
						if (parsed == null) {
							ExprParseError.addError(stringToParse + " could not be parsed as " + classInfo.getName().withIndefiniteArticle());
							parseLogHandler.clearError();
							return tryTo ? stringToParse : null;
						}
						return parsed;
					});
				}
			} else {
				toParse.changeInPlace(event, stringToParse -> {
					Object parsed = parser.parse(stringToParse, ParseContext.PARSE);
					if (parsed == null) {
						ExprParseError.addError(stringToParse + " could not be parsed as " + classInfo.getName().withIndefiniteArticle());
						return tryTo ? stringToParse : null;
					}
					return parsed;
				});
			}
		} finally {
			parseLogHandler.clear();
			parseLogHandler.printLog();
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (tryTo ? "try to " : "") + "parse " + toParse + " as " + classInfo.getName().withIndefiniteArticle();
	}

}
