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
import java.util.regex.PatternSyntaxException;

import ch.njol.skript.SkriptConfig;
import ch.njol.skript.lang.Literal;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

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

@Name("Join & Split")
@Description("Joins several texts with a common delimiter (e.g. \", \"), or splits a text into multiple texts at a given delimiter.")
@Examples({
	"message \"Online players: %join all players' names with \"\" | \"\"%\" # %all players% would use the default \"x, y, and z\"",
	"set {_s::*} to the string argument split at \",\""
})
@Since("2.1, 2.5.2 (regex support), 2.7 (case sensitivity)")
public class ExprJoinSplit extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprJoinSplit.class, String.class, ExpressionType.COMBINED,
			"(concat[enate]|join) %strings% [(with|using|by) [[the] delimiter] %-string%]",
			"split %string% (at|using|by) [[the] delimiter] %string% [case:with case sensitivity]",
			"%string% split (at|using|by) [[the] delimiter] %string% [case:with case sensitivity]",
			"regex split %string% (at|using|by) [[the] delimiter] %string%",
			"regex %string% split (at|using|by) [[the] delimiter] %string%");
	}

	private boolean join;
	private boolean regex;
	private boolean caseSensitivity;

	private Expression<String> strings;
	private @Nullable Expression<String> delimiter;

	private @Nullable Pattern pattern;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		join = matchedPattern == 0;
		regex = matchedPattern >= 3;
		caseSensitivity = SkriptConfig.caseSensitive.value() || parseResult.hasTag("case");
		//noinspection unchecked
		strings = (Expression<String>) exprs[0];
		//noinspection unchecked
		delimiter = (Expression<String>) exprs[1];
		if (!join && delimiter instanceof Literal) {
			String stringPattern = ((Literal<String>) delimiter).getSingle();
			try {
				this.pattern = compilePattern(stringPattern);
			} catch (PatternSyntaxException e) {
				Skript.error("'" + stringPattern + "' is not a valid regular expression");
				return false;
			}
		}
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event) {
		String[] strings = this.strings.getArray(event);
		String delimiter = this.delimiter != null ? this.delimiter.getSingle(event) : "";
		if (strings.length == 0 || delimiter == null)
			return new String[0];
		if (join)
			return new String[] {StringUtils.join(strings, delimiter)};
		try {
			Pattern pattern = this.pattern;
			if (pattern == null)
				pattern = compilePattern(delimiter);
			return pattern.split(strings[0], -1);
		} catch (PatternSyntaxException e) {
			return new String[0];
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
	public String toString(@Nullable Event event, boolean debug) {
		StringBuilder builder = new StringBuilder();
		if (join) {
			builder.append("join ").append(strings.toString(event, debug));
			if (delimiter != null)
				builder.append(" with ").append(delimiter.toString(event, debug));
			return builder.toString();
		}

        assert delimiter != null;
		if (regex)
			builder.append("regex ");
        builder.append("split ")
			.append(strings.toString(event, debug))
			.append(" at ")
			.append(delimiter.toString(event, debug));
		if (!regex)
			builder.append(" (case sensitive: ").append(caseSensitivity).append(")");
		return builder.toString();
	}

	private Pattern compilePattern(String delimiter) {
		return Pattern.compile(regex ? delimiter : (caseSensitivity ? "" : "(?i)") + Pattern.quote(delimiter));
	}

}
