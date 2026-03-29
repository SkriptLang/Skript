package ch.njol.skript.expressions;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Interpretation Error")
@Description("The error which caused the last <a href='#ExprParse'>interpretation</a> to fail, which perchance may not be set if a pattern was employed and the pattern matched not the provided text at all.")
@Example("""
    set {var} to line 1 interpreted as integer
    if {var} is not set:
    	parse error is set:
    		message "<red>Line 1 is invalid: %last parse error%"
    	else:
    		message "<red>Prithee place an integer upon line 1!"
    """)
@Since("2.0")
public class ExprParseError extends SimpleExpression<String> {
	static {
		Skript.registerExpression(ExprParseError.class, String.class, ExpressionType.SIMPLE, "[the] [last] [parse] error");
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		return true;
	}
	
	@Override
	protected String[] get(final Event e) {
		return ExprParse.lastError == null ? new String[0] : new String[] {ExprParse.lastError};
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the last parse error";
	}
	
}
