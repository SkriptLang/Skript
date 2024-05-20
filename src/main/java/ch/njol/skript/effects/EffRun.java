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

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.function.DynamicFunctionReference;
import ch.njol.skript.lang.function.FunctionReference;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Feature;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.util.Executable;

import java.util.*;

@Name("Run (Experimental)")
@Description({
		"Executes a task (a function). Any returned result is discarded."
})
@Examples({
		"set {_function} to the function named \"myFunction\"",
		"run {_function}",
		"run {_function} with arguments {_things::*}",
})
@Since("INSERT VERSION")
@Keywords({"run", "execute", "reflection", "function"})
@SuppressWarnings({"rawtypes", "unchecked", "NotNullFieldNotInitialized"})
public class EffRun extends Effect {

	static {
		Skript.registerEffect(EffRun.class,
				"run %executable% [arguments:with arg[ument]s %-objects%]",
				"execute %executable% [arguments:with arg[ument]s %-objects%]");
	}

	// We don't bother with the generic type here because we have no way to verify it
	// from the expression, and it makes casting more difficult to no benefit.
	private Expression<Executable> executable;
	private Expression<?> arguments;
	private DynamicFunctionReference.Input input;
	private boolean hasArguments;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean isDelayed, ParseResult result) {
		if (!this.getParser().hasExperiment(Feature.SCRIPT_REFLECTION))
			return false;
		this.executable = ((Expression<Executable>) expressions[0]);
		this.hasArguments = result.hasTag("arguments");
		if (hasArguments) {
			this.arguments = LiteralUtils.defendExpression(expressions[1]);
			Expression<?>[] arguments;
			if (this.arguments instanceof ExpressionList<?>)
				arguments = ((ExpressionList<?>) this.arguments).getExpressions();
			else
				arguments = new Expression[]{this.arguments};
			this.input = new DynamicFunctionReference.Input(arguments);
			return LiteralUtils.canInitSafely(this.arguments);
		} else {
			this.input = new DynamicFunctionReference.Input();
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		@Nullable Executable task = executable.getSingle(event);
		if (task == null)
			return;
		Object[] arguments;
		if (task instanceof DynamicFunctionReference) {
			DynamicFunctionReference<?> reference = (DynamicFunctionReference) task;
			Expression<?> validated = reference.validate(input);
			if (validated == null)
				return;
			arguments = validated.getArray(event);
		} else if (hasArguments) {
			arguments = this.arguments.getArray(event);
		} else {
			arguments = new Object[0];
		}
		task.execute(event, arguments);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (hasArguments)
			return "run " + executable.toString(event, debug) + " with arguments " + arguments.toString(event, debug);
		return "run " + executable.toString(event, debug);
	}

}
