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

import ch.njol.skript.Skript;
import ch.njol.skript.lang.function.EffFunctionCall;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Iterator;

/**
 * Supertype of conditions and effects
 *
 * @see Condition
 * @see Effect
 */
public abstract class Statement extends TriggerItem implements SyntaxElement {

	@Nullable
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static Statement parse(String input, String defaultError) {
		ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			EffFunctionCall functionCall = EffFunctionCall.parse(input);
			if (functionCall != null) {
				log.printLog();
				return functionCall;
			} else if (log.hasError()) {
				log.printError();
				return null;
			}
			log.clear();

			EffectSection section = EffectSection.parse(input, null, null, null);
			if (section != null) {
				log.printLog();
				return new EffectSectionEffect(section);
			}
			log.clear();

			Statement statement = (Statement) SkriptParser.parse(input, (Iterator) Skript.getStatements().iterator(), defaultError);
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
