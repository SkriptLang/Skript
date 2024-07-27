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
package ch.njol.skript.lang;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A literal, e.g. a number, string or item. Literals are constants which do not depend on the event and can thus e.g. be used in events.
 * 
 * @author Peter Güttinger
 */
public interface Literal<T> extends Expression<T> {

	T[] getArray();

	T getSingle();

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	<R> Literal<? extends R> getConvertedExpression(Class<R>... to);

	T[] getAll();

}
