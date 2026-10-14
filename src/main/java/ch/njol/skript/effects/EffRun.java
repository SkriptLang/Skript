package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.experiments.ReflectionExperimentSyntax;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.util.Executable;

@Name("Run")
@Description("Executes a task (a function). Any returned result is discarded.")
@Example("""
	set {_function} to the function named "myFunction"
	run {_function}
	run {_function} with arguments {_things::*}
	""")
@Since("2.10")
@Keywords({"run", "execute", "reflection", "function"})
@SuppressWarnings({"rawtypes", "unchecked"})
public class EffRun extends Effect implements ReflectionExperimentSyntax {

	static {
		Skript.registerEffect(EffRun.class,
				"run %executable% [arguments:with arg[ument]s %-objects%]",
				"execute %executable% [arguments:with arg[ument]s %-objects%]");
	}

	// We don't bother with the generic type here because we have no way to verify it
	// from the expression, and it makes casting more difficult to no benefit.
	private Expression<Executable> executable;
	private Expression<?> arguments;
	private Expression<?>[] argumentExpressions;
	private boolean hasArguments;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean isDelayed, ParseResult result) {
		this.executable = ((Expression<Executable>) expressions[0]);
		this.hasArguments = result.hasTag("arguments");
		if (hasArguments) {
			this.arguments = LiteralUtils.defendExpression(expressions[1]);
			Expression<?>[] arguments;
			if (this.arguments instanceof ExpressionList<?> expressionList) {
				arguments = expressionList.getExpressions();
			} else {
				arguments = new Expression[]{this.arguments};
			}
			this.argumentExpressions = arguments;
			return LiteralUtils.canInitSafely(this.arguments);
		} else {
			this.argumentExpressions = new Expression[0];
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		Executable task = executable.getSingle(event);
		if (task == null)
			return;
		// something which decides for itself what its arguments mean, such as a function with
		// parameters, is handed the argument expressions rather than a flat list of their values
		Executable.BoundExecutable<Event, ?> boundExecutable = task.bind(argumentExpressions);
		if (boundExecutable != null) {
			Executable.run(boundExecutable, event, this::error);
			return;
		}

		Object[] arguments;
		if (hasArguments) {
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
