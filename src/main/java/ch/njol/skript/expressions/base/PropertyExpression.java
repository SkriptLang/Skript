/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions.base;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import org.skriptlang.skript.lang.converter.Converter;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.util.SimpleExpression;
import org.skriptlang.skript.lang.converter.Converters;
import ch.njol.util.Kleenean;

/**
 * Represents an expression which represents a property of another one. Remember to set the expression with {@link #setExpr(Expression)} in
 * {@link SyntaxElement#init(Expression[], int, Kleenean, ParseResult) init()}.
 * 
 * @author Peter Güttinger
 * @see SimplePropertyExpression
 * @see #register(Class, Class, String, String)
 */
public abstract class PropertyExpression<F, T> extends SimpleExpression<T> {
	
	/**
	 * Registers an expression as {@link ExpressionType#PROPERTY} with the two default property patterns "property of %types%" and "%types%'[s] property"
	 * 
	 * @param c
	 * @param type
	 * @param property The name of the property
	 * @param fromType Should be plural but doesn't have to be
	 */
	public static <T> void register(final Class<? extends Expression<T>> c, final Class<T> type, final String property, final String fromType) {
		Skript.registerExpression(c, type, ExpressionType.PROPERTY, "[the] " + property + " of %" + fromType + "%", "%" + fromType + "%'[s] " + property);
	}
	
	@SuppressWarnings("null")
	private Expression<? extends F> expr;
	
	/**
	 * Sets the expression this expression represents a property of. No reference to the expression should be kept.
	 * 
	 * @param expr
	 */
	protected final void setExpr(final Expression<? extends F> expr) {
		this.expr = expr;
	}
	
	public final Expression<? extends F> getExpr() {
		return expr;
	}
	
	@Override
	protected final T[] get(final Event e) {
		return get(e, expr.getArray(e));
	}
	
	@Override
	public final T[] getAll(final Event e) {
		return get(e, expr.getAll(e));
	}
	
	/**
	 * Converts the given source object(s) to the correct type.
	 * <p>
	 * Please note that the returned array must neither be null nor contain any null elements!
	 * 
	 * @param e
	 * @param source
	 * @return An array of the converted objects, which may contain less elements than the source array, but must not be null.
	 * @see Converters#convert(Object[], Class, Converter)
	 */
	protected abstract T[] get(Event e, F[] source);
	
	/**
	 * @param source
	 * @param converter must return instances of {@link #getReturnType()}
	 * @return An array containing the converted values
	 * @throws ArrayStoreException if the converter returned invalid values
	 */
	protected T[] get(final F[] source, final Converter<? super F, ? extends T> converter) {
		assert source != null;
		assert converter != null;
		return Converters.convertUnsafe(source, getReturnType(), converter);
	}

	@Override
	public final boolean isSingle() {
		return expr.isSingle();
	}
	
	@Override
	public final boolean getAnd() {
		return expr.getAnd();
	}
	
	@Override
	public Expression<? extends T> simplify() {
		expr = expr.simplify();
		return this;
	}
	
}
