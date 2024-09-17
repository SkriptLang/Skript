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
import org.jetbrains.annotations.UnknownNullability;
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
		StringBuilder warnings = new StringBuilder();
		ScriptWarning[] values = ScriptWarning.values();
		for (int i = 0; i < values.length; i++) {
			if (i != 0)
				warnings.append('|');
			warnings.append(values[i].ordinal()).append(':').append(values[i].getPattern());
		}
		Skript.registerEffect(EffSuppressWarnings.class, "[local[ly]] suppress [the] (" + warnings + ") warning[s]");
	}

	private @UnknownNullability ScriptWarning warning;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!getParser().isActive()) {
			Skript.error("You can't suppress warnings outside of a script!");
			return false;
		}

		warning = ScriptWarning.values()[parseResult.mark];
		if (warning.isDeprecated()) {
			Skript.warning(warning.getDeprecationMessage());
		} else {
			getParser().getCurrentScript().suppressWarning(warning);
		}
		return true;
	}

	@Override
	protected void execute(Event event) { }

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "suppress " + warning.getWarningName() + " warnings";
	}

}
