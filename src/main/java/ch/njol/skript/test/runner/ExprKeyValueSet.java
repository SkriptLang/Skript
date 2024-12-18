package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;

@NoDoc
public class ExprKeyValueSet extends SimpleExpression<Object> implements KeyProviderExpression<Object> {

	static {
		if (TestMode.ENABLED)
			Skript.registerExpression(ExprKeyValueSet.class, Object.class, ExpressionType.SIMPLE,
				"test key values of %~objects%",
				"test key values"
			);
	}

	private static final Map<String, String> testSet = Map.of("hello", "there", "foo", "bar", "a", "b");

	private Variable<?> variable;
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (matchedPattern == 0) {
			Expression<?>  expression = expressions[0];
			if (!(expression instanceof Variable<?> variable) || !variable.isList()) {
				Skript.error("The expression '" + expression + "' is not a list variable.");
				return false;
			}
			this.variable = variable;
		}
		return true;
	}

	@Override
	public @NotNull String @NotNull [] getArrayKeys(Event event) throws IllegalStateException {
		if (variable == null)
			return testSet.keySet().toArray(new String[0]);
		return variable.getArrayKeys(event);
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		if (variable == null)
			return testSet.values().toArray();
		return variable.getArray(event);
	}

	@Override
	public Class<?> getReturnType() {
		return Object.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (variable != null)
			return "test key values of " + variable.toString(event, debug);
		return "test key values";
	}

}
