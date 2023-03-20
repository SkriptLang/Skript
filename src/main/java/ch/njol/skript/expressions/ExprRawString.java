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
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Name("Raw String")
@Description("Returns the string without formatting (colors etc.) and without stripping them from it, " +
	"e.g. <code>raw \"&aHello There!\"</code> would output <code>&aHello There!</code>")
@Examples("send raw \"&aThis text is unformatted!\" to all players")
@Since("2.7")
public class ExprRawString extends SimpleExpression<String> {

	private static final Pattern HEX_PATTERN = Pattern.compile("(?i)&x((?:&\\p{XDigit}){6})");

	static {
		Skript.registerExpression(ExprRawString.class, String.class, ExpressionType.COMBINED, "raw %strings%");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<String> expr;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<? extends String>[] messages;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		expr = (Expression<String>) exprs[0];
		messages = expr instanceof ExpressionList<?> ?
			((ExpressionList<String>) expr).getExpressions() : new Expression[]{expr};
		for (Expression<? extends String> message : messages) {
			if (message instanceof ExprColoured) {
				Skript.error("The 'colored' expression may not be used in a 'raw string' expression");
				return false;
			}
		}
		return true;
	}

	@Override
	protected String[] get(Event event) {
		List<String> strings = new ArrayList<>();
		for (Expression<? extends String> message : messages) {
			if (message instanceof VariableString) {
				strings.add(((VariableString) message).toUnformattedString(event));
				continue;
			}
			for (String string : message.getArray(event)) {
				String raw = SkriptColor.replaceColorChar(string);
				if (raw.toLowerCase().contains("&x")) {
					raw = HEX_PATTERN.matcher(raw).replaceAll(matchResult ->
						"<#" + matchResult.group(1).replace("&", "") + '>');
				}
				strings.add(raw);
			}
		}
		return strings.toArray(new String[0]);
	}

	@Override
	public boolean isSingle() {
		return expr.isSingle();
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
