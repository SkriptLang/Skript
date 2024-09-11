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

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptCommand;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.parser.ParserInstance;
import org.skriptlang.skript.lang.script.Script;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.io.File;

@Name("Is Script Loaded")
@Description("Check if the current script, or another script, is currently loaded.")
@Examples({
	"script is loaded",
	"script \"example.sk\" is loaded"
})
@Since("2.2-dev31")
public class CondScriptLoaded extends Condition {
	
	static {
		Skript.registerCondition(CondScriptLoaded.class,
				"script[s] [%-strings%] (is|are) loaded",
				"script[s] [%-strings%] (isn't|is not|aren't|are not) loaded"
		);
	}
	
	@Nullable
	private Expression<String> scripts;
	@Nullable
	private Script currentScript;
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		scripts = (Expression<String>) exprs[0];

		ParserInstance parser = getParser();
		if (scripts == null) {
			if (parser.isActive()) { // no scripts provided means use current script
				currentScript = parser.getCurrentScript();
			} else { // parser is inactive but no scripts were provided
				Skript.error("The condition 'script loaded' requires a script name argument when used outside of script files");
				return false;
			}
		}

		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (scripts == null)
			return ScriptLoader.getLoadedScripts().contains(currentScript) ^ isNegated();
		return scripts.check(event, scriptName -> {
			File scriptFile = SkriptCommand.getScriptFromName(scriptName);
			return scriptFile != null && ScriptLoader.getLoadedScripts().contains(ScriptLoader.getScript(scriptFile));
		}, isNegated());
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String scriptName = scripts == null ?
			"script" : (scripts.isSingle() ? "script" : "scripts" + " " + scripts.toString(event, debug));
		if (scripts == null || scripts.isSingle())
			return scriptName + (isNegated() ? " isn't" : " is") + " loaded";
		return scriptName + (isNegated() ? " aren't" : " are") + " loaded";
	}
	
}
