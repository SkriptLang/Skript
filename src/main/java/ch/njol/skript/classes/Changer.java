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
package ch.njol.skript.classes;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.data.DefaultChangers;
import ch.njol.skript.lang.Expression;

/**
 * An interface to declare changeable values. All Expressions implement something similar like this by default, but refuse any change if {@link Expression#acceptChange(ChangeMode)}
 * isn't overridden.
 * <p>
 * Some useful Changers can be found in {@link DefaultChangers}
 *
 * @see DefaultChangers
 * @see Expression
 */
public interface Changer<T> {
	
	enum ChangeMode {
		ADD, SET, REMOVE, REMOVE_ALL, DELETE, RESET;
	}
	
	/**
	 * Tests whether this changer supports the given mode, and if yes what type(s) it expects the elements of <code>delta</code> to be.
	 * <p>
	 * Unlike {@link Expression#acceptChange(ChangeMode)} this method must not print errors.
	 * 
	 * @param mode The {@link ChangeMode} to test.
	 * @return An array of types that {@link #change(Object[], Object[], ChangeMode)} accepts as its <code>delta</code> parameter (which can be arrays to denote that multiple of
	 *         that type are accepted), or null if the given mode is not supported. For {@link ChangeMode#DELETE} and {@link ChangeMode#RESET} this can return any non-null array to
	 *         mark them as supported.
	 */
	Class<?> @Nullable [] acceptChange(ChangeMode mode);
	
	/**
	 * @param what The objects to change
	 * @param delta An array with one or more instances of one or more of the the classes returned by {@link #acceptChange(ChangeMode)} for the given change mode (null for
	 *            {@link ChangeMode#DELETE} and {@link ChangeMode#RESET}). <b>This can be a Object[], thus casting is not allowed.</b>
	 * @param mode The {@link ChangeMode} to test.
	 * @throws UnsupportedOperationException (optional) if this method was called on an unsupported ChangeMode.
	 */
	void change(T[] what, Object @Nullable [] delta, ChangeMode mode);
	
	abstract class ChangerUtils {

		public static <T> void change(Changer<T> changer, Object[] what, Object @Nullable [] delta, ChangeMode mode) {
			//noinspection unchecked
			changer.change((T[]) what, delta, mode);
		}
		
		/**
		 * Tests whether an expression accepts changes of a certain type. If multiple types are given it test for whether any of the types is accepted.
		 * 
		 * @param expression The expression to test
		 * @param mode The ChangeMode to use in the test
		 * @param types The types to test for
		 * @return Whether <tt>expression.{@link Expression#change(Event, Object[], ChangeMode) change}(event, type[], mode)</tt> can be used or not.
		 */
		public static boolean acceptsChange(final Expression<?> expression, final ChangeMode mode, final Class<?>... types) {
			final Class<?>[] validTypes = expression.acceptChange(mode);
			if (validTypes == null)
				return false;
			for (final Class<?> type : types) {
				for (final Class<?> validType : validTypes) {
					if (validType.isArray() ? validType.getComponentType().isAssignableFrom(type) : validType.isAssignableFrom(type))
						return true;
				}
			}
			return false;
		}
		
	}
	
}
