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
package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.VariableString;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Has Annotations")
@Description({
	"Returns true if any annotations are visible to this line."
})
@NoDoc
public class CondHasAnnotations extends Condition {

	static {
		Skript.registerCondition(CondHasAnnotations.class, "annotation %string% [not:not] present");
	}

	private Expression<?> pattern;
	private boolean result, negated;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		String pattern;
		this.pattern = exprs[0];
		if (this.pattern instanceof VariableString) {
			VariableString string = (VariableString) exprs[0];
			if (!string.isSimple())
				return false;
			pattern = string.toString(null);
		} else {
			pattern = exprs[0].toString(null, false);
		}
		this.result = (negated = parseResult.hasTag("not")) ^ this.getParser().hasAnnotationMatching(pattern);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return result;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "annotation" + pattern + (negated ? " not " : " ") + "present";
	}

}
