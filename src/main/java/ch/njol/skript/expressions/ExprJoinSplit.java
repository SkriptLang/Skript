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

import java.util.regex.Pattern;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
@Name("Join & Split")
@Description("Joins several texts with a common delimiter (e.g. \", \"), or splits a text into multiple texts at a given delimiter.")
@Examples({"message \"Online players: %join all players with \"\" | \"\"%\" # %all players% would use the default \"x, y, and z\"",
		"set {_s::*} to the string argument split at \",\""})
@Since("2.1, 2.5.2 (regex support)")
public class ExprJoinSplit extends SimpleExpression<String> {
	
	static {
		Skript.registerExpression(ExprJoinSplit.class, String.class, ExpressionType.COMBINED,
			"(concat[enate]|join) %strings% [(with|using|by) [[the] delimiter] %-string%]",
			"split %string% (at|using|by) [[the] delimiter] %string%",
			"%string% split (at|using|by) [[the] delimiter] %string%",
			"regex split %string% (at|using|by) [[the] delimiter] %string%",
			"regex %string% split (at|using|by) [[the] delimiter] %string%");
	}
	
	private boolean join;
	private boolean regex;
	
	@SuppressWarnings("null")
	private Expression<String> strings;
	@Nullable
	private Expression<String> delimiter;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		join = matchedPattern == 0;
		regex = matchedPattern >= 3;
		strings = (Expression<String>) exprs[0];
		delimiter = (Expression<String>) exprs[1];
		return true;
	}
	
	@Override
	@Nullable
	protected String[] get(final Event e) {
		final String[] s = strings.getArray(e);
		final String d = delimiter != null ? delimiter.getSingle(e) : "";
		if (s.length == 0 || d == null)
			return new String[0];
		if (join) {
			return new String[] {StringUtils.join(s, d)};
		} else {
			return s[0].split(regex ? d : Pattern.quote(d), -1);
		}
	}
	
	@Override
	public boolean isSingle() {
		return join;
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return join ? "join " + strings.toString(e, debug) + (delimiter != null ? " with " + delimiter.toString(e, debug) : "") : ((regex ? "regex " : "") + "split " + strings.toString(e, debug) + (delimiter != null ? " at " + delimiter.toString(e, debug) : ""));
	}
	
}
