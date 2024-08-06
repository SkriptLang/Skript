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
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Name("Regex Matches")
@Description("Returns all the matches of a string using a regex pattern. Returns nothing if the pattern is invalid.")
@Examples("set {_numbers::*} to the regex matches of {_input} using \"\\d+\"")
@Since("INSERT VERSION")
public class ExprRegexMatches extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprRegexMatches.class, String.class, ExpressionType.COMBINED,
				"[all [[of] the]|the] regex matches of %strings% (with|using) %string%");
	}

	private Expression<String> input, pattern;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		input = (Expression<String>) exprs[0];
		pattern = (Expression<String>) exprs[1];
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event event) {
		Pattern pattern = null;
		try {
			pattern = this.pattern.getOptionalSingle(event).map(Pattern::compile).orElse(null);
		} catch (PatternSyntaxException ignore) {}
		if (pattern == null)
			return new String[0];
        List<String> matches = new ArrayList<>();
        for (String input : input.getArray(event)) {
            Matcher matcher = pattern.matcher(input);
			while (matcher.find())
				matches.add(matcher.group());
        }
        return matches.toArray(new String[0]);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "regex matches of " + input.toString(event, debug) + " using " + pattern.toString(event, debug);
	}

}
