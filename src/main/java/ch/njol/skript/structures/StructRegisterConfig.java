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
package ch.njol.skript.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.registrations.Feature;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;

@Name("Register Config")
@Description({
	"Register a custom configuration file that can be accessed from this script.",
	"By default, custom configs are loaded from the `Skript/configs/` folder.",
	"A config will be loaded at the same time as a script that registers it."
})
@Examples({
	"register config \"my cool config\"",
	"on load:",
	"\tset {_config} to the config named \"my cool config\"",
	"\tset {_node} to node \"welcome message\" of {_config}",
	"\tbroadcast the text value of {_node}"
})
@Since("INSERT VERSION")
@SuppressWarnings("NotNullFieldNotInitialized")
public class StructRegisterConfig extends Structure {

	public static final Priority PRIORITY = new Priority(18);

	static {
		Skript.registerSimpleStructure(StructRegisterConfig.class, "register config [file] %*string%");
	}

	private Literal<String> name;

	@Override
	public boolean init(Literal<?> @NotNull [] arguments, int pattern, ParseResult result, @Nullable EntryContainer container) {
		if (!this.getParser().hasExperiment(Feature.SCRIPT_REFLECTION))
			return false;
		//noinspection unchecked
		this.name = (Literal<String>) arguments[0];
		String string = name.getSingle();
		Script script = this.getParser().getCurrentScript();
		return Skript.userConfigs().register(script, string);
	}

	@Override
	public boolean load() {
		return true;
	}

	@Override
	public Priority getPriority() {
		return PRIORITY;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "register config " + name.toString(event, debug);
	}

}
