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

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import org.skriptlang.skript.lang.converter.Converters;

import java.util.Iterator;
import java.util.NoSuchElementException;

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
	final Converter<? super F, ? extends T> converter;

	/**
	 * Converter information.
	 */
	private final ConverterInfo<? super F, ? extends T> converterInfo;

	public ConvertedExpression(Expression<? extends F> source, Class<T> to, ConverterInfo<? super F, ? extends T> info) {
		this.source = source;
		this.to = to;
		this.converter = info.getConverter();
		this.converterInfo = info;
	}

	@SafeVarargs
	@Nullable
	public static <F, T> ConvertedExpression<F, T> newInstance(Expression<F> from, Class<T>... to) {
		assert !CollectionUtils.containsSuperclass(to, from.getReturnType());
		for (Class<T> type : to) { // REMIND try more converters? -> also change WrapperExpression (and maybe ExprLoopValue)
			assert type != null;
			// casting <? super ? extends F> to <? super F> is wrong, but since the converter is only used for values returned by the expression
			// (which are instances of "<? extends F>") this won't result in any ClassCastExceptions.
			@SuppressWarnings("unchecked")
			ConverterInfo<? super F, ? extends T> conv = (ConverterInfo<? super F, ? extends T>) Converters.getConverterInfo(from.getReturnType(), type);
			if (conv == null)
				continue;
			return new ConvertedExpression<>(from, type, conv);
		}
		return null;
	}

	@Override
	public final boolean init(Expression<?>[] vars, int matchedPattern, Kleenean isDelayed, ParseResult matcher) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (debug && event == null)
			return "(" + source.toString(event, debug) + " >> " + converter + ": "
				+ converterInfo + ")";
		return source.toString(event, debug);
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

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		if (CollectionUtils.containsSuperclass(to, this.to))
			return (Expression<? extends R>) this;
		return source.getConvertedExpression(to);
	}

	@Nullable
	private ClassInfo<? super T> returnTypeInfo;

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		Class<?>[] validClasses = source.acceptChange(mode);
		if (validClasses == null) {
			ClassInfo<? super T> returnTypeInfo;
			this.returnTypeInfo = returnTypeInfo = Classes.getSuperClassInfo(getReturnType());
			Changer<?> changer = returnTypeInfo.getChanger();
			return changer == null ? null : changer.acceptChange(mode);
		}
		return validClasses;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		ClassInfo<? super T> returnTypeInfo = this.returnTypeInfo;
		if (returnTypeInfo != null) {
			Changer<? super T> changer = returnTypeInfo.getChanger();
			if (changer != null)
				changer.change(getArray(event), delta, mode);
		} else {
			source.change(event, delta, mode);
		}
	}

	@Override
	@Nullable
	public T getSingle(Event event) {
		F value = source.getSingle(event);
		if (value == null)
			return null;
		return converter.convert(value);
	}

	@Override
	public T[] getArray(Event event) {
		return Converters.convert(source.getArray(event), to, converter);
	}

	@Override
	public T[] getAll(Event event) {
		return Converters.convert(source.getAll(event), to, converter);
	}

	@Override
	public boolean check(Event event, Checker<? super T> checker, boolean negated) {
		return negated ^ check(event, checker);
	}

	@Override
	public boolean check(Event event, Checker<? super T> checker) {
		return source.check(event, (Checker<F>) value -> {
			T convertedValue = converter.convert(value);
			if (convertedValue == null) {
				return false;
			}
			return checker.check(convertedValue);
		});
	}

	@Override
	public boolean getAnd() {
		return source.getAnd();
	}

	@Override
	public boolean setTime(int time) {
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
	public boolean isLoopOf(String input) {
		return false;// A loop does not convert the expression to loop
	}

	@Override
	@Nullable
	public Iterator<T> iterator(Event event) {
		Iterator<? extends F> iterator = source.iterator(event);
		if (iterator == null)
			return null;
		return new Iterator<T>() {
			@Nullable
			T next = null;

			@Override
			public boolean hasNext() {
				if (next != null)
					return true;
				while (next == null && iterator.hasNext()) {
					F value = iterator.next();
					next = value == null ? null : converter.convert(value);
				}
				return next != null;
			}

			@Override
			public T next() {
				if (!hasNext())
					throw new NoSuchElementException();
				T n = next;
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

	@Override
	@SuppressWarnings("unchecked")
	public Expression<? extends T> simplify() {
		Expression<? extends T> convertedExpression = source.simplify().getConvertedExpression(to);
		if (convertedExpression != null)
			return convertedExpression;
		return this;
	}

	@Override
	@Nullable
	public Object[] beforeChange(Expression<?> changed, @Nullable Object[] delta) {
		return source.beforeChange(changed, delta); // Forward to source
		// TODO this is not entirely safe, even though probably works well enough
	}

}
