package ch.njol.skript.lang.function;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.util.common.AnyNamed;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Contract;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.util.Executable;
import org.skriptlang.skript.util.Validated;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A partial reference to a Skript function.
 * This reference knows some of its information in advance (such as the function's name)
 * but will not be resolved until it receives inputs for the first time.
 * @param <Result> The return type of this function, if known.
 */
public class DynamicFunctionReference<Result>
	implements Contract, Executable<Event, Result[]>, Validated, AnyNamed {

	private final @NotNull String name;
	private final @Nullable Script source;
	private final Reference<Function<? extends Result>> function;
	private final @UnknownNullability Signature<? extends Result> signature;
	private final Validated validator = Validated.validator();
	private final Map<Input, Expression<?>> checkedInputs = new HashMap<>();
	private final boolean resolved;

	public DynamicFunctionReference(Function<? extends Result> function) {
		this.resolved = true;
		this.function = new WeakReference<>(function);
		this.name = function.getName();
		this.signature = function.getSignature();
		@Nullable File file = ScriptLoader.getScriptFromName(signature.script);
		this.source = file != null ? ScriptLoader.getScript(file) : null;
	}

	public DynamicFunctionReference(@NotNull String name) {
		this(name, null);
	}

	public DynamicFunctionReference(@NotNull String name, @Nullable Script source) {
		this.name = name;
		Function<? extends Result> function;
		if (source != null) {
			//noinspection unchecked
			function = (Function<? extends Result>) Functions.getFunction(name, source.getConfig().getFileName());
		} else {
			//noinspection unchecked
			function = (Function<? extends Result>) Functions.getFunction(name, null);
		}
		this.resolved = function != null;
		this.function = new WeakReference<>(function);
		if (resolved) {
			this.signature = function.getSignature();
			@Nullable File file = ScriptLoader.getScriptFromName(signature.script);
			this.source = file != null ? ScriptLoader.getScript(file) : null;
		} else {
			this.signature = null;
			this.source = null;
		}
	}

	public @Nullable Script source() {
		return source;
	}

	@Override
	public @NotNull String name() {
		return name;
	}

	@Override
	public boolean isSingle(Expression<?>... arguments) {
		if (!resolved)
			return true;
		return signature.contract != null
				? signature.contract.isSingle(arguments)
				: signature.isSingle();
	}

	@Override
	public @Nullable Class<?> getReturnType(Expression<?>... arguments) {
		if (!resolved)
			return Object.class;
		if (signature.contract != null)
			return signature.contract.getReturnType(arguments);
		Function<? extends Result> function = this.function.get();
		if (function != null && function.getReturnType() != null)
			return function.getReturnType().getC();
		return null;
	}

	@Override
	public Result @Nullable [] execute(Event event, Object... arguments) {
		if (!this.valid())
			return null;
		Function<? extends Result> function = this.function.get();
		if (function == null)
			return null;
		// We shouldn't trust the caller provided an array of arrays
		Object[][] consigned = FunctionReference.consign(arguments);
		try {
			return function.execute(consigned);
		} finally {
			function.resetReturnValue();
		}
	}

	@Override
	public void invalidate() {
		this.validator.invalidate();
	}

	@Override
	public boolean valid() {
		return resolved && validator.valid()
			&& function.get() != null // function was garbage-collected
			&& (source == null || source.valid());
		// if our source script has been reloaded our reference was invalidated
	}

	@Override
	public String toString() {
		if (source != null)
			return name + "() from " + Classes.toString(source);
		return name + "()";
	}

	/**
	 * Validates whether dynamic inputs are appropriate for the resolved function.
	 * If the inputs are acceptable, this will collect them into an expression list
	 * (the output of which can be passed directly to the task).
	 *
	 * @param parameters The input types to check
	 * @return A combined expression list, if these inputs are appropriate for the function
	 */
	public @Nullable Expression<?> validate(Expression<?>[] parameters) {
		Input input = new Input(parameters);
		return this.validate(input);
	}

	public @Nullable Expression<?> validate(Input input) {
		if (checkedInputs.containsKey(input))
			return checkedInputs.get(input);
		this.checkedInputs.put(input, null); // failure case
		if (signature == null)
			return null;
		boolean varArgs = signature.getMaxParameters() == 1 && !signature.getParameter(0).single;
		Expression<?>[] parameters = input.parameters();
		// Too many parameters
		if (parameters.length > signature.getMaxParameters() && !varArgs)
			return null;
		// Not enough parameters
		else if (parameters.length < signature.getMinParameters())
			return null;
		Expression<?>[] checked = new Expression[parameters.length];

		// Check parameter types
		for (int i = 0; i < parameters.length; i++) {
			Parameter<?> parameter = signature.parameters[varArgs ? 0 : i];
			//noinspection unchecked
			Expression<?> expression = parameters[i].getConvertedExpression(parameter.type.getC());
			if (expression == null) {
				return null;
			} else if (parameter.single && !expression.isSingle()) {
				return null;
			}
			checked[i] = expression;
		}

		// if successful, replace with our known result
		ExpressionList<?> result = new ExpressionList<>(checked, Object.class, true);
		this.checkedInputs.put(input, result);
		return result;
	}

	/**
	 * Attempts to parse a function reference from the format it would be stringified in.
	 * The name can include the source script name for the case of parsing local functions.
	 * @param name The function name, possibly including its script name
	 * @return A reference, if one is available
	 */
	public static @Nullable DynamicFunctionReference<?> parseFunction(String name) {
		// Function reference string-ifying appends a () and potentially its source,
		// e.g. `myFunction() from MyScript.sk` and we should turn that into a valid function.
		if (name.contains(") from ")) {
			// The user might be trying to resolve a local function by name only
			String source = name.substring(name.lastIndexOf(" from ") + 6).trim();
			Script script = getScript(source);
			return resolveFunction(name.substring(0, name.lastIndexOf(" from ")).trim(), script);
		}
		return resolveFunction(name, null);
	}

	/**
	 * Used to resolve a function from its name.
	 * @param name The function name
	 * @param script Potentially, the script it is from, if one is known
	 * @return A function reference, if one is available.
	 */
	public static @Nullable DynamicFunctionReference<?> resolveFunction(String name, @Nullable Script script) {
		if (name.contains("(") && name.contains(")"))
			name = name.replaceAll("\\(.*\\).*", "").trim();
		// In the future, if function overloading is supported, we could even use the header
		// to specify parameter types (e.g. "myFunction(text, player)"
		DynamicFunctionReference<Object> reference = new DynamicFunctionReference<>(name, script);
		if (!reference.valid())
			return null;
		return reference;
	}

	private static @Nullable Script getScript(@Nullable String source) {
		if (source == null || source.isEmpty())
			return null;
		@Nullable File file = ScriptLoader.getScriptFromName(source);
		if (file == null || file.isDirectory())
			return null;
		return ScriptLoader.getScript(file);
	}

	/**
	 * An index-linking key for a particular set of input expressions.
	 * Validation only needs to be done once for a set of parameter types,
	 * so this is used to prevent re-validation.
	 */
	public static class Input {
		private final Class<?>[] types;
		private transient final Expression<?>[] parameters;

		public Input(Expression<?>... types) {
			Class<?>[] classes = new Class<?>[types.length];
			for (int i = 0; i < types.length; i++) {
				classes[i] = types[i].getReturnType();
			}
			this.parameters = types;
			this.types = classes;
		}

		private Expression<?>[] parameters() {
			return parameters;
		}

		@Override
		public boolean equals(Object object) {
			if (this == object)
				return true;
			if (!(object instanceof Input input))
				return false;
			return Arrays.equals(parameters, input.parameters) && Objects.deepEquals(types, input.types);
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(types) ^ Arrays.hashCode(parameters);
		}

	}

}
