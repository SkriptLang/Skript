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
package ch.njol.skript.util;

import ch.njol.skript.lang.Expression;
import org.jetbrains.annotations.Nullable;

/**
 * The 'contract' of a function or another callable.
 * This is a non-exhaustive helper for type hints, singularity, etc. that may change based on the arguments
 * passed to a callable, in order for it to make better judgements on correct use at parse time.
 */
public interface Contract {

	/**
	 * @return Whether, given these parameters, this will return a single value
	 * @see Expression#isSingle()
	 */
	boolean isSingle(Expression<?>... arguments);

	/**
	 * @return What this will return, given these parameters
	 * @see Expression#getReturnType()
	 */
	@Nullable
	Class<?> getReturnType(Expression<?>... arguments);

}
