package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.experiments.ReflectionExperimentSyntax;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.util.Executable;

@Name("Result")
@Description({
	"Runs something (like a function) and returns its result.",
	"If the thing is expected to return multiple values, use 'results' instead of 'result'."
})
@Example("set {_function} to the function named \"myFunction\"")
@Example("set {_result} to the result of {_function}")
@Example("set {_list::*} to the results of {_function}")
@Example("set {_result} to the result of {_function} with arguments 13 and true")
@Since("2.10")
@Keywords({"run", "result", "execute", "function", "reflection"})
public class ExprResult extends PropertyExpression<Executable<Event, Object>, Object> implements ReflectionExperimentSyntax {

	static {
		Skript.registerExpression(ExprResult.class, Object.class, ExpressionType.COMBINED,
			"[the] result[plural:s] of [running|executing] %executable% [arguments:with arg[ument]s %-objects%]");
	}

	private Expression<?> arguments;
	private boolean hasArguments, isPlural;
	private Expression<?>[] argumentExpressions;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult result) {
		//noinspection unchecked
		this.setExpr((Expression<? extends Executable<Event, Object>>) expressions[0]);
		this.hasArguments = result.hasTag("arguments");
		this.isPlural = result.hasTag("plural");
		if (hasArguments) {
			this.arguments = LiteralUtils.defendExpression(expressions[1]);
			Expression<?>[] arguments;
			if (this.arguments instanceof ExpressionList<?> list) {
				arguments = list.getExpressions();
			} else {
				arguments = new Expression[] {this.arguments};
			}
			this.argumentExpressions = arguments;
			return LiteralUtils.canInitSafely(this.arguments);
		} else {
			this.argumentExpressions = new Expression[0];
		}
		return true;
	}

	@Override
	protected Object[] get(Event event, Executable<Event, Object>[] source) {
		for (Executable<Event, Object> task : source) {
			// something which decides for itself what its arguments mean, such as a function with
			// parameters, is handed the argument expressions rather than a flat list of their values
			Executable.BoundExecutable<Event, Object> boundExecutable = task.bind(argumentExpressions);
			if (boundExecutable != null) {
				Object result = Executable.run(boundExecutable, event, this::error);
				if (result == null)
					return new Object[0];
				if (result instanceof Object[] results)
					return results;
				return new Object[]{result};
			}

			Object[] arguments;
			if (hasArguments) {
				arguments = this.arguments.getArray(event);
			} else {
				arguments = new Object[0];
			}
			Object execute = task.execute(event, arguments);
			if (execute instanceof Object[] results)
				return results;
			return new Object[] {execute};
		}
		return new Object[0];
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return null;
	}

	@Override
	public Class<Object> getReturnType() {
		return Object.class;
	}

	@Override
	public boolean isSingle() {
		return !isPlural;
	}

	@Override
	public String toString(@Nullable Event event, final boolean debug) {
		String text = "the result" + (isPlural ? "s" : "") + " of " + getExpr().toString(event, debug);
		if (hasArguments)
			text += " with arguments " + arguments.toString(event, debug);
		return text;
	}

}
