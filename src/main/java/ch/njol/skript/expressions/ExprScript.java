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

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptCommand;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.parser.ParserInstance;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Name("Script")
@Description({"The current script, or a script from its (file) name.",
	"If the script is enabled or disabled (or reloaded) this reference will become invalid.",
	"Therefore, it is recommended to obtain a script reference <em>when needed</em>."})
@Examples({
	"on script load:",
	"\tbroadcast \"Loaded %the current script%\"",
	"on script load:",
	"\tset {running::%script%} to true",
	"on script unload:",
	"\tset {running::%script%} to false",
	"set {script} to the script named \"weather.sk\"",
	"loop the scripts in directory \"quests/\":",
	"\tenable loop-value"
})
@Since("2.0")
public class ExprScript extends SimpleExpression<Script> {

	static {
		Skript.registerExpression(ExprScript.class, Script.class, ExpressionType.SIMPLE,
			"[the] [current] script",
			"[the] script[s] [named] %strings%",
			"[the] scripts in [directory|folder] %string%"
		);
	}

	private @Nullable Script script;
	private @Nullable Expression<String> name;
	private int mode;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.mode = matchedPattern;
		if (matchedPattern == 0) {
			ParserInstance parser = this.getParser();
			if (!parser.isActive()) {
				Skript.error("You can't use the current script expression outside of scripts!");
				return false;
			}
			this.script = parser.getCurrentScript();
		} else {
			//noinspection unchecked
			this.name = (Expression<String>) exprs[0];
		}
		return true;
	}

	@Override
	protected Script[] get(Event event) {
		if (script != null)
			return new Script[]{script};
		assert name != null;
		if (mode == 2) {
			@Nullable String string = name.getSingle(event);
			if (string == null)
				return new Script[0];
			File folder = new File(Skript.getInstance().getScriptsFolder(), string);
			List<Script> scripts = new ArrayList<>();
			if (!folder.isDirectory())
				return new Script[0];
			this.getScripts(folder, scripts);
			return scripts.toArray(new Script[0]);
		}
		return name.stream(event)
				.map(SkriptCommand::getScriptFromName)
				.map(ExprScript::getHandle)
				.filter(Objects::nonNull)
				.toArray(Script[]::new);
	}

	private void getScripts(File folder, List<Script> scripts) {
		File[] files = folder.listFiles();
		if (files == null)
			return;
		for (File file : files) {
			if (file.isDirectory())
				this.getScripts(file, scripts);
			else if (file.getName().endsWith(".sk")) {
				@Nullable Script handle = ExprScript.getHandle(file);
				if (handle != null)
					scripts.add(handle);
			}
		}
	}

	@Override
	public boolean isSingle() {
		return script != null || name != null && name.isSingle() && mode != 2;
	}

	@Override
	public Class<? extends Script> getReturnType() {
		return Script.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (script != null)
			return "the current script";
		assert name != null;
		if (mode == 2)
			return "the scripts in directory " + name.toString(event, debug);
		if (name.isSingle())
			return "the script named " + name.toString(event, debug);
		return "the scripts named " + name.toString(event, debug);
	}

	static @Nullable Script getHandle(@Nullable File file) {
		if (file == null || file.isDirectory())
			return null;
		Script script = ScriptLoader.getScript(file);
		if (script != null)
			return script;
		return ScriptLoader.createDummyScript(file.getName(), file);
	}

}
