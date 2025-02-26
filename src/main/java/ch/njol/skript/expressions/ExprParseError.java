package ch.njol.skript.expressions;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Güttinger
 */
@Name("Parse Error")
@Description("The error which caused the last <a href='#ExprParse'>parse operation</a> to fail, which might not be set if a pattern was used and the pattern didn't match the provided text at all.")
@Examples({"set {var} to line 1 parsed as integer",
		"if {var} is not set:",
		"	parse error is set:",
		"		message \"&lt;red&gt;Line 1 is invalid: %last parse error%\"",
		"	else:",
		"		message \"&lt;red&gt;Please put an integer on line 1!\""})
@Since("2.0")
public class ExprParseError extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprParseError.class, String.class, ExpressionType.SIMPLE, "[the] [last] [parse] error[all:s]");
	}

	private static final List<String> lastErrors = new ArrayList<>();
	private boolean allErrors = false;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		allErrors = parseResult.hasTag("all");
		return true;
	}
	
	@Override
	protected String[] get(final Event e) {
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
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the last parse error";
	}

	public static void addError(String error) {
		lastErrors.add(error);
	}

	public static void clearErrors() {
		lastErrors.clear();
	}
	
}
