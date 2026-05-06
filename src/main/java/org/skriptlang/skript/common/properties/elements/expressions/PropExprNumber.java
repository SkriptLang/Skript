package org.skriptlang.skript.common.properties.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseExpression;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Number Of")
@Description("""
	The number of something.
	Using 'number of {list::*}' will return the length of the list, so if you want the numbers of the things inside the \
	lists, use 'numbers of {list::*}'.
	""")
@Example("message \"There are %number of all players% players online!\"")
@Since({"1.0", "2.13 (numbers of)"})
@RelatedProperty("number")
public class PropExprNumber extends PropertyBaseExpression<ExpressionPropertyHandler<?, ?>> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION,
			PropertyExpression.infoBuilder(PropExprNumber.class, Object.class, "number[:s]", "objects", false)
				.supplier(PropExprNumber::new)
				.build());
	}

	private ExpressionList<?> exprs;
	private @Nullable Variable<?> list;
	private boolean useProperties;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		// number[s] of x -> property
		// numbers of x, y -> property
		// number of x, y -> list length
		useProperties = parseResult.hasTag("s") || expressions[0].isSingle();
		if (useProperties)
			return super.init(expressions, matchedPattern, isDelayed, parseResult);

		// if exprlist or varlist, count elements
		this.exprs = PropExprAmount.asExprList(expressions[0]);
		if (expressions[0] instanceof Variable<?> variable)
			this.list = variable;
		return LiteralUtils.canInitSafely(this.exprs);
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		if (useProperties)
			return super.get(event);

		if (list != null)
			return new Long[]{(long) list.size(event)};

		return new Long[]{(long) exprs.getArray(event).length};
	}

	@Override
	public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
		if (useProperties)
			return super.acceptChange(mode);
		return null;
	}

	@Override
	public @NotNull Property<ExpressionPropertyHandler<?, ?>> getProperty() {
		return Property.NUMBER;
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
		return "number of " + this.exprs.toString(event, debug);
	}

}
