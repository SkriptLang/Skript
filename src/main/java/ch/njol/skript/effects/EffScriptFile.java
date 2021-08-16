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
import java.io.IOException;
import java.util.Collections;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptCommand;
import ch.njol.skript.config.Config;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.FileUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.OpenCloseable;

@Name("Enable/Disable/Reload Script File")
@Description("Enables, disables, or reloads a script file.")
@Examples({"reload script \"test\"",
			"enable script file \"testing\"",
			"unload script file \"script.sk\""})
@Since("2.4")
public class EffScriptFile extends Effect {
	static {
		Skript.registerEffect(EffScriptFile.class, "(1¦enable|1¦load|2¦reload|3¦disable|3¦unload) s(c|k)ript [file] %string%");
	}
	
	private static final int ENABLE = 1, RELOAD = 2, DISABLE = 3;
	
	private int mark;
	@Nullable
	private Expression<String> fileName;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		mark = parseResult.mark;
		fileName = (Expression<String>) exprs[0];
		return true;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return (mark == ENABLE ? "enable" : mark == RELOAD ? "disable" : mark == DISABLE ? "unload" : "") + " script file " + (fileName != null ? fileName.toString(e, debug) : "");
	}
	
	@Override
	protected void execute(Event e) {
		String name = fileName != null ? fileName.getSingle(e) : "";
		File file = SkriptCommand.getScriptFromName(name != null ? name : "");
		if (file == null) {
			return;
		}
		switch (mark) {
			case ENABLE: {
				if (!file.getName().startsWith("-")) {
					return;
				}
				
				try {
					file = FileUtils.move(file, new File(file.getParentFile(), file.getName().substring(1)), false);
				} catch (final IOException ex) {
					Skript.exception(ex, "Error while enabling script file: " + name);
					return;
				}
				Config config = ScriptLoader.loadStructure(file);
				if (config != null)
					ScriptLoader.loadScripts(Collections.singletonList(config), OpenCloseable.EMPTY);
				break;
			}
			case RELOAD: {
				if (file.getName().startsWith("-")) {
					return;
				}
				
				ScriptLoader.reloadScript(file, OpenCloseable.EMPTY);
				break;
			}
			case DISABLE: {
				if (file.getName().startsWith("-")) {
					return;
				}
				
				ScriptLoader.unloadScript(file);
				try {
					FileUtils.move(file, new File(file.getParentFile(), "-" + file.getName()), false);
				} catch (final IOException ex) {
					Skript.exception(ex, "Error while disabling script file: " + name);
					return;
				}
				break;
			}
			default: {
				assert false;
				return;
			}
		}
	}
}
