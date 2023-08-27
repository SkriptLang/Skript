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

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;

/**
 * A useful class for creating default expressions. It simply returns the event value of the given type.
 * <p>
 * This class can be used as default expression with <code>new EventValueExpression&lt;T&gt;(T.class)</code> or extended to make it manually placeable in expressions with:
 * 
 * <pre>
 * class MyExpression extends EventValueExpression&lt;SomeClass&gt; {
 * 	public MyExpression() {
 * 		super(SomeClass.class);
 * 	}
 * 	// ...
 * }
 * </pre>
 * 
 * @author Peter Güttinger
 * @see Classes#registerClass(ClassInfo)
 * @see ClassInfo#defaultExpression(DefaultExpression)
 * @see DefaultExpression
 */
public class EventValueExpression<T> extends SimpleExpression<T> implements DefaultExpression<T> {

	/**
	 * Registers an expression as {@link ExpressionType#EVENT} with the provided pattern.
	 * 
	 * @param expression The class that represents this EventValueExpression.
	 * @param type The return type of the expression.
	 * @param pattern The pattern for this syntax.
	 */
	public static <T> void register(Class<? extends EventValueExpression<T>> expression, Class<T> type, String pattern) {
		Skript.registerExpression(expression, type, ExpressionType.EVENT, "[the] " + pattern);
	}

	private final Map<Class<? extends Event>, Getter<? extends T, ?>> getters = new HashMap<>();

	@Nullable
	private Changer<? super T> changer;
	private final Class<? extends T> c;

	public EventValueExpression(Class<? extends T> c) {
		this(c, null);
	}

	public EventValueExpression(Class<? extends T> c, @Nullable Changer<? super T> changer) {
		assert c != null;
		this.c = c;
		this.changer = changer;
	}

	@Override
	@Nullable
	protected T[] get(Event event) {
		T object = getValue(event);
		if (object == null)
			return null;
		@SuppressWarnings("unchecked")
		T[] one = (T[]) Array.newInstance(c, 1);
		one[0] = object;
		return one;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private <E extends Event> T getValue(E event) {
		if (getters.containsKey(event.getClass())) {
			Getter<? extends T, ? super E> getter = (Getter<? extends T, ? super E>) getters.get(event.getClass());
			return getter == null ? null : getter.get(event);
		}

		for (Entry<Class<? extends Event>, Getter<? extends T, ?>> entry : getters.entrySet()) {
			if (entry.getKey().isAssignableFrom(event.getClass())) {
				getters.put(event.getClass(), entry.getValue());
				return entry.getValue() == null ? null : ((Getter<? extends T, ? super E>) entry.getValue()).get(event);
			}
		}

		getters.put(event.getClass(), null);
		return null;
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		if (exprs.length != 0)
			throw new SkriptAPIException(this.getClass().getName() + " has expressions in its pattern but does not override init(...)");
		return init();
	}

	@Override
	public boolean init() {
		ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			boolean hasValue = false;
			Class<? extends Event>[] events = getParser().getCurrentEvents();
			if (events == null) {
				assert false;
				return false;
			}
			for (Class<? extends Event> event : events) {
				if (getters.containsKey(event)) {
					hasValue = getters.get(event) != null;
					continue;
				}
				Getter<? extends T, ?> getter = EventValues.getEventValueGetter(event, c, getTime());
				if (getter != null) {
					getters.put(event, getter);
					hasValue = true;
				}
			}
			if (!hasValue) {
				log.printError("There's no " + Classes.getSuperClassInfo(c).getName() + " in " + Utils.a(getParser().getCurrentEventName()) + " event");
				return false;
			}
			log.printLog();
			return true;
		} finally {
			log.stop();
		}
	}

	@Override
	public Class<? extends T> getReturnType() {
		return c;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (!debug || event == null)
			return "event-" + Classes.getSuperClassInfo(c).getName();
		return Classes.getDebugMessage(getValue(event));
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	public Class<?>[] acceptChange(ChangeMode mode) {
		Changer<? super T> changer = this.changer;
		if (changer == null)
			this.changer = changer = (Changer<? super T>) Classes.getSuperClassInfo(c).getChanger();
		return changer == null ? null : changer.acceptChange(mode);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		Changer<? super T> changer = this.changer;
		if (changer == null)
			throw new UnsupportedOperationException();
		ChangerUtils.change(changer, getArray(event), delta, mode);
	}

	@Override
	public boolean setTime(int time) {
		Class<? extends Event>[] events = getParser().getCurrentEvents();
		if (events == null) {
			assert false;
			return false;
		}
		for (Class<? extends Event> event : events) {
			assert event != null;
			if (EventValues.doesEventValueHaveTimeStates(event, c)) {
				super.setTime(time);
				// Since the time was changed, we now need to re-initalize the getters we already got. START
				getters.clear();
				init();
				// END
				return true;
			}
		}
		return false;
	}

	/**
	 * @return true
	 */
	@Override
	public boolean isDefault() {
		return true;
	}

}
