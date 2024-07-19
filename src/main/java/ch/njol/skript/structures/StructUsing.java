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
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.lang.structure.Structure;

@Name("Using Experimental Feature")
@Description({
	"Place at the top of a script file to enable an optional experimental feature.",
	"For example, this might include "
})
@Examples({
	"using 1.21",
	"using my-cool-addon-feature"
})
@Since("2.9.0")
public class StructUsing extends Structure {

	public static final Priority PRIORITY = new Priority(15);

	static {
		Skript.registerSimpleStructure(StructUsing.class, "using <.+>");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Experiment experiment;

	@Override
	public boolean init(Literal<?> @NotNull [] arguments, int pattern, ParseResult result, @Nullable EntryContainer container) {
		this.enableExperiment(result.regexes.get(0).group());
		return true;
	}

	private void enableExperiment(String name) {
		this.experiment = Skript.experiments().find(name.trim());
		switch (experiment.phase()) {
			case MAINSTREAM:
				Skript.warning("The experimental feature '" + name + "' is now included by default and is no longer required.");
				break;
			case DEPRECATED:
				Skript.warning("The experimental feature '" + name + "' is deprecated and may be removed in future versions.");
				break;
			case UNKNOWN:
				Skript.warning("The experimental feature '" + name + "' was not found.");
		}
		this.getParser().addExperiment(experiment);
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
		return "using " + experiment.codeName();
	}

}
