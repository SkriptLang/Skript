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
package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.lang.script.ScriptWarning;

@Name("Locally Suppress Warning")
@Description("Suppresses target warnings from the current script.")
@Examples({
	"locally suppress missing conjunction warnings",
	"suppress the variable save warnings"
})
@Since("2.3")
public class EffSuppressWarnings extends Effect {

	static {
		Skript.registerEffect(EffSuppressWarnings.class,
			"[local[ly]] suppress [the] (1:conflict|2:variable save|3:[missing] conjunction[s]|4:starting [with] expression[s]|5:deprecated syntax) warning[s]"
		);
	}

	private static final int CONFLICT = 1, INSTANCE = 2, CONJUNCTION = 3, START_EXPR = 4, DEPRECATED = 5;
	private int mark = 0;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!getParser().isActive()) {
			Skript.error("You can't suppress warnings outside of a script!");
			return false;
		}

		mark = parseResult.mark;
		if (mark == 1) {
			Skript.warning("Variable conflict warnings no longer need suppression, as they have been removed altogether");
		} else {
			getParser().getCurrentScript().suppressWarning(ScriptWarning.values()[mark - 2]);
		}
		return true;
	}

	@Override
	protected void execute(Event event) { }

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String word;
		switch (mark) {
			case CONFLICT:
				word = "conflict";
				break;
			case INSTANCE:
				word = "variable save";
				break;
			case CONJUNCTION:
				word = "missing conjunction";
				break;
			case START_EXPR:
				word = "starting expression";
				break;
			case DEPRECATED:
				word = "deprecated syntax";
				break;
			default:
				throw new IllegalStateException();
		}
		return "suppress " + word + " warnings";
	}

}
