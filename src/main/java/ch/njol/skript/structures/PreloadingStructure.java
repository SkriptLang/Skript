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
package ch.njol.skript.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A {@link Structure} which is parsed before any other triggers or structures.
 * <br>
 * This is, for example, used by functions to assure
 * they can be used in a script, even before their definition.
 *
 * @see Structure
 * @see Skript#registerStructure(Class, String...)
 */
public abstract class PreloadingStructure extends Structure {

	/**
	 * This method is not called during the normal parsing round, instead it is called before
	 * any other structures' {@code init} method.
	 * <br>
	 * Implementations should not load script code in this method, because certain language features,
	 * such as options and functions, may not be available yet. Instead, code should be loaded
	 * in {@link #init(SectionNode)}.
	 *
	 * @see ch.njol.skript.lang.SyntaxElement#init(Expression[], int, Kleenean, ParseResult).
	 */
	@Override
	public abstract boolean init(Expression<?>[] exprs,
								 int matchedPattern,
								 Kleenean isDelayed,
								 ParseResult parseResult,
								 SectionNode node);

	/**
	 * This method is called when the normal parsing process arrives at this structure,
	 * which means any code should be loaded here, instead of in {@link #init(Expression[], int, Kleenean, ParseResult, SectionNode)}.
	 */
	public void init(SectionNode node) { }

	@Nullable
	public static PreloadingStructure parse(String expr, SectionNode sectionNode, int priority) {
		Structure.setNode(sectionNode);

		ParseLogHandler parseLogHandler = SkriptLogger.startParseLogHandler();
		try {
			PreloadingStructure preloadingStructure = SkriptParser.parseStatic(expr,
				Skript.getPreloadingStructures().stream().filter(info -> info.getPriority() == priority).iterator(), null);
			if (preloadingStructure != null) {
				parseLogHandler.printLog();
				return preloadingStructure;
			}
			parseLogHandler.printError();
			return null;
		} finally {
			parseLogHandler.stop();
		}
	}

}
