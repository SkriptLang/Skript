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

import java.lang.reflect.Array;
import java.util.Iterator;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.ArrayIterator;

/**
 * An implementation of the {@link Expression} interface. You should usually extend this class to make a new expression.
 * 
 * @see Skript#registerExpression(Class, Class, ExpressionType, String...)
 * @author Peter Güttinger
 */
public abstract class SimpleExpression<T> implements Expression<T> {
	
	private int time = 0;
	
	protected SimpleExpression() {}
	
	@Override
	@Nullable
	public final T getSingle(final Event e) {
		final T[] all = getArray(e);
		if (all.length == 0)
			return null;
		if (all.length > 1)
			throw new SkriptAPIException("Call to getSingle() on a non-single expression");
		return all[0];
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Unlike {@link #get(Event)} you have to make sure that the this method's returned array is neither null nor contains null elements.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T[] getAll(final Event e) {
		final T[] all = get(e);
		if (all == null) {
			final T[] r = (T[]) Array.newInstance(getReturnType(), 0);
			assert r != null;
			return r;
		}
		if (all.length == 0)
			return all;
		int numNonNull = 0;
		for (final T t : all)
			if (t != null)
				numNonNull++;
		if (numNonNull == all.length)
			return all;
		final T[] r = (T[]) Array.newInstance(getReturnType(), numNonNull);
		assert r != null;
		int i = 0;
		for (final T t : all)
			if (t != null)
				r[i++] = t;
		return r;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final T[] getArray(final Event e) {
		final T[] all = get(e);
		if (all == null) {
			final T[] r = (T[]) Array.newInstance(getReturnType(), 0);
			assert r != null;
			return r;
		}
		if (all.length == 0)
			return all;
		
		int numNonNull = 0;
		for (final T t : all)
			if (t != null)
				numNonNull++;
		
		if (!getAnd()) {
			if (all.length == 1 && all[0] != null)
				return all;
			int rand = Utils.random(0, numNonNull);
			final T[] one = (T[]) Array.newInstance(getReturnType(), 1);
			for (final T t : all) {
				if (t != null) {
					if (rand == 0) {
						one[0] = t;
						return one;
					}
					rand--;
				}
			}
			assert false;
		}
		
		if (numNonNull == all.length)
			return all;
		final T[] r = (T[]) Array.newInstance(getReturnType(), numNonNull);
		assert r != null;
		int i = 0;
		for (final T t : all)
			if (t != null)
				r[i++] = t;
		return r;
	}
	
	/**
	 * This is the internal method to get an expression's values.<br>
	 * To get the expression's value from the outside use {@link #getSingle(Event)} or {@link #getArray(Event)}.
	 * 
	 * @param e The event
	 * @return An array of values for this event. May not contain nulls.
	 */
	@Nullable
	protected abstract T[] get(Event e);
	
	@Override
	public final boolean check(final Event e, final Checker<? super T> c) {
		return check(e, c, false);
	}
	
	@Override
	public final boolean check(final Event e, final Checker<? super T> c, final boolean negated) {
		return check(get(e), c, negated, getAnd());
	}
	
	// TODO return a kleenean (UNKNOWN if 'all' is null or empty)
	public static <T> boolean check(final @Nullable T[] all, final Checker<? super T> c, final boolean invert, final boolean and) {
		if (all == null)
			return invert;
		boolean hasElement = false;
		for (final T t : all) {
			if (t == null)
				continue;
			hasElement = true;
			final boolean b = c.check(t);
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
	protected <R> ConvertedExpression<T, ? extends R> getConvertedExpr(final Class<R>... to) {
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
	@SuppressWarnings("unchecked")
	@Nullable
	public <R> Expression<? extends R> getConvertedExpression(final Class<R>... to) {
		if (CollectionUtils.containsSuperclass(to, getReturnType()))
			return (Expression<? extends R>) this;
		return this.getConvertedExpr(to);
	}
	
	@Nullable
	private ClassInfo<?> returnTypeInfo;
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		ClassInfo<?> rti = returnTypeInfo;
		if (rti == null)
			returnTypeInfo = rti = Classes.getSuperClassInfo(getReturnType());
		final Changer<?> c = rti.getChanger();
		if (c == null)
			return null;
		return c.acceptChange(mode);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		final ClassInfo<?> rti = returnTypeInfo;
		if (rti == null)
			throw new UnsupportedOperationException();
		final Changer<?> c = rti.getChanger();
		if (c == null)
			throw new UnsupportedOperationException();
		((Changer<T>) c).change(getArray(e), delta, mode);
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
	public boolean setTime(final int time) {
		if (getParser().getHasDelayBefore() == Kleenean.TRUE && time != 0) {
			Skript.error("Can't use time states after the event has already passed");
			return false;
		}
		this.time = time;
		return false;
	}
	
	protected final boolean setTime(final int time, final Class<? extends Event> applicableEvent) {
		if (getParser().getHasDelayBefore() == Kleenean.TRUE && time != 0) {
			Skript.error("Can't use time states after the event has already passed");
			return false;
		}
		if (!getParser().isCurrentEvent(applicableEvent))
			return false;
		this.time = time;
		return true;
	}
	
	protected final boolean setTime(final int time, final Class<? extends Event> applicableEvent, final Expression<?>... mustbeDefaultVars) {
		if (getParser().getHasDelayBefore() == Kleenean.TRUE && time != 0) {
			Skript.error("Can't use time states after the event has already passed");
			return false;
		}
		if (!getParser().isCurrentEvent(applicableEvent))
			return false;
		for (final Expression<?> var : mustbeDefaultVars) {
			if (!var.isDefault()) {
				return false;
			}
		}
		this.time = time;
		return true;
	}
	
	protected final boolean setTime(final int time, final Expression<?> mustbeDefaultVar, final Class<? extends Event>... applicableEvents) {
		if (getParser().getHasDelayBefore() == Kleenean.TRUE && time != 0) {
			Skript.error("Can't use time states after the event has already passed");
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
	public boolean isLoopOf(final String s) {
		return false;
	}
	
	@Override
	@Nullable
	public Iterator<? extends T> iterator(final Event e) {
		return new ArrayIterator<>(getArray(e));
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
