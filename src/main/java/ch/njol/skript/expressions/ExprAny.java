package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Any Element")
@Description("Returns any element from a list and forces the 'or' list behavior on expressions.")
@Example("""
	set {_list::*} to 1, 4, 5
	broadcast whether any {_list::*} is evenly divisible by 2 # true
	""")
@Since("INSERT VERSION")
public class ExprAny extends WrapperExpression<Object> {

	static {
		Skript.registerExpression(ExprAny.class, Object.class, ExpressionType.PROPERTY, "any %~objects%");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (expressions[0].isSingle()) {
			Skript.error("The 'any' expression cannot be used with a single value.");
			return false;
		}
		setExpr(expressions[0]);
		return true;
	}

	@Override
	public boolean getAnd() {
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "any " + getExpr().toString(event, debug);
	}

}
