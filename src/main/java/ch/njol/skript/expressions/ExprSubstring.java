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

import java.util.ArrayList;
import java.util.List;

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
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Subtext")
@Description("Extracts part of a text. You can either get the first &lt;x&gt; characters, the last &lt;x&gt; characters, the character at index &lt;x&gt;, or the characters between indices &lt;x&gt; and &lt;y&gt;."
		+ " The indices &lt;x&gt; and &lt;y&gt; should be between 1 and the <a href='#ExprLength'>length</a> of the text (other values will be fit into this range).")
@Examples({"set {_s} to the first 5 characters of the text argument",
		"message \"%subtext of {_s} from characters 2 to (the length of {_s} - 1)%\" # removes the first and last character from {_s} and sends it to the player or console",
		"set {_characters::*} to characters at 1, 2 and 7 in player's display name",
		"send the last character of all players' names"})
@Since("2.1, 2.5.2 (character at, multiple strings support)")
public class ExprSubstring extends SimpleExpression<String> {
	static {
		Skript.registerExpression(ExprSubstring.class, String.class, ExpressionType.COMBINED,
				"[the] (part|sub[ ](text|string)) of %strings% (between|from) (ind(ex|ices)|character[s]|) %number% (and|to) (index|character|) %number%",
				"[the] (1¦first|2¦last) [%-number%] character[s] of %strings%", "[the] %number% (1¦first|2¦last) characters of %strings%",
				"[the] character[s] at [(index|position|indexes|indices|positions)] %numbers% (in|of) %strings%");
	}
	
	@SuppressWarnings("null")
	private Expression<String> string;
	@Nullable
	private Expression<Number> start, end;
	private boolean usedSubstring;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		usedSubstring = matchedPattern == 0;
		string = (Expression<String>) exprs[usedSubstring ? 0 : 1];
		start = (Expression<Number>) (usedSubstring ? exprs[1] : parseResult.mark == 1 ? null : exprs[0] == null ? new SimpleLiteral<>(1, false) : exprs[0]);
		end = (Expression<Number>) (usedSubstring ? exprs[2] : parseResult.mark == 2 ? null : exprs[0] == null ? new SimpleLiteral<>(1, false) : exprs[0]);
		assert end != null || start != null;
		return true;
	}
	
	@Override
	@Nullable
	@SuppressWarnings("null")
	protected String[] get(final Event e) {
		final List<String> parts = new ArrayList<>();
		final String[] strings = string.getArray(e);
		if (strings == null)
			return new String[0];
		for (String string : strings) {
			if (start != null && !start.isSingle()) {
				Number[] i = start.getArray(e);
				if (i == null) return new String[0];
				for (Number p : i) {
					if (p.intValue() > string.length() || p.intValue() < 1) continue;
					parts.add(string.substring(p.intValue() - 1, p.intValue()));
				}
			} else {
				Number d1 = start != null ? start.getSingle(e) : 1;
				Number d2 = end != null ? end.getSingle(e) : string.length();
				if (d1 == null || d2 == null) continue;
				if (end == null) d1 = string.length() - d1.intValue() + 1;
				int i1 = Math.max(d1.intValue() - 1, 0);
				int i2 = Math.min(d2.intValue(), string.length());
				if (i1 >= i2) continue;
				parts.add(string.substring(i1, i2));
			}
		}
		return parts.toArray(new String[parts.size()]);
	}
	
	@Override
	@SuppressWarnings("null")
	public boolean isSingle() {
		return string.isSingle() && (start == null || start.isSingle());
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	@SuppressWarnings("null")
	public String toString(final @Nullable Event e, final boolean debug) {
		if (start == null) {
			assert end != null;
			return "the first " + end.toString(e, debug) + " characters of " + string.toString(e, debug);
		} else if (end == null) {
			assert start != null;
			return "the last " + start.toString(e, debug) + " characters of " + string.toString(e, debug);
		} else if (usedSubstring) {
			return "the substring of " + string.toString(e, debug) + " from index " + start.toString(e, debug) + " to " + end.toString(e, debug);
		} else {
			return "the character at " + (start.isSingle() ? "index " : "indexes ") + start.toString(e, debug) + " in " + string.toString(e, debug);
		}
	}
	
}
