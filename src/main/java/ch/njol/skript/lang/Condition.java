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
package ch.njol.skript.lang;

import java.util.Iterator;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.conditions.CondExpression;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Checker;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.SingleItemIterator;

/**
 * A condition which must be fulfilled for the trigger to continue. If the condition is in a section the behaviour depends on the section.
 *
 * @see Skript#registerCondition(Class, String...)
 */
public abstract class Condition extends Statement implements Expression<Boolean> {
	
	private boolean negated = false;
	
	protected Condition() {}
	
	/**
	 * Checks whether this condition is satisfied with the given event. This should not alter the event or the world in any way, as conditions are only checked until one returns
	 * false. All subsequent conditions of the same trigger will then be omitted.<br/>
	 * <br/>
	 * You might want to use {@link SimpleExpression#check(Event, Checker)}
	 * 
	 * @param e the event to check
	 * @return <code>true</code> if the condition is satisfied, <code>false</code> otherwise or if the condition doesn't apply to this event.
	 */
	public abstract boolean check(Event e);
	
	@Override
	public final boolean run(Event e) {
		return check(e);
	}
	
	/**
	 * Sets the negation state of this condition. This will change the behaviour of {@link Expression#check(Event, Checker, boolean)}.
	 */
	protected final void setNegated(boolean invert) {
		negated = invert;
	}
	
	/**
	 * @return whether this condition is negated or not.
	 */
	public final boolean isNegated() {
		return negated;
	}
	
	@SuppressWarnings({"ConstantConditions", "unchecked"})
	@Nullable
	public static Condition parse(String s, @Nullable String defaultError) {
		s = s.trim();
		while (s.startsWith("(") && SkriptParser.next(s, 0, ParseContext.DEFAULT) == s.length())
			s = s.substring(1, s.length() - 1);
		
		Expression<? extends Boolean> expression;
		ParseLogHandler logHandler = SkriptLogger.startParseLogHandler();
		try {
			expression = new SkriptParser(s).parseExpression(Boolean.class);
			if (expression == null) {
				logHandler.printError(defaultError);
				return null;
			}
			
			logHandler.printLog();
		} finally {
			logHandler.stop();
		}
		
		return expression instanceof Condition ? (Condition) expression : new CondExpression(expression);
	}
	
	@Override
	public Boolean getSingle(Event e) {
		return check(e);
	}
	
	@Override
	public Boolean[] getArray(Event e) {
		return new Boolean[] {check(e)};
	}
	
	@Override
	public Boolean[] getAll(Event e) {
		return new Boolean[] {check(e)};
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public boolean check(Event e, Checker<? super Boolean> c, boolean negated) {
		return SimpleExpression.check(getAll(e), c, negated, getAnd());
	}
	
	@Override
	public boolean check(Event e, Checker<? super Boolean> c) {
		return SimpleExpression.check(getAll(e), c, false, getAnd());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		if (CollectionUtils.containsSuperclass(to, Boolean.class))
			return (Expression<? extends R>) this;
		return ConvertedExpression.newInstance(this, to);
	}
	
	@Override
	public Class<? extends Boolean> getReturnType() {
		return Boolean.class;
	}
	
	@Override
	public boolean getAnd() {
		return true;
	}
	
	@Override
	public boolean setTime(int time) {
		return false;
	}
	
	@Override
	public int getTime() {
		return 0;
	}
	
	@Override
	public boolean isDefault() {
		return false;
	}
	
	@Nullable
	@Override
	public Iterator<? extends Boolean> iterator(Event e) {
		return new SingleItemIterator<>(check(e));
	}
	
	@Override
	public boolean isLoopOf(String s) {
		return false;
	}
	
	@Override
	public Expression<?> getSource() {
		return this;
	}
	
	@Override
	public Expression<? extends Boolean> simplify() {
		return this;
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		return null;
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
		throw new UnsupportedOperationException();
	}
	
}
