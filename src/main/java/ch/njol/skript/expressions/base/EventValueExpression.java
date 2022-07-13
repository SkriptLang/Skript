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

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.Expression;
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
	
	private final Class<? extends T> c;
	@Nullable
	private Changer<? super T> changer;
	private final Map<Class<? extends Event>, Getter<? extends T, ?>> getters = new HashMap<>();
	
	public EventValueExpression(final Class<? extends T> c) {
		this(c, null);
	}
	
	public EventValueExpression(final Class<? extends T> c, final @Nullable Changer<? super T> changer) {
		assert c != null;
		this.c = c;
		this.changer = changer;
	}
	
	@Override
	@Nullable
	protected T[] get(final Event e) {
		final T o = getValue(e);
		if (o == null)
			return null;
		@SuppressWarnings("unchecked")
		final T[] one = (T[]) Array.newInstance(c, 1);
		one[0] = o;
		return one;
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	private <E extends Event> T getValue(final E e) {
		if (getters.containsKey(e.getClass())) {
			final Getter<? extends T, ? super E> g = (Getter<? extends T, ? super E>) getters.get(e.getClass());
			return g == null ? null : g.get(e);
		}
		
		for (final Entry<Class<? extends Event>, Getter<? extends T, ?>> p : getters.entrySet()) {
			if (p.getKey().isAssignableFrom(e.getClass())) {
				getters.put(e.getClass(), p.getValue());
				return p.getValue() == null ? null : ((Getter<? extends T, ? super E>) p.getValue()).get(e);
			}
		}
		
		getters.put(e.getClass(), null);
		
		return null;
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		if (exprs.length != 0)
			throw new SkriptAPIException(this.getClass().getName() + " has expressions in its pattern but does not override init(...)");
		return init();
	}
	
	@SuppressWarnings("null")
	@Override
	public boolean init() {
		final ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			boolean hasValue = false;
			final Class<? extends Event>[] es = getParser().getCurrentEvents();
			if (es == null) {
				assert false;
				return false;
			}
			for (final Class<? extends Event> e : es) {
				if (getters.containsKey(e)) {
					hasValue = getters.get(e) != null;
					continue;
				}
				final Getter<? extends T, ?> getter = EventValues.getEventValueGetter(e, c, getTime());
				if (getter != null) {
					getters.put(e, getter);
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
	public String toString(final @Nullable Event e, final boolean debug) {
		if (!debug || e == null)
			return "event-" + Classes.getSuperClassInfo(c).getName();
		return Classes.getDebugMessage(getValue(e));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		Changer<? super T> ch = changer;
		if (ch == null)
			changer = ch = (Changer<? super T>) Classes.getSuperClassInfo(c).getChanger();
		return ch == null ? null : ch.acceptChange(mode);
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		final Changer<? super T> ch = changer;
		if (ch == null)
			throw new UnsupportedOperationException();
		ChangerUtils.change(ch, getArray(e), delta, mode);
	}
	
	@Override
	public boolean setTime(final int time) {
		final Class<? extends Event>[] es = getParser().getCurrentEvents();
		if (es == null) {
			assert false;
			return false;
		}
		for (final Class<? extends Event> e : es) {
			assert e != null;
			if (EventValues.doesEventValueHaveTimeStates(e, c)) {
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
