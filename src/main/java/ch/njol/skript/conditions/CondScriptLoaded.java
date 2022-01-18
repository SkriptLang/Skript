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
package ch.njol.skript.conditions;

import java.io.File;

import ch.njol.skript.config.Config;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptCommand;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Is Script Loaded")
@Description("Check if the current script, or another script, is currently loaded.")
@Examples({"script is loaded", "script \"example.sk\" is loaded"})
@Since("2.2-dev31")
public class CondScriptLoaded extends Condition {
	
	static {
		Skript.registerCondition(CondScriptLoaded.class,
				"script[s] [%-strings%] (is|are) loaded",
				"script[s] [%-strings%] (isn't|is not|aren't|are not) loaded");
	}
	
	@Nullable
	private Expression<String> scripts;
	@Nullable
	private File currentScriptFile;
	
	@Override
	@SuppressWarnings({"unchecked"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		scripts = (Expression<String>) exprs[0];
		setNegated(matchedPattern == 1);
		Config cs = getParser().getCurrentScript();
		if (cs == null && scripts == null) {
			Skript.error("The condition 'script loaded' requires a script name argument when used outside of script files");
			return false;
		} else if (cs != null) {
			currentScriptFile = cs.getFile();
		}
		return true;
	}

	@Override
	public boolean check(Event e) {
		if (scripts == null) {
			if (currentScriptFile == null)
				return isNegated();
			return ScriptLoader.getLoadedFiles().contains(currentScriptFile) ^ isNegated();
		}
		
		return scripts.check(e,
				scriptName -> ScriptLoader.getLoadedFiles().contains(SkriptCommand.getScriptFromName(scriptName)),
				isNegated());
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		String scriptName;

		if (scripts == null)
			scriptName = "script";
		else
			scriptName = (scripts.isSingle() ? "script" : "scripts" + " " + scripts.toString(e, debug));
		
		boolean isSingle = scripts == null || scripts.isSingle();
		if (isSingle)
			return scriptName + (isNegated() ? " isn't" : " is") + " loaded";
		else
			return scriptName + (isNegated() ? " aren't" : " are") + " loaded";
	}
	
}
