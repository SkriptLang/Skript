package ch.njol.skript.expressions.base;

import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;
import org.skriptlang.skript.lang.converter.Converter;

/**
 * A base class for property expressions that requires only few overridden methods
 * 
 * @see PropertyExpression
 * @see PropertyExpression#register(Class, Class, String, String)
 */
public abstract class SimplePropertyExpression<F, T> extends PropertyExpression<F, T> implements Converter<F, T>, SyntaxRuntimeErrorProducer {

	private Node node;
	protected String rawExpr;
  
	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (LiteralUtils.hasUnparsedLiteral(expressions[0])) {
			setExpr(LiteralUtils.defendExpression(expressions[0]));
			return LiteralUtils.canInitSafely(getExpr());
		}
		setExpr((Expression<? extends F>) expressions[0]);
		node = getParser().getNode();
		rawExpr = parseResult.expr;
		return true;
	}

	@Override
	@Nullable
	public abstract T convert(F from);

	@Override
	protected T[] get(Event event, F[] source) {
		return super.get(source, this);
	}

	@Override
	public Node getNode() {
		return node;
	}

	/**
	 * Used to collect the property type used in the register method.
	 * This forms the toString of this SimplePropertyExpression.
	 * 
	 * @return The name of the type used when registering this SimplePropertyExpression.
	 */
	protected abstract String getPropertyName();

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return getPropertyName() + " of " + getExpr().toString(event, debug);
	}

}
