package ch.njol.skript.lang.function;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.util.Contract;
import ch.njol.skript.util.Utils;
import ch.njol.util.StringUtils;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.function.Parameter.Modifier;

import java.util.*;

/**
 * Function signature: name, parameter types and a return type.
 */
public class Signature<T> {

	/**
	 * Name of the script that the function is inside.
	 */
	final @Nullable String script;

	/**
	 * Name of function this refers to.
	 */
	final String name; // Stored for hashCode

	/**
	 * Parameters taken by this function, in order.
	 */
	private final LinkedHashMap<String, org.skriptlang.skript.lang.function.Parameter<?>> parameters;

	/**
	 * Whether this function is only accessible in the script it was declared in
	 */
	final boolean local;

	/**
	 * Return type of this function. For functions that return nothing, this
	 * is null. void is never used as return type, because it is not registered
	 * to Skript's type system.
	 */
	final @Nullable ClassInfo<T> returnType;

	/**
	 * Whether this function returns a single value, or multiple ones.
	 * Unspecified and unused when {@link #returnType} is null.
	 */
	final boolean single;

	/**
	 * References (function calls) to function with this signature.
	 */
	final Collection<FunctionReference<?>> calls;

	/**
	 * The class path for the origin of this signature.
	 */
	final @Nullable String originClassPath;

	/**
	 * An overriding contract for this function (e.g. to base its return on its arguments).
	 */
	final @Nullable Contract contract;

	public Signature(String script,
					 String name,
					 Parameter<?>[] parameters, boolean local,
					 @Nullable ClassInfo<T> returnType,
					 boolean single,
					 @Nullable String originClassPath,
					 @Nullable Contract contract) {
		this(script, name, initParameters(parameters), local, returnType, single, originClassPath, contract);
	}

	private static LinkedHashMap<String, org.skriptlang.skript.lang.function.Parameter<?>> initParameters(Parameter<?>[] params) {
		LinkedHashMap<String, org.skriptlang.skript.lang.function.Parameter<?>> map = new LinkedHashMap<>();
		for (Parameter<?> parameter : params) {
			map.put(parameter.name(), parameter);
		}
		return map;
	}

	public Signature(String script,
					 String name,
					 LinkedHashMap<String, org.skriptlang.skript.lang.function.Parameter<?>> parameters, boolean local,
					 @Nullable ClassInfo<T> returnType,
					 boolean single,
					 @Nullable String originClassPath,
					 @Nullable Contract contract) {
		this.script = script;
		this.name = name;
		this.parameters = parameters;
		this.local = local;
		this.returnType = returnType;
		this.single = single;
		this.originClassPath = originClassPath;
		this.contract = contract;

		calls = Collections.newSetFromMap(new WeakHashMap<>());
	}

	public Signature(String script,
					 String name,
					 Parameter<?>[] parameters, boolean local,
					 @Nullable ClassInfo<T> returnType,
					 boolean single,
					 @Nullable String originClassPath) {
		this(script, name, parameters, local, returnType, single, originClassPath, null);
	}

	public Signature(String script, String name, Parameter<?>[] parameters, boolean local, @Nullable ClassInfo<T> returnType, boolean single) {
		this(script, name, parameters, local, returnType, single, null);
	}

	public String getName() {
		return name;
	}

	/**
	 * @deprecated Use {@link #getParameter(String)}} or {@link #parameters()} instead.
	 */
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	public org.skriptlang.skript.lang.function.Parameter<?> getParameter(int index) {
		return parameters.values().toArray(new org.skriptlang.skript.lang.function.Parameter<?>[0])[index];
	}

	/**
	 * @return A {@link SequencedCollection} containing all parameters.
	 */
	public @NotNull LinkedHashMap<String, org.skriptlang.skript.lang.function.Parameter<?>> parameters() {
		return new LinkedHashMap<>(parameters);
	}

	/**
	 * @param name The parameter name.
	 * @return The parameter with the specified name, or null if none is found.
	 */
	public org.skriptlang.skript.lang.function.Parameter<?> getParameter(@NotNull String name) {
		Preconditions.checkNotNull(name, "name cannot be null");

		return parameters.get(name);
	}

	/**
	 * @deprecated Use {@link #parameters()} instead.
	 */
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	public org.skriptlang.skript.lang.function.Parameter<?>[] getParameters() {
		return parameters.values().toArray(new org.skriptlang.skript.lang.function.Parameter<?>[0]);
	}

	public boolean isLocal() {
		return local;
	}

	public @Nullable ClassInfo<T> getReturnType() {
		return returnType;
	}

	public boolean isSingle() {
		return single;
	}

	public String getOriginClassPath() {
		return originClassPath;
	}

	public @Nullable Contract getContract() {
		return contract;
	}

	/**
	 * Gets maximum number of parameters that the function described by this
	 * signature is able to take.
	 *
	 * @return Maximum number of parameters.
	 */
	public int getMaxParameters() {
		return parameters.size();
	}

	/**
	 * Gets minimum number of parameters that the function described by this
	 * signature is able to take. Parameters that have default values and do
	 * not have any parameters that are mandatory after them, are optional.
	 *
	 * @return Minimum number of parameters required.
	 */
	public int getMinParameters() {
		List<org.skriptlang.skript.lang.function.Parameter<?>> params = new LinkedList<>(parameters.values());

		int i = parameters.size() - 1;
		for (org.skriptlang.skript.lang.function.Parameter<?> parameter : params.reversed()) {
			if (!parameter.modifiers().contains(Modifier.OPTIONAL)) {
				return i + 1;
			}
			i--;
		}

		return 0; // No-args function
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return toString(true, Skript.debug());
	}

	public String toString(boolean includeReturnType, boolean debug) {
		StringBuilder signatureBuilder = new StringBuilder();

		if (local)
			signatureBuilder.append("local ");
		signatureBuilder.append(name);

		signatureBuilder.append('(')
			.append(StringUtils.join(parameters.values(), ", "))
			.append(')');

		if (includeReturnType && returnType != null) {
			signatureBuilder.append(" :: ");
			signatureBuilder.append(Utils.toEnglishPlural(returnType.getCodeName(), !single));
		}

		return signatureBuilder.toString();
	}

}
