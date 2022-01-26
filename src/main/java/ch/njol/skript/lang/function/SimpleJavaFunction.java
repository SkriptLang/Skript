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

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.ClassInfo;

/**
 * A {@link JavaFunction} which doesn't make use of
 * the {@link FunctionEvent} instance and that cannot
 * accept empty / {@code null} parameters.
 */
public abstract class SimpleJavaFunction<T> extends JavaFunction<T> {
	
	public SimpleJavaFunction(Signature<T> sign) {
		super(sign);
	}
	
	public SimpleJavaFunction(String name, Parameter<?>[] parameters, ClassInfo<T> returnType, boolean single) {
		super(name, parameters, returnType, single);
	}
	
	@SuppressWarnings("ConstantConditions")
	@Nullable
	@Override
	public final T[] execute(FunctionEvent<?> e, Object[][] params) {
		for (Object[] param : params) {
			if (param == null || param.length == 0 || param[0] == null)
				return null;
		}
		return executeSimple(params);
	}
	
	@Nullable
	public abstract T[] executeSimple(Object[][] params);
	
}
