package ch.njol.skript.expressions.base;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

/**
 * Represents an expression which is a wrapper of another one. Remember to set the wrapped expression in the constructor ({@link #WrapperExpression(SimpleExpression)})
 * or with {@link #setExpr(Expression)} in {@link SyntaxElement#init(Expression[], int, Kleenean, ParseResult) init()}.<br/>
 * If you override {@link #get(Event)} you must override {@link #iterator(Event)} as well.
 * 
 * @author Peter Güttinger
 */
public abstract class WrapperExpression<T> extends SimpleExpression<T> {

	private Expression<? extends T> expr;

	@SuppressWarnings("null")
	protected WrapperExpression() {}

	public WrapperExpression(SimpleExpression<? extends T> expr) {
		this.expr = expr;
	}

	/**
	 * Sets wrapped expression. Parser instance is automatically copied from
	 * this expression.
	 * @param expr Wrapped expression.
	 */
	protected void setExpr(Expression<? extends T> expr) {
		this.expr = expr;
	}

	public Expression<?> getExpr() {
		return expr;
	}

	@Override
	protected T[] get(Event event) {
		return expr.getArray(event);
	}

	@Override
	@Nullable
	public Iterator<? extends T> iterator(Event event) {
		return expr.iterator(event);
	}

	@Override
	public boolean isSingle() {
		return expr.isSingle();
	}

	@Override
	public boolean getAnd() {
		return expr.getAnd();
	}

	@Override
	public Class<? extends T> getReturnType() {
		return expr.getReturnType();
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		return expr.acceptChange(mode);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		expr.change(event, delta, mode);
	}

	@Override
	public boolean setTime(int time) {
		return expr.setTime(time);
	}

	@Override
	public int getTime() {
		return expr.getTime();
	}

	@Override
	public boolean returnNestedStructures(boolean nested) {
		return expr.returnNestedStructures(nested);
	}

	@Override
	public boolean returnsNestedStructures() {
		return expr.returnsNestedStructures();
	}

	@Override
	public boolean isDefault() {
		return expr.isDefault();
	}

	/**
	 * This method must be overridden if the subclass uses more than one expression.
	 * i.e. if only {@link #setExpr(Expression)} is used, this method does not need to be overridden.
	 * But if a second expression is stored, this method must be overridden to account for that.
	 * <br>
	 * <br>
	 * {@inheritDoc}
	 */
	@Override
	public Expression<? extends T> simplify() {
		setExpr(expr.simplify());
		if (getExpr() instanceof Literal<?>)
			return SimplifiedLiteral.fromExpression(this);
		return this;
	}

	@Override
	@Nullable
	public Object[] beforeChange(Expression<?> changed, @Nullable Object[] delta) {
		return expr.beforeChange(changed, delta); // Forward to what we're wrapping
	}

	@Override
	public Class<? extends T>[] possibleReturnTypes() {
		return expr.possibleReturnTypes();
	}

	@Override
	public boolean canReturn(Class<?> returnType) {
		return expr.canReturn(returnType);
	}

}
