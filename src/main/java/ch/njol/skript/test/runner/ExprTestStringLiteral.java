package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Test String Literal")
@Description("Accepts only a string literal. Used for testing correct parsing & literal treatment. Returns the value.")
@NoDoc
public class ExprTestStringLiteral extends SimpleExpression<String> {

	static {
		if (TestMode.ENABLED)
			Skript.registerExpression(ExprTestStringLiteral.class, String.class, ExpressionType.SIMPLE, "test string literal %*string%");
	}

	private Expression<String> literal;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.literal = (Expression<String>) expressions[0];
		return literal instanceof LiteralString;
	}

	@Override
	protected @Nullable String[] get(Event event) {
		return literal.getArray(event);
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "test string literal " + literal.toString(event, debug);
	}

}
