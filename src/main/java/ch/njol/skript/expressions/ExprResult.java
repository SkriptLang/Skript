package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.function.DynamicFunctionReference;
import ch.njol.skript.registrations.Feature;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.util.Executable;

@Name("Result (Experimental)")
@Description({
	"Runs something (like a function) and returns its result.",
	"If the thing is expected to return multiple values, use 'results' instead of 'result'."
})
@Examples({
	"set {_function} to the function named \"myFunction\"",
	"set {_result} to the result of {_function}",
	"set {_list::*} to the results of {_function}",
	"set {_result} to the result of {_function} with arguments 13 and true"
})
@Since("2.10")
@Keywords({"run", "result", "execute", "function", "reflection"})
public class ExprResult extends PropertyExpression<Executable<Event, Object>, Object> {

	static {
		Skript.registerExpression(ExprResult.class, Object.class, ExpressionType.COMBINED,
			"[the] result[plural:s] of [running|executing] %executable% [arguments:with arg[ument]s %-objects%]");
	}

	private Expression<?> arguments;
	private boolean hasArguments, isPlural;
	private DynamicFunctionReference.Input input;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed,
						ParseResult result) {
		if (!this.getParser().hasExperiment(Feature.SCRIPT_REFLECTION))
			return false;
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
			this.input = new DynamicFunctionReference.Input(arguments);
			return LiteralUtils.canInitSafely(this.arguments);
		} else {
			this.input = new DynamicFunctionReference.Input();
		}
		return true;
	}

	@Override
	protected Object[] get(Event event, Executable<Event, Object>[] source) {
		for (Executable<Event, Object> task : source) {
			Object[] arguments;
			//noinspection rawtypes
			if (task instanceof DynamicFunctionReference reference) {
				Expression<?> validated = reference.validate(input);
				if (validated == null)
					return new Object[0];
				arguments = validated.getArray(event);
			} else if (hasArguments) {
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
