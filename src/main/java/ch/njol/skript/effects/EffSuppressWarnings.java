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
import org.jetbrains.annotations.Nullable;
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
			"[local[ly]] suppress [the] (1:variable save|2:[missing] conjunction[s]|3:starting [with] expression[s]|4:deprecated syntax|5:local variable type[s]) warning[s]"
		);
	}

	private enum Pattern {
		INSTANCE(ScriptWarning.VARIABLE_SAVE),
		CONJUNCTION(ScriptWarning.MISSING_CONJUNCTION),
		START_EXPR(ScriptWarning.VARIABLE_STARTS_WITH_EXPRESSION),
		DEPRECATED(ScriptWarning.DEPRECATED_SYNTAX),
		LOCAL_TYPES(ScriptWarning.LOCAL_VARIABLE_TYPE);

		private final ScriptWarning warning;

		Pattern(ScriptWarning warning) {
			this.warning = warning;
		}

		public ScriptWarning getWarning() {
			return warning;
		}

	}

	private Pattern pattern;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!getParser().isActive()) {
			Skript.error("You can't suppress warnings outside of a script!");
			return false;
		}

		pattern = Pattern.values()[parseResult.mark - 1];
		getParser().getCurrentScript().suppressWarning(pattern.getWarning());
		return true;
	}

	@Override
	protected void execute(Event event) { }

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String word;
		switch (pattern) {
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
			case LOCAL_TYPES:
				word = "local variable types";
				break;
			default:
				throw new IllegalStateException();
		}
		return "suppress " + word + " warnings";
	}

}
