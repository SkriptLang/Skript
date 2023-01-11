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

import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.util.EnumUtils;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class can be used for an easier writing of ClassInfos that are enums,
 * it registers a language node with usage, a serializer, default expression and a parser.
 * Making it easier to register enum ClassInfos.
 * @param <T> The enum class.
 */
public class EnumClassInfo<T extends Enum<T>> extends ClassInfo<T> {

	/**
	 * @param c The class
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 */
	public EnumClassInfo(Class<T> c, String codeName, String languageNode) {
		this(c, codeName, languageNode, new EventValueExpression<>(c));
	}

	/**
	 * @param c The class
	 * @param codeName The name used in patterns
	 * @param languageNode The language node of the type
	 * @param defaultExpression The default expression of the type
	 */
	public EnumClassInfo(Class<T> c, String codeName, String languageNode, DefaultExpression<T> defaultExpression) {
		super(c, codeName);
		EnumUtils<T> enumUtils = new EnumUtils<>(c, languageNode);
		usage(enumUtils.getAllNames())
			.serializer(new EnumSerializer<>(c))
			.defaultExpression(defaultExpression)
			.parser(new Parser<T>() {
				@Override
				@Nullable
				public T parse(String s, ParseContext context) {
					return enumUtils.parse(s);
				}

				@Override
				public String toString(T o, int flags) {
					return enumUtils.toString(o, flags);
				}

				@Override
				public String toVariableNameString(T o) {
					return o.name();
				}
			});
	}

}
