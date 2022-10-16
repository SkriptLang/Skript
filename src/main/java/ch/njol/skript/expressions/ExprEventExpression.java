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
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.NonNullPair;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Provided for convenience: one can write 'event-world' instead of only 'world' to distinguish between the event-world and the loop-world.
 * 
 * @author Peter Güttinger
 */
@NoDoc
public class ExprEventExpression extends WrapperExpression<Object> {

	static {
		Skript.registerExpression(ExprEventExpression.class, Object.class, ExpressionType.PROPERTY, "[the] event-<.+>");// property so that it is parsed after most other expressions
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		String className = parser.regexes.get(0).group(0);

		System.out.println(className);
		System.out.println(Noun.stripIndefiniteArticle(className));
		ClassInfo<?> classInfo = Classes.getClassInfoFromUserInput(Noun.stripIndefiniteArticle(className));
		if (classInfo == null)
			return false;
		Class<?> c = classInfo.getC();

		boolean plural = Utils.getEnglishPlural(className).getSecond();
		EventValueExpression<?> eventValue = new EventValueExpression<Object>(plural ? CollectionUtils.arrayType(c) : c);
		setExpr(eventValue);
		return eventValue.init();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return getExpr().toString(event, debug);
	}

}
