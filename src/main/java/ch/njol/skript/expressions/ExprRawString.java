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
import ch.njol.skript.classes.Converter;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ExprRawString extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprRawString.class, String.class, ExpressionType.SIMPLE, "raw %strings%");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> expr;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<? extends String>[] messages;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		expr = LiteralUtils.defendExpression(exprs[0]);
		messages = expr instanceof ExpressionList<?> ?
			((ExpressionList<String>) expr).getExpressions() : new Expression[]{expr};
		for (Expression<? extends String> message : messages) {
			if (message instanceof ExprColoured) {
				Skript.error("the 'colored' expression may not be used with a 'raw string' expression", ErrorQuality.SEMANTIC_ERROR);
				return false;
			}
		}
		return LiteralUtils.canInitSafely(expr);
	}

	@Override
	protected @Nullable String[] get(Event e) {
		List<String> strings = new ArrayList<>();
		for (Expression<? extends String> message : messages) {
			if (message instanceof VariableString) {
				strings.add(((VariableString) message).toUnformattedString(e));
				continue;
			}
			strings.addAll(List.of(message.getArray(e)));
		}
		return strings.toArray(new String[0]);
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "raw " + expr.toString(e, debug);
	}
}
