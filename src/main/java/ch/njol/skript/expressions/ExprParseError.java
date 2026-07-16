package ch.njol.skript.expressions;

import ch.njol.skript.doc.*;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

import java.util.ArrayList;
import java.util.List;

@Name("Parse Error")
@Description("The error(s) that caused the last <a href='#ExprParse'>parse operation</a> to fail. " +
	"This may include multiple errors if multiple issues were encountered, such as when using the <a href='#ExprParse'>parse effect</a>.")
@Example("""
	set {var} to line 1 parsed as integer
	if {var} is not set:
		parse error is set:
			message "Line 1 is invalid: %last parse error%"
		else:
			message "Please put an integer on line 1!"
	""")
@Example("""
	parse {list::*} as integer
	if last parse errors are set:
		message last parse errors
	""")
@Since("2.0, INSERT VERSION (multiple)")
public class ExprParseError extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprParseError.class, String.class, ExpressionType.SIMPLE, "[the] [last] [parse] error[all:s]");
	}

	private static final List<String> lastErrors = new ArrayList<>();
	private boolean allErrors = false;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		allErrors = parseResult.hasTag("all");
		return true;
	}
	
	@Override
	protected String[] get(Event event) {
		if (lastErrors.isEmpty())
			return new String[0];

		if (allErrors)
			return lastErrors.toArray(new String[0]);

		return new String[] { lastErrors.getLast() };
	}
	
	@Override
	public boolean isSingle() {
		return !allErrors;
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the last parse error" + (allErrors ? "s" : "");
	}

	/**
	 * Adds a new error to the list of errors that will be returned by this expression.
	 * Examples of this being used are ExprParse and EffParse.
	 * @param error the error to add
	 */
	public static void addError(String error) {
		lastErrors.add(error);
	}

	/**
	 * Clears all errors from this expression. This method should be called before you begin adding new errors.
	 * Examples of this being used are ExprParse and EffParse.
	 */
	public static void clearErrors() {
		lastErrors.clear();
	}
	
}
