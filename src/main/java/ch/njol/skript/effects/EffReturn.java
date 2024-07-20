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
package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ReturnHandler;
import ch.njol.skript.lang.ReturnHandler.ReturnHandlerStack;
import ch.njol.skript.lang.SectionExitHandler;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Return")
@Description("Makes a trigger (e.g. a function) return a value")
@Examples({
	"function double(i: number) :: number:",
		"\treturn 2 * {_i}",
	"",
	"function divide(i: number) returns number:",
		"\treturn {_i} / 2"
})
@Since("2.2, 2.8.0 (returns aliases)")
public class EffReturn extends Effect {

	static {
		Skript.registerEffect(EffReturn.class, "return %objects%");
		ParserInstance.registerData(ReturnHandlerStack.class, ReturnHandlerStack::new);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private ReturnHandler<?> handler;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> value;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		handler = getParser().getData(ReturnHandlerStack.class).getCurrentHandler();
		if (handler == null) {
			Skript.error("The return statement cannot be used here");
			return false;
		}

		if (!isDelayed.isFalse()) {
			Skript.error("A return statement after a delay is useless, as the calling trigger will resume when the delay starts (and won't get any returned value)");
			return false;
		}

		Class<?> returnType = handler.returnValueType();
		if (returnType == null) {
			Skript.error(handler + " doesn't return any value. Please use 'stop' or 'exit' if you want to stop the trigger.");
			return false;
		}

		RetainingLogHandler log = SkriptLogger.startRetainingLog();
		Expression<?> convertedExpr;
		try {
			convertedExpr = exprs[0].getConvertedExpression(returnType);
			if (convertedExpr == null) {
				String typeName = Classes.getSuperClassInfo(returnType).getName().withIndefiniteArticle();
				log.printErrors(handler + " is declared to return " + typeName + ", but " + exprs[0].toString(null, false) + " is not of that type.");
				return false;
			}
			log.printLog();
		} finally {
			log.stop();
		}

		if (handler.isSingleReturnValue() && !convertedExpr.isSingle()) {
			Skript.error(handler + " is defined to only return a single " + returnType + ", but this return statement can return multiple values.");
			return false;
		}
		value = convertedExpr;

		return true;
	}

	@Override
	@Nullable
	protected TriggerItem walk(Event event) {
		debug(event, false);
		//noinspection rawtypes,unchecked
		((ReturnHandler) handler).returnValues(event, value);

		TriggerSection parent = getParent();
		while (parent != null && parent != handler) {
			if (parent instanceof SectionExitHandler)
				((SectionExitHandler) parent).exit(event);

			parent = parent.getParent();
		}

		if (handler instanceof SectionExitHandler)
			((SectionExitHandler) handler).exit(event);

		return null;
	}

	@Override
	protected void execute(Event event) {
		assert false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "return " + value.toString(event, debug);
	}

}
