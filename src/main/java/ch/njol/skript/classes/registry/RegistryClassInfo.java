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
package ch.njol.skript.classes.registry;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.ParseContext;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class can be used for an easier writing of ClassInfos that are registries,
 * it registers a language node with usage, a serializer, default expression and a parser.
 * Making it easier to register registry ClassInfos.
 *
 * @param <R> The Registry class.
 */
public class RegistryClassInfo<R extends Keyed> extends ClassInfo<R> {

	public RegistryClassInfo(Class<R> registryClass, Registry<R> registry, String codeName, String languageNode) {
		this(registryClass, registry, codeName, languageNode, new EventValueExpression<>(registryClass));
	}

	/**
	 * @param registry The registry
	 * @param codeName The name used in patterns
	 */
	public RegistryClassInfo(Class<R> registryClass, Registry<R> registry, String codeName, String languageNode, DefaultExpression<R> defaultExpression) {
		super(registryClass, codeName);
		RegistryUtils<R> registryUtils = new RegistryUtils<>(registry, languageNode);
		usage(registryUtils.getAllNames())
			.serializer(new RegistrySerializer<R>(registry))
			.defaultExpression(defaultExpression)
			.parser(new Parser<R>() {

				@Override
				public @Nullable R parse(String string, ParseContext context) {
					return registryUtils.parse(string);
				}

				@Override
				public @NotNull String toString(R object, int flags) {
					return registryUtils.toString(object, flags);
				}

				@Override
				public @NotNull String toVariableNameString(R object) {
					return toString(object, 0);
				}
			});
	}

}
