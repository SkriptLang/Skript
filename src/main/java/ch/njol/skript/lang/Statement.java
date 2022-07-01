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

import java.util.Iterator;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.function.EffFunctionCall;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;

/**
 * Supertype of conditions and effects
 *
 * @see Condition
 * @see Effect
 */
public abstract class Statement extends TriggerItem implements SyntaxElement {

	@SuppressWarnings({"rawtypes", "unchecked", "null"})
	@Nullable
	public static Statement parse(String s, String defaultError) {
		ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			EffFunctionCall f = EffFunctionCall.parse(s);
			if (f != null) {
				log.printLog();
				return f;
			} else if (log.hasError()) {
				log.printError();
				return null;
			}
			log.clear();

			EffectSection section = EffectSection.parse(s, null, null, null);
			if (section != null) {
				log.printLog();
				return new EffectSectionEffect(section);
			}
			log.clear();

			Statement statement = (Statement) SkriptParser.parse(s, (Iterator) Skript.getStatements().iterator(), defaultError);
			if (statement != null) {
				log.printLog();
				return statement;
			}

			log.printError();
			return null;
		} finally {
			log.stop();
		}
	}

}
