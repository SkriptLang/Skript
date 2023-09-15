/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.lang;

/**
 * Represents an expression that can be used as the default value of a certain type and event.
 *
 * @author Peter Güttinger
 */
public interface DefaultExpression<T> extends Expression<T> {

	public boolean init();

	/**
	 * @return Usually true, though this is not required, as e.g. SimpleLiteral implements DefaultExpression but is usually not the default of an event.
	 */
	@Override
	public boolean isDefault();

}
