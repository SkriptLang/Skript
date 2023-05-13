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
 * 
 * @author Peter Güttinger
 * @see PropertyExpression
 * @see PropertyExpression#register(Class, Class, String, String)
 */
@SuppressWarnings("deprecation") // for backwards compatibility
public abstract class SimplePropertyExpression<F, T> extends PropertyExpression<F, T> implements Converter<F, T> {
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		setExpr((Expression<? extends F>) exprs[0]);
		return true;
	}
	
	protected abstract String getPropertyName();
	
	@Override
	@Nullable
	public abstract T convert(F f);
	
	@Override
	protected T[] get(final Event e, final F[] source) {
		return super.get(source, this);
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the " + getPropertyName() + " of " + getExpr().toString(e, debug);
	}
}
