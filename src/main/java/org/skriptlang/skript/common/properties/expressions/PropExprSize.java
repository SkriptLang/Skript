package org.skriptlang.skript.common.properties.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.*;
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
import org.skriptlang.skript.lang.properties.PropertyHandler.ExpressionPropertyHandler;

@Name("Size")
@Description("""
	The size of something.
	Using 'size of {list::*}' will return the length of the list, so if you want the sizes of the things inside the \
	lists, use 'sizes of {list::*}'.
	""")
@Example("message \"There are %size of all players% players online!\"")
@Since({"1.0", "2.13 (sizes of)"})
@RelatedProperty("size")
public class PropExprSize extends PropertyBaseExpression<ExpressionPropertyHandler<?, ?>> {

	static {
		register(PropExprSize.class, "size[:s]", "objects");
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
			this.exprs = PropExprAmount.asExprList(expressions[0]);
			return LiteralUtils.canInitSafely(this.exprs);
		}
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		if (useProperties)
			return super.get(event);
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
		return Property.SIZE;
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
		return "size of " + this.exprs.toString(event, debug);
	}

}
