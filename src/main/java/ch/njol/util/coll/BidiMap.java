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
package ch.njol.util.coll;

import java.util.Map;
import java.util.Set;

/**
 * @deprecated Unused in Skript. Use {@link java.util.LinkedHashMap} instead.
 */
@Deprecated(forRemoval = true)
public interface BidiMap<T1, T2> extends Map<T1, T2> {
	
	public BidiMap<T2, T1> getReverseView();
	
	public T1 getKey(final T2 value);
	
	public T2 getValue(final T1 key);
	
	public Set<T2> valueSet();
	
}
