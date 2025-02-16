package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class EffParse extends Effect {

	static {
		Skript.registerEffect(EffParse.class, "parse %~strings% as %*classinfo%");
	}

	private Expression<String> toParse;
	private ClassInfo<?> classInfo;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		toParse = (Expression<String>) expressions[0];
		classInfo = ((Literal<ClassInfo<?>>) expressions[1]).getSingle();

		if (!toParse.getAnd()) {
			Skript.error("Can't use 'or' in a parse effect");
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
		if (toParse instanceof ExpressionList<String> toParseExpressions) {
			for (int i = 0; i < toParseExpressions.getAllExpressions().size(); i++) {
				Expression<String> expression = (Expression<String>) toParseExpressions.getAllExpressions().get(i);
				expression.changeInPlace(event, (stringToParse) -> Classes.parseSimple(stringToParse, classInfo.getC(), ParseContext.PARSE));
			}
		} else {
			toParse.changeInPlace(event, (stringToParse) -> Classes.parseSimple(stringToParse, classInfo.getC(), ParseContext.PARSE));
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "parse " + toParse + " as " + classInfo.getName().withIndefiniteArticle();
	}

}
