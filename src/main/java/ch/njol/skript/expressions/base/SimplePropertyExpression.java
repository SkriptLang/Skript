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
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;

/**
 * A base class for property expressions that requires only few overridden methods
 * 
 * @see PropertyExpression
 * @see PropertyExpression#register(Class, Class, String, String)
 */
@SuppressWarnings("deprecation") // for backwards compatibility
public abstract class SimplePropertyExpression<F, T> extends PropertyExpression<F, T> implements Converter<F, T> {

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (LiteralUtils.hasUnparsedLiteral(expressions[0])) {
			setExpr(LiteralUtils.defendExpression(expressions[0]));
			return LiteralUtils.canInitSafely(getExpr());
		}
		setExpr((Expression<? extends F>) expressions[0]);
		return true;
	}

	@Override
	@Nullable
	public abstract T convert(F from);

	@Override
	protected T[] get(Event event, F[] source) {
		return super.get(source, this);
	}

	/**
	 * Used to collect the property type used in the register method.
	 * This forms the toString of this SimplePropertyExpression.
	 * 
	 * @return The name of the type used when registering this SimplePropertyExpression.
	 */
	protected abstract String getPropertyName();

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return getPropertyName() + " of " + getExpr().toString(event, debug);
	}

}
