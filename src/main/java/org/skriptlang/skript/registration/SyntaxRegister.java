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
package org.skriptlang.skript.registration;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;

/**
 * A syntax register is a collection of registered {@link SyntaxInfo}s of a common type.
 * @param <I> The type of syntax in this register.
 */
@ApiStatus.Experimental
interface SyntaxRegister<I extends SyntaxInfo<?>> {

	/**
	 * @return An unmodifiable snapshot of all syntaxes this register contains.
	 */
	@Unmodifiable
	Collection<I> syntaxes();

	/**
	 * Adds a new syntax info to this register.
	 * @param info The syntax info to add.
	 */
	void add(I info);

}
