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
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.lang.converter.Converters;

public class ExprFunctionCall<T> extends SimpleExpression<T> {

	private final FunctionReference<?> reference;
	private final Class<? extends T>[] returnTypes;
	private final Class<T> returnType;

	public ExprFunctionCall(FunctionReference<T> reference) {
		this(reference, reference.returnTypes);
	}

	@SuppressWarnings("unchecked")
	public ExprFunctionCall(FunctionReference<?> reference, Class<? extends T>[] expectedReturnTypes) {
		this.reference = reference;
		Class<?> functionReturnType = reference.getReturnType();
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
		Object[] returnValue = reference.execute(e);
		reference.resetReturnValue();
		return Converters.convert(returnValue, returnTypes, returnType);
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		if (CollectionUtils.containsSuperclass(to, getReturnType()))
			return (Expression<? extends R>) this;
		assert reference.getReturnType() != null;
		if (Converters.converterExists(reference.getReturnType(), to)) {
			return new ExprFunctionCall<>(reference, to);
		}
		return null;
	}

	@Override
	public boolean isSingle() {
		return reference.isSingle();
	}

	@Override
	public Class<? extends T> getReturnType() {
		return returnType;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return reference.toString(e, debug);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		assert false;
		return false;
	}

	@Override
	public Expression<T> simplify() {
		Function<?> function = reference.getFunction();
		if (function == null)
			return this;

		if (!function.isPure())
			return this;

		if (!reference.parameters()
				.stream()
				.allMatch(expression -> expression instanceof Literal))
			return this;

		T[] result = getArray(null);
		if (result.length == 0)
			return this;
		return new SimpleLiteral<>(result, returnType, getAnd());
	}

}
