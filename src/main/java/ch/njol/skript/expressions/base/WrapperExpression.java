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

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import org.skriptlang.skript.lang.converter.Converters;

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
	@Nullable
	@SuppressWarnings("unchecked")
	protected <R> ConvertedExpression<T, ? extends R> getConvertedExpr(Class<R>... to) {
		for (Class<R> type : to) {
			assert type != null;
			ConverterInfo<? super T, ? extends R> conv = (ConverterInfo<? super T, ? extends R>) Converters.getConverterInfo(getReturnType(), type);
			if (conv == null)
				continue;
			return new ConvertedExpression<T, R>(expr, type, conv) {
				@Override
				public String toString(@Nullable Event event, boolean debug) {
					if (debug && event == null)
						return "(" + WrapperExpression.this.toString(event, debug) + ")->" + to.getName();
					return WrapperExpression.this.toString(event, debug);
				}
			};
		}
		return null;
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
	public boolean isDefault() {
		return expr.isDefault();
	}
	
	@Override
	public Expression<? extends T> simplify() {
		return expr;
	}
	
	@Override
	@Nullable
	public Object[] beforeChange(Expression<?> changed, @Nullable Object[] delta) {
		return expr.beforeChange(changed, delta); // Forward to what we're wrapping
	}
	
}
