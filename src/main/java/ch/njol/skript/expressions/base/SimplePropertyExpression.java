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

import ch.njol.skript.classes.Converter;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * A base class for property expressions that requires only few overridden methods

 * @see PropertyExpression
 * @see PropertyExpression#register(Class, Class, String, String)
 */
@SuppressWarnings("deprecation") // for backwards compatibility
public abstract class SimplePropertyExpression<F, T> extends PropertyExpression<F, T> implements Converter<F, T> {

	private Event event;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<? extends F>) exprs[0]);
		return true;
	}

	protected abstract String getPropertyName();

	@Override
	@Nullable
	public abstract T convert(F f);

	@Override
	protected T[] get(Event event, F[] source) {
		this.event = event;
		return super.get(source, this);
	}

	/**
	 * Returns the Event that was used in this expression.
	 * Will only be null if called in the init method.
	 * 
	 * @return The Event that was used in this expression.
	 */
	@Nullable
	protected Event getEvent() {
		return event;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return getPropertyName() + " of " + getExpr().toString(event, debug);
	}

}
