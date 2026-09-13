package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.ExprParseError;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

@Name("Parse")
@Description("Parses a string or a list of strings as a type. If \"try to\" is used, " +
	"the existing value won't be deleted if the attempt to parse fails.")
@Example("""
	set {_a::*} to "1", "2a", "3", "4c", "5"
	parse {_a::*} as integer
	send {_a::*} # would send 1, 3 and 5
	send last parse errors # would print errors about 2a and 4c
	""")
@Since("INSERT VERSION")
public class EffParse extends Effect {

	static {
		Skript.registerEffect(EffParse.class, "[try:(try|attempt) to] parse %~objects% as %*classinfo%");
	}

	private List<Expression<?>> exprs;
	private ClassInfo<?> classInfo;
	private Parser<?> parser;
	private boolean tryTo;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		tryTo = parseResult.hasTag("try");
		Expression<?> toParse = expressions[0];
		classInfo = ((Literal<ClassInfo<?>>) expressions[1]).getSingle();

		if (!toParse.getAnd()) {
			Skript.error("Can't use 'or' lists in a parse effect");
			return false;
		}

		if (classInfo.getC() == String.class) {
			Skript.error("Parsing as text is useless as only things that are already text may be parsed");
			return false;
		}

		parser = classInfo.getParser();
		if (parser == null || !parser.canParse(ParseContext.PARSE)) {
			Skript.error("Text cannot be parsed as " + classInfo.getName().withIndefiniteArticle());
			return false;
		}

		if (toParse instanceof ExpressionList<?> toParseExpressions) {
			exprs = (List<Expression<?>>) toParseExpressions.getAllExpressions();
			for (Expression<?> expression : exprs) {
				if (!(expression instanceof Variable<?>)) {
					Skript.error(expression + " can't be used here as it's not a variable");
					return false;
				}
			}
		} else {
			exprs = List.of(toParse);

			if (!(toParse instanceof Variable<?>)) {
				Skript.error(toParse + " can't be used here as it's not a variable");
				return false;
			}
		}

		return true;
	}

	@Override
	protected void execute(Event event) {
		ExprParseError.clearErrors();

		ParseLogHandler parseLogHandler = new ParseLogHandler().start();
		try {
			for (Expression<?> expression : exprs) {
				expression.changeInPlace(event, val -> {
					if (!(val instanceof String stringToParse)) {
						return tryTo ? val : null;
					}
					Object parsed = parser.parse(stringToParse, ParseContext.PARSE);
					if (parsed == null) {
						ExprParseError.addError(stringToParse + " could not be parsed as " + classInfo.getName().withIndefiniteArticle());
						parseLogHandler.clearError();
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
		String exprs = this.exprs.stream()
			.map(e -> e.toString(event, debug))
			.collect(Collectors.joining());
		String classInfo = this.classInfo.getName().withIndefiniteArticle();
		return (tryTo ? "try to " : "") + "parse " + exprs + " as " + classInfo;
	}

}
