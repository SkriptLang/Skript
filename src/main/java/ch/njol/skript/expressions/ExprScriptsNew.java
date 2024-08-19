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
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Feature;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Name("All Scripts (Experimental)")
@Description("Returns all of the scripts, or just the enabled or disabled ones.")
@Examples({
	"command /scripts:",
	"\ttrigger:",
	"\t\tsend \"All Scripts: %scripts%\" to player",
	"\t\tsend \"Loaded Scripts: %enabled scripts%\" to player",
	"\t\tsend \"Unloaded Scripts: %disabled scripts%\" to player"
})
@Since("INSERT VERSION")
public class ExprScriptsNew extends SimpleExpression<Script> {

	static {
		Skript.registerExpression(ExprScriptsNew.class, Script.class, ExpressionType.SIMPLE,
				"[all [[of] the]|the] scripts",
				"[all [[of] the]|the] (enabled|loaded) scripts",
				"[all [[of] the]|the] (disabled|unloaded) scripts");
	}

	private boolean includeEnabled;
	private boolean includeDisabled;
	private int pattern;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!this.getParser().hasExperiment(Feature.SCRIPT_REFLECTION))
			return false;
		this.includeEnabled = matchedPattern <= 1;
		this.includeDisabled = matchedPattern != 1;
		this.pattern = matchedPattern;
		return true;
	}

	@Override
	protected Script[] get(Event event) {
		List<Script> scripts = new ArrayList<>();
		if (includeEnabled)
			scripts.addAll(ScriptLoader.getLoadedScripts());
		if (includeDisabled) {
			scripts.addAll(ScriptLoader.getDisabledScripts()
					.stream()
					.map(ExprScript::getHandle)
					.filter(Objects::nonNull)
					.toList());
		}
		return scripts.toArray(new Script[0]);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends Script> getReturnType() {
		return Script.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (pattern == 1)
		    return "all enabled scripts";
		else if (pattern == 2)
			return "all disabled scripts";
		return "all scripts";
	}

}
