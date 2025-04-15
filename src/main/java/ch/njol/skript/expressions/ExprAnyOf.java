package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Any Of")
@Description({
	"Returns an 'or list' composed of the given objects. For example, `any of (1, 2, and 3)` is equivalent to `1, 2, or 3`",
	"Useful when doing comparisons with variable lists."
})
@Example("if any of {_numbers::*} are 1:")
@Example("if any of {teamA::*} are within location(0, 0, 0) and location(10, 10, 10):")
public class ExprAnyOf extends WrapperExpression<Object> {

	static {
		Skript.registerExpression(ExprAnyOf.class, Object.class, ExpressionType.PATTERN_MATCHES_EVERYTHING, "(any [one]|one) of [the] %objects%");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		Expression<?> expr = LiteralUtils.defendExpression(expressions[0]);
		setExpr(expr);
		return LiteralUtils.canInitSafely(expr);
	}

	@Override
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
		return new Class[0];
	}

	@Override
	public boolean getAnd() {
		return false;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "any of " + getExpr().toString(event, debug);
	}

}
