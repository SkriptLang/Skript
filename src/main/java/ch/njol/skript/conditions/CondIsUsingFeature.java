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

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.VariableString;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.lang.script.Script;

@Name("Is Using Experimental Feature")
@Description("Checks whether a script is using an experimental feature by name.")
@Examples({"the script is using \"example feature\"",
		"on load:",
		"\tif the script is using \"example feature\":",
		"\t\tbroadcast \"You're using an experimental feature!\""})
@Since("INSERT VERSION")
public class CondIsUsingFeature extends Condition {
	static {
		Skript.registerCondition(CondIsUsingFeature.class,
								 "[the] [current] script is using %strings%",
								 "[the] [current] script is(n't| not) using %strings%");
	}

	private @UnknownNullability Expression<String> names;
	private @UnknownNullability Script script;
	private @Nullable Boolean knownResult;
	
	@SuppressWarnings("null")
	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result) {
		//noinspection unchecked
		this.names = (Expression<String>) expressions[0];
		this.setNegated(pattern == 1);
		this.script = this.getParser().getCurrentScript();
		// if this is a 'simple' variablestring with no inputs we can check it during parse time
		if (names instanceof VariableString) {
			VariableString string = (VariableString) names;
			if (string.isSimple()) {
				String value = string.toString(null);
				knownResult = this.isNegated() ^ this.hasExperiment(value);
			}
		}
		return true;
	}
	
	@Override
	public boolean check(Event event) {
		if (knownResult != null) // we checked this in advance during init
			return knownResult;
		String[] array = names.getArray(event);
		if (array.length == 0)
			return true;
		boolean isUsing = true;
		for (@NotNull String object : array) {
			isUsing &= this.hasExperiment(object);
		}
		return this.isNegated() ^ isUsing;
	}

	@Override
	public boolean hasExperiment(String name) {
		Experiment experiment = Skript.experiments().find(name);
		return script.hasExperiment(experiment);
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the current script " + (isNegated() ? " isn't" : " is") + " using " + names.toString(event, debug);
	}
	
}
