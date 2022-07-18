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
package ch.njol.skript.lang.function;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

public class ExprFunctionCall<T> extends SimpleExpression<T> {

	private final FunctionReference<?> function;
	private final Class<? extends T>[] returnTypes;
	private final Class<T> returnType;

	@SuppressWarnings("unchecked")
	public ExprFunctionCall(FunctionReference<T> function) {
		this.function = function;
		this.returnTypes = function.returnTypes;
		this.returnType = (Class<T>) Utils.getSuperType(returnTypes);
	}

	@Override
	@Nullable
	protected T[] get(Event e) {
		Object[] returnValue = function.execute(e);
		function.resetReturnValue();
		return Converters.convertArray(returnValue, returnTypes, returnType);
	}

	@Override
	public boolean isSingle() {
		return function.isSingle();
	}

	@Override
	public Class<? extends T> getReturnType() {
		return returnType;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return function.toString(e, debug);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		assert false;
		return false;
	}

}
