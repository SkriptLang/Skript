package ch.njol.skript.lang.function;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Documentable;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Contract;
import ch.njol.skript.util.Utils;
import ch.njol.util.StringUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.common.function.FunctionReference;
import org.skriptlang.skript.common.function.Parameter.Modifier;

import java.util.*;

/**
 * Function signature: name, parameter types and a return type.
 */
public class Signature<T> implements org.skriptlang.skript.common.function.Signature<T>, Documentable {

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
	private final SequencedMap<String, org.skriptlang.skript.common.function.Parameter<?>> parameters;

	/**
	 * Whether this function is only accessible in the script it was declared in
	 */
	final boolean local;

	/**
	 * The return type.
	 */
	final @Nullable ClassInfo<T> returnType;
	final Class<?> returns;

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
	 * An overriding contract for this function (e.g. to base its return on its arguments).
	 */
	final @Nullable Contract contract;

	public Signature(@Nullable String script, String name, Parameter<?>[] parameters, boolean local, @Nullable ClassInfo<T> returnType, boolean single, @Nullable Contract contract) {
		this.script = script;
		this.name = name;
		this.parameters = initParameters(parameters);
		this.local = local;
		this.returnType = returnType;
		if (returnType == null) {
			this.returns = null;
		} else {
			if (single) {
				this.returns = returnType.getC();
			} else {
				this.returns = returnType.getC().arrayType();
			}
		}
		this.single = single;
		this.contract = contract;
		this.calls = Collections.newSetFromMap(new WeakHashMap<>());
	}

	public Signature(@Nullable String script, String name, Parameter<?>[] parameters, boolean local, @Nullable ClassInfo<T> returnType, boolean single, String stacktrace) {
		this(script, name, parameters, local, returnType, single, (Contract) null);
	}

	public Signature(String script, String name, Parameter<?>[] parameters, boolean local, ClassInfo<T> returnType, boolean single, String stacktrace, @Nullable Contract contract) {
		this(script, name, parameters, local, returnType, single, contract);
	}

	public Signature(@Nullable String script, String name, SequencedMap<String, org.skriptlang.skript.common.function.Parameter<?>> parameters, Class<T> returnType, boolean local) {
		this.script = script;
		this.name = name;
		this.parameters = parameters;
		this.local = local;
		this.returns = returnType;
		this.single = single();
		if (returnType != null && returnType.isArray()) {
			//noinspection unchecked
			this.returnType = (ClassInfo<T>) Classes.getExactClassInfo(returnType.componentType());
		} else {
			this.returnType = Classes.getExactClassInfo(returnType);
		}
		this.contract = null;
		this.calls = Collections.newSetFromMap(new WeakHashMap<>());
	}

	public Signature(String namespace, String name, org.skriptlang.skript.common.function.Parameter<?>[] parameters, Class<T> returnType, boolean single, @Nullable Contract contract) {
		this(namespace, name, initParameters(parameters), returnType, false);
	}

	private static SequencedMap<String, org.skriptlang.skript.common.function.Parameter<?>> initParameters(org.skriptlang.skript.common.function.Parameter<?>[] params) {
		SequencedMap<String, org.skriptlang.skript.common.function.Parameter<?>> map = new LinkedHashMap<>();
		for (org.skriptlang.skript.common.function.Parameter<?> parameter : params) {
			map.put(parameter.name(), parameter);
		}
		return map;
	}

	/**
	 * Converts a {@link org.skriptlang.skript.common.function.Parameter} to a {@link Parameter}.
	 * @param parameter The parameter to use to convert.
	 * @return The converted parameter.
	 */
	private static Parameter<?> toParameter(org.skriptlang.skript.common.function.Parameter<?> parameter) {
		if (parameter == null) {
			return null;
		}

		ClassInfo<?> classInfo;
		if (parameter.type().isArray()) {
			classInfo = Classes.getExactClassInfo(parameter.type().componentType());
		} else {
			classInfo = Classes.getExactClassInfo(parameter.type());
		}

		return new Parameter<>(parameter.name(), classInfo, !parameter.type().isArray(), null, parameter.modifiers().toArray(new Modifier[0]));
	}

	/**
	 * @deprecated Use {@link #getParameter(String)} or {@link #parameters()} instead.
	 */
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	public Parameter<?> getParameter(int index) {
		return parameters.values()
				.stream().map(Signature::toParameter)
				.toList().get(index);
	}

	/**
	 * @deprecated Use {@link #parameters()} instead.
	 */
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	public Parameter<?>[] getParameters() {
		return parameters.values().stream().map(Signature::toParameter)
				.toList()
				.toArray(new Parameter[0]);
	}

	@Override
	public Class<T> returnType() {
		if (returns == null) {
			return null;
		}
		//noinspection unchecked
		return (Class<T>) returns;
	}

	/**
	 * @return A {@link SequencedMap} containing all parameters.
	 */
	@Override
	public @NotNull SequencedMap<String, org.skriptlang.skript.common.function.Parameter<?>> parameters() {
		return Collections.unmodifiableSequencedMap(parameters);
	}

	@Override
	public Contract contract() {
		return contract;
	}

	@Override
	public void addCall(FunctionReference<?> reference) {
		calls.add(reference);
	}

	/**
	 * @param name The parameter name.
	 * @return The parameter with the specified name, or null if none is found.
	 */
	public org.skriptlang.skript.common.function.Parameter<?> getParameter(@NotNull String name) {
		Preconditions.checkNotNull(name, "name cannot be null");

		return parameters.get(name);
	}

	public String getName() {
		return name;
	}

	public boolean isLocal() {
		return local;
	}

	/**
	 * @return The namespace of this signature.
	 */
	public String namespace() {
		return script;
	}

	public @Nullable ClassInfo<T> getReturnType() {
		return returnType;
	}

	/**
	 * @return Whether this signature returns a single or multiple values.
	 */
	public boolean isSingle() {
		return single;
	}

	/**
	 * @deprecated Unused.
	 */
	@Deprecated(forRemoval = true, since = "2.13")
	public String getOriginClassPath() {
		return "";
	}

	public @Nullable Contract getContract() {
		return contract;
	}

	public Collection<FunctionReference<?>> calls() {
		return calls;
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
		List<org.skriptlang.skript.common.function.Parameter<?>> params = new LinkedList<>(parameters.values());

		int i = parameters.size() - 1;
		for (org.skriptlang.skript.common.function.Parameter<?> parameter : Lists.reverse(params)) {
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

		if (includeReturnType && returns != null) {
			signatureBuilder.append(" :: ");

			signatureBuilder.append(Utils.toEnglishPlural(returnType.getCodeName(), returns.isArray()));
		}

		return signatureBuilder.toString();
	}

	@Override
	public @NotNull String name() {
		return name;
	}

	@Override
	public @Unmodifiable @NotNull List<String> description() {
		return List.of();
	}

	@Override
	public @Unmodifiable @NotNull List<String> since() {
		return List.of();
	}

	@Override
	public @Unmodifiable @NotNull List<String> examples() {
		return List.of();
	}

	@Override
	public @Unmodifiable @NotNull List<String> keywords() {
		return List.of();
	}

	@Override
	public @Unmodifiable @NotNull List<String> requires() {
		return List.of();
	}

}
