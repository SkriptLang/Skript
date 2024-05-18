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

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptCommand;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.script.Script;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.FileUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.OpenCloseable;
import org.bukkit.event.Event;

import java.io.File;
import java.io.IOException;
import java.util.Set;

@Name("Enable/Disable/Reload Script")
@Description("Enables, disables, or reloads a script.")
@Examples({
	"reload script \"test\"",
	"enable script file \"testing\"",
	"unload script file \"script.sk\"",
	"set {_script} to the script \"MyScript.sk\"",
	"reload {_script}"
})
@Since("2.4")
public class EffScriptFile extends Effect {

	static {
		Skript.registerEffect(EffScriptFile.class,
			"(1:(enable|load)|2:reload|3:disable|4:unload) script [file|named] %string%",
			"(1:(enable|load)|2:reload|3:disable|4:unload) skript file %string%",
			"(1:(enable|load)|2:reload|3:disable|4:unload) %scripts%"
		);
		/*
			The string-pattern must come first (since otherwise `script X` would match the expression)
			and we cannot get a script object for a non-loaded script.
		 */
	}

	private static final int ENABLE = 1, RELOAD = 2, DISABLE = 3, UNLOAD = 4;

	private int mark;

	private @UnknownNullability Expression<String> stringExpression;
	private @UnknownNullability Expression<Script> scriptExpression;
	private boolean scripts;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		this.mark = parseResult.mark;
		switch (matchedPattern) {
			case 0:
			case 1:
				this.stringExpression = (Expression<String>) exprs[0];
				break;
			case 2:
				this.scriptExpression = (Expression<Script>) exprs[0];
				this.scripts = true;
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (scripts) {
			Script[] array = scriptExpression.getArray(event);
			for (Script script : array) {
				@Nullable File file = script.getConfig().getFile();
				this.handle(file, script.getConfig().getFileName());
			}
		} else {
			String name = stringExpression.getSingle(event);
			if (name == null)
				return;
			this.handle(SkriptCommand.getScriptFromName(name), name);
		}
	}

	private void handle(@Nullable File scriptFile, @Nullable String name) {
		if (scriptFile == null || !scriptFile.exists())
			return;
		if (name == null)
			name = scriptFile.getName();
		switch (mark) {
			case ENABLE:
				if (ScriptLoader.getLoadedScripts().contains(ScriptLoader.getScript(scriptFile)))
					return;
				if (scriptFile.getName().startsWith(ScriptLoader.DISABLED_SCRIPT_PREFIX)) {
					try {
						// TODO Central methods to be used between here and SkriptCommand should be created for
						//  enabling/disabling (renaming) files
						scriptFile = FileUtils.move(
							scriptFile,
							new File(scriptFile.getParentFile(), scriptFile.getName()
								.substring(ScriptLoader.DISABLED_SCRIPT_PREFIX_LENGTH)),
							false
						);
					} catch (IOException ex) {
						//noinspection ThrowableNotThrown
						Skript.exception(ex, "Error while enabling script file: " + name);
						return;
					}
				}

				ScriptLoader.loadScripts(scriptFile, OpenCloseable.EMPTY);
				break;
			case RELOAD:
				if (ScriptLoader.getDisabledScriptsFilter().accept(scriptFile))
					return;

				this.unloadScripts(scriptFile);

				ScriptLoader.loadScripts(scriptFile, OpenCloseable.EMPTY);
				break;
			case UNLOAD:
				if (!ScriptLoader.getLoadedScriptsFilter().accept(scriptFile))
					return;

				this.unloadScripts(scriptFile);
				break;
			case DISABLE:
				if (ScriptLoader.getDisabledScriptsFilter().accept(scriptFile))
					return;

				this.unloadScripts(scriptFile);

				try {
					FileUtils.move(
						scriptFile,
						new File(scriptFile.getParentFile(), ScriptLoader.DISABLED_SCRIPT_PREFIX + scriptFile.getName()),
						false
					);
				} catch (IOException ex) {
					//noinspection ThrowableNotThrown
					Skript.exception(ex, "Error while disabling script file: " + name);
					return;
				}
				break;
			default:
				assert false;
		}
	}

	private void unloadScripts(File file) {
		Set<Script> loaded = ScriptLoader.getLoadedScripts();
		if (file.isDirectory()) {
			Set<Script> scripts = ScriptLoader.getScripts(file);
			if (scripts.isEmpty())
				return;
			scripts.retainAll(loaded); // skip any that are not loaded (avoid throwing error)
			ScriptLoader.unloadScripts(scripts);
		} else {
			Script script = ScriptLoader.getScript(file);
			if (!loaded.contains(script))
				return; // don't need to unload if not loaded (avoid throwing error)
			if (script != null)
				ScriptLoader.unloadScript(script);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String start = mark == ENABLE ? "enable " : mark == RELOAD ? "disable " : mark == DISABLE ? "unload " : " ";
		if (scripts)
			return start + scriptExpression.toString(event, debug);
		return start + "script file " + stringExpression.toString(event, debug);
	}

}
