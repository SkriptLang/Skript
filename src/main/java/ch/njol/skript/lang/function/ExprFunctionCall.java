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
import org.skriptlang.skript.lang.converter.Converters;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

public class ExprFunctionCall<T> extends SimpleExpression<T> {

	private final FunctionReference<?> function;
	private final Class<? extends T>[] returnTypes;
	private final Class<T> returnType;

	public ExprFunctionCall(FunctionReference<T> function) {
		this(function, function.returnTypes);
	}

	@SuppressWarnings("unchecked")
	public ExprFunctionCall(FunctionReference<?> function, Class<? extends T>[] expectedReturnTypes) {
		this.function = function;
		Class<?> functionReturnType = function.getReturnType();
		assert  functionReturnType != null;
		if (CollectionUtils.containsSuperclass(expectedReturnTypes, functionReturnType)) {
			// Function returns expected type already
			this.returnTypes = new Class[] {functionReturnType};
			this.returnType = (Class<T>) functionReturnType;
		} else {
			// Return value needs to be converted
			this.returnTypes = expectedReturnTypes;
			this.returnType = (Class<T>) Utils.getSuperType(expectedReturnTypes);
		}
	}

	@Override
	@Nullable
	protected T[] get(Event e) {
		Object[] returnValue = function.execute(e);
		function.resetReturnValue();
		return Converters.convert(returnValue, returnTypes, returnType);
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		if (CollectionUtils.containsSuperclass(to, getReturnType()))
			return (Expression<? extends R>) this;
		assert function.getReturnType() != null;
		if (Converters.converterExists(function.getReturnType(), to)) {
			return new ExprFunctionCall<>(function, to);
		}
		return null;
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
