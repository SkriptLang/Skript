package org.skriptlang.skript.common.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseExpression;
import org.skriptlang.skript.lang.properties.PropertyHandler;

@Name("Size")
@Description("""
	The size of something.
	Using 'size of {list::*}' will return the length of the list, so if you want the sizes of the things inside the \
	lists, use 'sizes of {list::*}'.
	""")
@Example("message \"There are %size of all players% players online!\"")
@Since({"1.0", "INSERT VERSION (sizes of)"})
public class PropExprSize extends PropertyBaseExpression<PropertyHandler.ExpressionPropertyHandler<?, ?>> {

	static {
		register(org.skriptlang.skript.common.expressions.PropExprAmount.class, "size[:s]", "objects");
	}

	private ExpressionList<?> exprs;
	private boolean useProperties;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		// size[s] of x -> property
		// sizes of x, y -> property
		// size of x, y -> list length
		useProperties = parseResult.hasTag("s") || expressions[0].isSingle();
		if (useProperties) {
			return super.init(expressions, matchedPattern, isDelayed, parseResult);
		} else {
			// if exprlist or varlist, count elements
			if (expressions[0] instanceof ExpressionList<?> exprList) {
				this.exprs = exprList;
			} else {
				this.exprs = new ExpressionList<>(new Expression<?>[]{ expressions[0] }, Object.class, false);
			}
			this.exprs = (ExpressionList<?>) LiteralUtils.defendExpression(this.exprs);
			if (!LiteralUtils.canInitSafely(this.exprs)) {
				return false;
			}
			if (this.exprs.isSingle()) {
				Skript.error("'" + this.exprs.toString(null, Skript.debug()) + "' can only ever have one value at most, thus the 'size of ...' expression is useless. Use '... exists' instead to find out whether the expression has a value.");
				return false;
			}
		}
		return true;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
		if (useProperties)
			return super.acceptChange(mode);
		return null;
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
	protected Object @Nullable [] get(Event event) {
		if (useProperties)
			return super.get(event);
		return new Long[]{(long) exprs.getArray(event).length};
	}

	@Override
	public @NotNull Property<PropertyHandler.ExpressionPropertyHandler<?, ?>> getProperty() {
		return Property.SIZE;
	}

	@Override
	public boolean isSingle() {
		if (useProperties)
			return super.isSingle();
		return true;
	}

	@Override
	public String toString(Event event, boolean debug) {
		if (useProperties)
			return super.toString(event, debug);
		return "size of " + this.exprs.toString(event, debug);
	}

}
