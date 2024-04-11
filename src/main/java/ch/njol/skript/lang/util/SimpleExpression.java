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

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.ArrayIterator;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.lang.converter.Converter;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;

/**
 * An implementation of the {@link Expression} interface. You should usually extend this class to make a new expression.
 * 
 * @see Skript#registerExpression(Class, Class, ExpressionType, String...)
 */
public abstract class SimpleExpression<T> implements Expression<T> {

	private int time = 0;

	protected SimpleExpression() {}

	@Override
	@Nullable
	public final T getSingle(Event event) {
		T[] values = getArray(event);
		if (values.length == 0)
			return null;
		if (values.length > 1)
			throw new SkriptAPIException("Call to getSingle() on a non-single expression");
		return values[0];
	}

	@Override
	@SuppressWarnings("unchecked")
	public T[] getAll(Event event) {
		T[] values = get(event);
		if (values == null) {
			T[] emptyArray = (T[]) Array.newInstance(getReturnType(), 0);
			assert emptyArray != null;
			return emptyArray;
		}
		if (values.length == 0)
			return values;
		int numNonNull = 0;
		for (T value : values)
			if (value != null)
				numNonNull++;
		if (numNonNull == values.length)
			return Arrays.copyOf(values, values.length);
		T[] valueArray = (T[]) Array.newInstance(getReturnType(), numNonNull);
		assert valueArray != null;
		int i = 0;
		for (T value : values)
			if (value != null)
				valueArray[i++] = value;
		return valueArray;
	}

	@Override
	@SuppressWarnings("unchecked")
	public final T[] getArray(Event event) {
		T[] values = get(event);
		if (values == null) {
			return (T[]) Array.newInstance(getReturnType(), 0);
		}
		if (values.length == 0)
			return values;

		int numNonNull = 0;
		for (T value : values)
			if (value != null)
				numNonNull++;

		if (!getAnd()) {
			if (values.length == 1 && values[0] != null)
				return Arrays.copyOf(values, 1);
			int rand = Utils.random(0, numNonNull);
			T[] valueArray = (T[]) Array.newInstance(getReturnType(), 1);
			for (T value : values) {
				if (value != null) {
					if (rand == 0) {
						valueArray[0] = value;
						return valueArray;
					}
					rand--;
				}
			}
			assert false;
		}

		if (numNonNull == values.length)
			return Arrays.copyOf(values, values.length);
		T[] valueArray = (T[]) Array.newInstance(getReturnType(), numNonNull);
		int i = 0;
		for (T value : values)
			if (value != null)
				valueArray[i++] = value;
		return valueArray;
	}

	/**
	 * This is the internal method to get an expression's values.<br>
	 * To get the expression's value from the outside use {@link #getSingle(Event)} or {@link #getArray(Event)}.
	 * 
	 * @param event The event with which this expression is evaluated.
	 * @return An array of values for this event. May not contain nulls.
	 */
	@Nullable
	protected abstract T[] get(Event event);

	@Override
	public final boolean check(Event event, Checker<? super T> checker) {
		return check(event, checker, false);
	}

	@Override
	public final boolean check(Event event, Checker<? super T> checker, boolean negated) {
		return check(get(event), checker, negated, getAnd());
	}

	// TODO return a kleenean (UNKNOWN if 'values' is null or empty)
	public static <T> boolean check(@Nullable T[] values, Checker<? super T> checker, boolean invert, boolean and) {
		if (values == null)
			return invert;
		boolean hasElement = false;
		for (T value : values) {
			if (value == null)
				continue;
			hasElement = true;
			boolean b = checker.check(value);
			if (and && !b)
				return invert;
			if (!and && b)
				return !invert;
		}
		if (!hasElement)
			return invert;
		return invert ^ and;
	}

	/**
	 * Converts this expression to another type. Unless the expression is special, the default implementation is sufficient.
	 * <p>
	 * This method is never called with a supertype of the return type of this expression, or the return type itself.
	 * 
	 * @param to The desired return type of the returned expression
	 * @return Expression with the desired return type or null if it can't be converted to the given type
	 * @see Expression#getConvertedExpression(Class...)
	 * @see ConvertedExpression#newInstance(Expression, Class...)
	 * @see Converter
	 */
	@Nullable
	protected <R> ConvertedExpression<T, ? extends R> getConvertedExpr(Class<R>... to) {
		assert !CollectionUtils.containsSuperclass(to, getReturnType());
		return ConvertedExpression.newInstance(this, to);
	}

	/**
	 * Usually, you want to override {@link SimpleExpression#getConvertedExpr(Class[])}.
	 * However, it may be useful to override this method if you have an expression with a return
	 * type that is unknown until runtime (like variables). Usually, you'll be fine with just
	 * the default implementation. This method is final on versions below 2.2-dev36.
	 *
	 * @param to The desired return type of the returned expression
	 * @return The converted expression
	 */
	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		if (CollectionUtils.containsSuperclass(to, getReturnType()))
			return (Expression<? extends R>) this;
		return this.getConvertedExpr(to);
	}

	@Nullable
	private ClassInfo<?> returnTypeInfo;

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		ClassInfo<?> returnTypeInfo = this.returnTypeInfo;
		if (returnTypeInfo == null)
			this.returnTypeInfo = returnTypeInfo = Classes.getSuperClassInfo(getReturnType());
		Changer<?> changer = returnTypeInfo.getChanger();
		if (changer == null)
			return null;
		return changer.acceptChange(mode);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		ClassInfo<?> returnTypeInfo = this.returnTypeInfo;
		if (returnTypeInfo == null)
			throw new UnsupportedOperationException();
		Changer<?> changer = returnTypeInfo.getChanger();
		if (changer == null)
			throw new UnsupportedOperationException();
		((Changer<T>) changer).change(getArray(event), delta, mode);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation sets the time but returns false.
	 * 
	 * @see #setTime(int, Class, Expression...)
	 * @see #setTime(int, Expression, Class...)
	 */
	@Override
	public boolean setTime(int time) {
		if (getParser().getHasDelayBefore() == Kleenean.TRUE && time != 0) {
			Skript.error("Can't use time states after the event has already passed.");
			return false;
		}
		this.time = time;
		return false;
	}

	protected final boolean setTime(int time, Class<? extends Event> applicableEvent) {
		if (getParser().getHasDelayBefore() == Kleenean.TRUE && time != 0) {
			Skript.error("Can't use time states after the event has already passed.");
			return false;
		}
		if (!getParser().isCurrentEvent(applicableEvent))
			return false;
		this.time = time;
		return true;
	}

	@SafeVarargs
	protected final boolean setTime(int time, Class<? extends Event>... applicableEvents) {
		if (getParser().getHasDelayBefore() == Kleenean.TRUE && time != 0) {
			Skript.error("Can't use time states after the event has already passed.");
			return false;
		}
		if (!getParser().isCurrentEvent(applicableEvents))
			return false;
		this.time = time;
		return true;
	}

	protected final boolean setTime(int time, Class<? extends Event> applicableEvent, @NonNull Expression<?>... mustbeDefaultVars) {
		if (getParser().getHasDelayBefore() == Kleenean.TRUE && time != 0) {
			Skript.error("Can't use time states after the event has already passed.");
			return false;
		}
		if (!getParser().isCurrentEvent(applicableEvent))
			return false;
		for (Expression<?> var : mustbeDefaultVars) {
			if (!var.isDefault()) {
				return false;
			}
		}
		this.time = time;
		return true;
	}

	@SafeVarargs
	protected final boolean setTime(int time, Expression<?> mustbeDefaultVar, Class<? extends Event>... applicableEvents) {
		if (getParser().getHasDelayBefore() == Kleenean.TRUE && time != 0) {
			Skript.error("Can't use time states after the event has already passed.");
			return false;
		}
		if (mustbeDefaultVar == null) {
			Skript.exception(new SkriptAPIException("Default expression was null. If the default expression can be null, don't be using" +
					" 'SimpleExpression#setTime(int, Expression<?>, Class<? extends Event>...)' instead use the setTime without an expression if null."));
			return false;
		}
		if (!mustbeDefaultVar.isDefault())
			return false;
		if (getParser().isCurrentEvent(applicableEvents)) {
			this.time = time;
			return true;
		}
		return false;
	}

	@Override
	public int getTime() {
		return time;
	}

	@Override
	public boolean isDefault() {
		return false;
	}

	@Override
	public boolean isLoopOf(String input) {
		return false;
	}

	@Override
	@Nullable
	public Iterator<? extends T> iterator(Event event) {
		return new ArrayIterator<>(getArray(event));
	}

	@Override
	public String toString() {
		return toString(null, false);
	}

	@Override
	public Expression<?> getSource() {
		return this;
	}

	@Override
	public Expression<? extends T> simplify() {
		return this;
	}

	@Override
	public boolean getAnd() {
		return true;
	}
}
