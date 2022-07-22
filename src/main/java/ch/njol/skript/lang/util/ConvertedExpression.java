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
package ch.njol.skript.lang.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import org.skriptlang.skript.lang.converter.Converters;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

/**
 * Represents a expression converted to another type. This, and not Expression, is the required return type of {@link SimpleExpression#getConvertedExpr(Class...)} because this
 * class
 * <ol>
 * <li>automatically lets the source expression handle everything apart from the get() methods</li>
 * <li>will never convert itself to another type, but rather request a new converted expression from the source expression.</li>
 * </ol>
 * 
 * @author Peter Güttinger
 */
public class ConvertedExpression<F, T> implements Expression<T> {
	
	protected Expression<? extends F> source;
	protected Class<T> to;
	final Converter<? super F, ? extends T> conv;
	
	/**
	 * Converter information.
	 */
	private final ConverterInfo<? super F, ? extends T> converterInfo;
	
	public ConvertedExpression(Expression<? extends F> source, Class<T> to, ConverterInfo<? super F, ? extends T> info) {
		assert source != null;
		assert to != null;
		assert info != null;
		
		this.source = source;
		this.to = to;
		this.conv = info.getConverter();
		this.converterInfo = info;
	}
	
	@SafeVarargs
	@Nullable
	public static <F, T> ConvertedExpression<F, T> newInstance(final Expression<F> v, final Class<T>... to) {
		assert !CollectionUtils.containsSuperclass(to, v.getReturnType());
		for (final Class<T> c : to) { // REMIND try more converters? -> also change WrapperExpression (and maybe ExprLoopValue)
			assert c != null;
			// casting <? super ? extends F> to <? super F> is wrong, but since the converter is only used for values returned by the expression
			// (which are instances of "<? extends F>") this won't result in any ClassCastExceptions.
			@SuppressWarnings("unchecked")
			final ConverterInfo<? super F, ? extends T> conv = (ConverterInfo<? super F, ? extends T>) Converters.getConverterInfo(v.getReturnType(), c);
			if (conv == null)
				continue;
			return new ConvertedExpression<>(v, c, conv);
		}
		return null;
	}
	
	@Override
	public final boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult matcher) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		if (debug && e == null)
			return "(" + source.toString(e, debug) + " >> " + conv + ": "
				+ converterInfo + ")";
		return source.toString(e, debug);
	}
	
	@Override
	public String toString() {
		return toString(null, false);
	}
	
	@Override
	public Class<T> getReturnType() {
		return to;
	}
	
	@Override
	public boolean isSingle() {
		return source.isSingle();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public <R> Expression<? extends R> getConvertedExpression(final Class<R>... to) {
		if (CollectionUtils.containsSuperclass(to, this.to))
			return (Expression<? extends R>) this;
		return source.getConvertedExpression(to);
	}
	
	@Nullable
	private ClassInfo<? super T> returnTypeInfo;
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		final Class<?>[] r = source.acceptChange(mode);
		if (r == null) {
			ClassInfo<? super T> rti = returnTypeInfo;
			returnTypeInfo = rti = Classes.getSuperClassInfo(getReturnType());
			final Changer<?> c = rti.getChanger();
			return c == null ? null : c.acceptChange(mode);
		}
		return r;
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		final ClassInfo<? super T> rti = returnTypeInfo;
		if (rti != null) {
			final Changer<? super T> c = rti.getChanger();
			if (c != null)
				c.change(getArray(e), delta, mode);
		} else {
			source.change(e, delta, mode);
		}
	}
	
	@Override
	@Nullable
	public T getSingle(final Event e) {
		final F f = source.getSingle(e);
		if (f == null)
			return null;
		return conv.convert(f);
	}
	
	@Override
	public T[] getArray(final Event e) {
		return Converters.convert(source.getArray(e), to, conv);
	}
	
	@Override
	public T[] getAll(final Event e) {
		return Converters.convert(source.getAll(e), to, conv);
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super T> c, final boolean negated) {
		return negated ^ check(e, c);
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super T> c) {
		return source.check(e, new Checker<F>() {
			@Override
			public boolean check(final F f) {
				final T t = conv.convert(f);
				if (t == null) {
					return false;
				}
				return c.check(t);
			}
		});
	}
	
	@Override
	public boolean getAnd() {
		return source.getAnd();
	}
	
	@Override
	public boolean setTime(final int time) {
		return source.setTime(time);
	}
	
	@Override
	public int getTime() {
		return source.getTime();
	}
	
	@Override
	public boolean isDefault() {
		return source.isDefault();
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return false;// A loop does not convert the expression to loop
	}
	
	@Override
	@Nullable
	public Iterator<T> iterator(final Event e) {
		final Iterator<? extends F> iter = source.iterator(e);
		if (iter == null)
			return null;
		return new Iterator<T>() {
			@Nullable
			T next = null;
			
			@Override
			public boolean hasNext() {
				if (next != null)
					return true;
				while (next == null && iter.hasNext()) {
					final F f = iter.next();
					next = f == null ? null : conv.convert(f);
				}
				return next != null;
			}
			
			@Override
			public T next() {
				if (!hasNext())
					throw new NoSuchElementException();
				final T n = next;
				next = null;
				assert n != null;
				return n;
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	@Override
	public Expression<?> getSource() {
		return source;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Expression<? extends T> simplify() {
		final Expression<? extends T> c = source.simplify().getConvertedExpression(to);
		if (c != null)
			return c;
		return this;
	}
	
	@Override
	@Nullable
	public Object[] beforeChange(Expression<?> changed, @Nullable Object[] delta) {
		return source.beforeChange(changed, delta); // Forward to source
		// TODO this is not entirely safe, even though probably works well enough
	}
	
}
