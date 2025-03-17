package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.simplification.Simplifiable;

/**
 * @author Peter Güttinger
 */
@Name("Rounding")
@Description("Rounds numbers normally, up (ceiling) or down (floor) respectively.")
@Examples({"set {var} to rounded health of player",
		"set line 1 of the block to rounded \"%(1.5 * player's level)%\"",
		"add rounded down argument to the player's health"})
@Since("2.0")
public class ExprRound extends PropertyExpression<Number, Long> {
	static {
		Skript.registerExpression(ExprRound.class, Long.class, ExpressionType.PROPERTY,
				"(a|the|) round[ed] down %number%",
				"(a|the|) round[ed] %number%",
				"(a|the|) round[ed] up %number%");
	}
	
	int action;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		setExpr((Expression<? extends Number>) exprs[0]);
		action = matchedPattern - 1;
		return true;
	}
	
	@Override
	protected Long[] get(final Event e, final Number[] source) {
		return get(source, n -> {
			if (n instanceof Integer)
				return n.longValue();
			else if (n instanceof Long)
				return (Long) n;
			return action == -1 ? Math2.floor(n.doubleValue()) : action == 0 ? Math2.round(n.doubleValue()) : Math2.ceil(n.doubleValue());
		});
	}
	
	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	public Expression<Long> simplify(@NotNull Step step, @Nullable Simplifiable<?> source) {
		super.simplify(step, source);
		if (getExpr() instanceof Literal<? extends Number>)
			return getAsSimplifiedLiteral();
		return this;
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return (action == -1 ? "floor" : action == 0 ? "round" : "ceil") + "(" + getExpr().toString(e, debug) + ")";
	}
	
}
