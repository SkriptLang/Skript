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
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import org.skriptlang.skript.lang.script.Script;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.event.Event;

import org.eclipse.jdt.annotation.Nullable;

@Name("All Scripts")
@Description("Returns all of the scripts, or just the enabled or disabled ones.")
@Examples({
	"command /scripts:",
	"\ttrigger:",
	"\t\tsend \"All Scripts: %scripts%\" to player",
	"\t\tsend \"Loaded Scripts: %enabled scripts%\" to player",
	"\t\tsend \"Unloaded Scripts: %disabled scripts%\" to player"
})
@Since("2.5")
public class ExprScripts extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprScripts.class, String.class, ExpressionType.SIMPLE,
				"[all [of the]|the] scripts [1:without ([subdirectory] paths|parents)]",
				"[all [of the]|the] (enabled|loaded) scripts [1:without ([subdirectory] paths|parents)]",
				"[all [of the]|the] (disabled|unloaded) scripts [1:without ([subdirectory] paths|parents)]");
	}

	private static final Path SCRIPTS_PATH = Skript.getInstance().getScriptsFolder().getAbsoluteFile().toPath();

	private boolean includeEnabled;
	private boolean includeDisabled;
	private boolean noPaths;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		includeEnabled = matchedPattern <= 1;
		includeDisabled = matchedPattern != 1;
		noPaths = parseResult.mark == 1;
		return true;
	}

	@Override
	protected String[] get(Event event) {
		List<Path> scripts = new ArrayList<>();
		if (includeEnabled) {
			for (Script script : ScriptLoader.getLoadedScripts())
				scripts.add(script.getConfig().getPath());
		}
		if (includeDisabled)
			scripts.addAll(ScriptLoader.getDisabledScripts()
					.stream()
					.map(File::toPath)
					.collect(Collectors.toList()));
		return formatPaths(scripts);
	}

	@SuppressWarnings("null")
	private String[] formatPaths(List<Path> paths) {
		return paths.stream()
			.map(path -> {
				if (noPaths)
					return path.getFileName();
				return SCRIPTS_PATH.relativize(path.toAbsolutePath()).toString();
			})
			.toArray(String[]::new);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "scripts";
	}

}
