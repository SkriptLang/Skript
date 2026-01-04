package org.skriptlang.skript.common.properties.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseExpression;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Amount")
@Description("""
	The amount of something.
	Using 'amount of {list::*}' will return the length of the list, so if you want the amounts of the things inside the \
	lists, use 'amounts of {list::*}'.
	""")
@Example("message \"There are %amount of all players% players online!\"")
@Example("if amount of player's tool > 5:")
@Example("if amounts of player's tool and player's offhand tool > 5:")
@Since({"1.0", "2.13 (amounts of)"})
@RelatedProperty("amount")
public class PropExprAmount extends PropertyBaseExpression<ExpressionPropertyHandler<?, ?>> {

	public static void register(SyntaxRegistry registry, Origin origin) {
		registry.register(SyntaxRegistry.EXPRESSION,
			PropertyExpression.infoBuilder(PropExprAmount.class, Object.class, "amount[:s]", "objects", false)
				.origin(origin)
				.supplier(PropExprAmount::new)
				.build());
	}

	private ExpressionList<?> exprs;
	private boolean useProperties;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		// amount[s] of x -> property
		// amounts of x, y -> property
		// amount of x, y -> list length
		useProperties = parseResult.hasTag("s") || expressions[0].isSingle();
		if (useProperties) {
			return super.init(expressions, matchedPattern, isDelayed, parseResult);
		} else {
			// if exprlist or varlist, count elements
			this.exprs = asExprList(expressions[0]);
			return LiteralUtils.canInitSafely(this.exprs);
		}
	}

	/**
	 * Wraps non-expressionlists in a expression list.
	 * @param expr The expression to wrap
	 * @return An ExpressionList containing the original expression, or the original expression if it was already a list.
	 *   		Null if the expression could not be converted to a valid expression list.
	 */
	@ApiStatus.Internal
	public static ExpressionList<?> asExprList(Expression<?> expr) {
		ExpressionList<?> exprs;
		if (expr instanceof ExpressionList<?> exprList) {
			exprs = exprList;
		} else {
			exprs = new ExpressionList<>(new Expression<?>[]{ expr }, Object.class, false);
		}
		return (ExpressionList<?>) LiteralUtils.defendExpression(exprs);
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		if (useProperties)
			return super.get(event);
		return new Long[]{(long) exprs.getArray(event).length};
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (useProperties)
			return super.acceptChange(mode);
		return null;
	}

	@Override
	public @NotNull Property<ExpressionPropertyHandler<?, ?>> getProperty() {
		return Property.AMOUNT;
	}

	@Override
	public boolean isSingle() {
		if (useProperties)
			return super.isSingle();
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		if (useProperties)
			return super.getReturnType();
		return Long.class;
	}

	@Override
	public Class<?>[] possibleReturnTypes() {
		if (useProperties)
			return super.possibleReturnTypes();
		return new Class[]{Long.class};
	}

	@Override
	public String toString(Event event, boolean debug) {
		if (useProperties)
			return super.toString(event, debug);
		return "amount of " + this.exprs.toString(event, debug);
	}

}
