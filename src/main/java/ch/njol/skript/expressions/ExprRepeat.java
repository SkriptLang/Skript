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
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Repeat String")
@Description("repeats a given string, a number of times")
@Examples({
	"\"Hello, World! \" repeated 2 times",
	"nl and nl repeated 5 times"
})
@Since("INSERT VERSION")
public class ExprRepeat extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprRepeat.class, String.class, ExpressionType.COMBINED, "%strings% repeated %number% times");
	}

	private Expression<String> strings;
	private Expression<Number> count;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		strings = (Expression<String>) exprs[0];
		count = (Expression<Number>) exprs[1];
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event event) {
		List<String> stringList = new ArrayList<>();
		int count = this.count.getOptionalSingle(event).orElse(1).intValue();
		for (String string : this.strings.getArray(event)) {
			if (count <= 1) {
				stringList.add(string);
				continue;
			}
			stringList.add(StringUtils.multiply(string, count));
		}
		return stringList.toArray(new String[0]);
	}

	@Override
	public boolean isSingle() {
		return strings.isSingle();
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	@Nullable
	public String toString(Event event, boolean debug) {
		return strings.toString(event, debug) + " repeated " + count.toString(event, debug) + " times";
	}

}
