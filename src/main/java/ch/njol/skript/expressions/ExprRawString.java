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
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ch.njol.skript.lang.SkriptParser.ParseResult;

@Name("Raw String")
@Description("Returns the string without formatting (colors etc.) and without stripping them from it, " +
	"e.g. <code>raw \"&aHello There!\"</code> would output <code>&aHello There!</code>")
@Examples("send raw \"&aThis text is unformatted!\" to all players")
@Since("INSERT VERSION")
public class ExprRawString extends PropertyExpression<String, String> {

	static {
		Skript.registerExpression(ExprRawString.class, String.class, ExpressionType.SIMPLE, "raw %strings%");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<? extends String>[] messages;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(LiteralUtils.defendExpression(exprs[0]));
		messages = getExpr() instanceof ExpressionList<?> ?
			((ExpressionList<String>) getExpr()).getExpressions() : new Expression[]{getExpr()};
		for (Expression<? extends String> message : messages) {
			if (message instanceof ExprColoured) {
				Skript.error("the 'colored' expression may not be used with a 'raw string' expression");
				return false;
			}
		}
		return LiteralUtils.canInitSafely(getExpr());
	}

	@Override
	protected String[] get(Event e, String[] source) {
		List<String> strings = new ArrayList<>();
		for (Expression<? extends String> message : messages) {
			if (message instanceof VariableString) {
				strings.add(((VariableString) message).toUnformattedString(e));
				continue;
			}
			strings.addAll(Arrays.asList(message.getArray(e)));
		}
		return strings.toArray(new String[0]);
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "raw " + getExpr().toString(e, debug);
	}
}
