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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks the annotated element as being subject to removal in the future.
 * <p>
 * The annotated element should also be annotated with {@link Deprecated}.
 * <p>
 * It is recommended to provide when the annotated element will be removed,
 * using the {@code version} element.
 */
@Retention(RetentionPolicy.SOURCE)
public @interface MarkedForRemoval {

	/**
	 * When the annotated element is expected to be removed.
	 * <p>
	 * For example, this could be {@code after "2.6.4"},
	 * {@code "starting from 2.7"} or simply {@code "2.7"}.
	 */
	String version() default "";

}
