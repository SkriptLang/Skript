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

import java.io.File;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.ScriptOptions;
import ch.njol.util.Kleenean;

@Name("Locally Suppress Warning")
@Description("Suppresses target warnings from the current script.")
@Examples({"locally suppress conflict warnings",
			"suppress the variable save warnings"})
@Since("2.3")
public class EffSuppressWarnings extends Effect {
	static {
		Skript.registerEffect(EffSuppressWarnings.class, "[local[ly]] suppress [the] (1¦conflict|2¦variable save|3¦[missing] conjunction[s]|4¦starting [with] expression[s]) warning[s]");
	}
	
	private int CONFLICT = 1, INSTANCE = 2, CONJUNCTION = 3, STARTEXPR = 4;
	private int mark = 0;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		Config cs = getParser().getCurrentScript();
		if (cs == null) {
			Skript.error("You can only suppress warnings for script files!");
			return false;
		}
		File scriptFile = cs.getFile();
		mark = parseResult.mark;
		switch (parseResult.mark) {
			case 1: { //Possible variable conflicts
				ScriptOptions.getInstance().setSuppressWarning(scriptFile, "conflict");
				break;
			}
			case 2: { //Variables cannot be saved
				ScriptOptions.getInstance().setSuppressWarning(scriptFile, "instance var");
				break;
			}
			case 3: { //Missing "and" or "or"
				ScriptOptions.getInstance().setSuppressWarning(scriptFile, "conjunction");
				break;
			}
			case 4: { //Variable starts with expression
				ScriptOptions.getInstance().setSuppressWarning(scriptFile, "start expression");
				break;
			}
			default: {
				throw new AssertionError();
			}
		}
		return true;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "suppress " + (mark == CONFLICT ? "conflict" : mark == INSTANCE ? "variable save" : mark == CONJUNCTION ? "missing conjunction" : mark == STARTEXPR ? "starting expression" : "") + " warnings";
	}

	@Override
	protected void execute(Event e) {

	}

}