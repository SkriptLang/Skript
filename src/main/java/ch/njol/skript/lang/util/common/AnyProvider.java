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
package ch.njol.skript.lang.util.common;

import org.skriptlang.skript.lang.converter.Converter;

import java.util.function.Function;

/**
 * 'AnyProvider' types are holders for common properties (e.g. name, size) where
 * it is highly likely that things other than Skript may wish to register
 * exponents of the property.
 * <br/>
 * <br/>
 * If possible, types should implement an {@link AnyProvider} subtype directly for
 * the best possible parsing efficiency.
 * However, implementing the interface may not be possible if:
 * <ul>
 *     <li>registering an existing class from a third-party library</li>
 *     <li>the subtype getter method conflicts with the type's own methods
 *     or erasure</li>
 *     <li>the presence of the supertype might confuse the class's design</li>
 * </ul>
 * In these cases, a converter from the class to the AnyX type can be registered.
 * <br/>
 * <br/>
 * The root provider supertype cannot include its own common methods, since these
 * may conflict between things that provide two values (e.g. something declaring
 * both a name and a size)
 */
public interface AnyProvider {

	static <To, From extends AnyProvider> Converter<To, From> convert(Function<To, From> from) {
		return from::apply;
	}
}
