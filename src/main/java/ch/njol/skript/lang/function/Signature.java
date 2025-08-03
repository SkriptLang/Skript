package ch.njol.skript.lang.function;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Contract;
import ch.njol.skript.util.Utils;
import ch.njol.util.StringUtils;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.function.FunctionReference;
import org.skriptlang.skript.lang.function.Parameter.Modifier;

import java.util.*;

/**
 * Function signature: name, parameter types and a return type.
 */
public class Signature<T> {

	/**
	 * Name of the script that the function is inside.
	 */
	private final @Nullable String namespace;

	/**
	 * Name of function this refers to.
	 */
	private final String name; // Stored for hashCode

	/**
	 * Parameters taken by this function, in order.
	 */
	private final LinkedHashMap<String, org.skriptlang.skript.lang.function.Parameter<?>> parameters;

	/**
	 * Whether this function is only accessible in the script it was declared in
	 */
	private final boolean local;

	/**
	 * The return type.
	 */
	private final Class<T> returnType;

	/**
	 * References (function calls) to function with this signature.
	 */
	private final Collection<FunctionReference<?>> calls;

	/**
	 * An overriding contract for this function (e.g. to base its return on its arguments).
	 */
	private final @Nullable Contract contract;

	public Signature(@Nullable String namespace,
					 @NotNull String name,
					 @NotNull LinkedHashMap<String, org.skriptlang.skript.lang.function.Parameter<?>> parameters,
					 boolean local,
					 @NotNull Class<T> returnType,
					 @Nullable Contract contract) {
		Preconditions.checkNotNull(name, "name cannot be null");
		Preconditions.checkNotNull(parameters, "parameters cannot be null");
		Preconditions.checkNotNull(returnType, "returnType cannot be null");

		this.namespace = namespace;
		this.name = name;
		this.parameters = parameters;
		this.local = local;
		this.returnType = returnType;
		this.contract = contract;

		calls = Collections.newSetFromMap(new WeakHashMap<>());
	}

	/**
	 * @deprecated Use {@link #Signature(String, String, LinkedHashMap, boolean, Class, Contract)} instead.
	 */
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	public Signature(String namespace,
					 String name,
					 Parameter<?>[] parameters, boolean local,
					 @Nullable ClassInfo<T> returnType,
					 boolean single,
					 @Nullable String originClassPath,
					 @Nullable Contract contract) {
		this(namespace, name, initParameters(parameters), local, initReturnType(returnType, single), contract);
	}

	private static <T> Class<T> initReturnType(ClassInfo<T> classInfo, boolean single) {
		if (classInfo == null) {
			//noinspection unchecked
			return (Class<T>) Void.class;
		}

		if (single) {
			return classInfo.getC();
		} else {
			//noinspection unchecked
			return (Class<T>) classInfo.getC().arrayType();
		}
	}

	private static LinkedHashMap<String, org.skriptlang.skript.lang.function.Parameter<?>> initParameters(Parameter<?>[] params) {
		LinkedHashMap<String, org.skriptlang.skript.lang.function.Parameter<?>> map = new LinkedHashMap<>();
		for (Parameter<?> parameter : params) {
			map.put(parameter.name(), parameter);
		}
		return map;
	}

	/**
	 * @deprecated Use {@link #Signature(String, String, LinkedHashMap, boolean, Class, Contract)} instead.
	 */
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	public Signature(String namespace,
					 String name,
					 Parameter<?>[] parameters, boolean local,
					 @Nullable ClassInfo<T> returnType,
					 boolean single,
					 @Nullable String originClassPath) {
		this(namespace, name, parameters, local, returnType, single, originClassPath, null);
	}

	/**
	 * @deprecated Use {@link #Signature(String, String, LinkedHashMap, boolean, Class, Contract)} instead.
	 */
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	public Signature(String namespace, String name, Parameter<?>[] parameters, boolean local, @Nullable ClassInfo<T> returnType, boolean single) {
		this(namespace, name, parameters, local, returnType, single, null);
	}

	/**
	 * @deprecated Use {@link #getParameter(String)} or {@link #parameters()} instead.
	 */
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	public org.skriptlang.skript.lang.function.Parameter<?> getParameter(int index) {
		return parameters.values().toArray(new org.skriptlang.skript.lang.function.Parameter<?>[0])[index];
	}

	/**
	 * @deprecated Use {@link #parameters()} instead.
	 */
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	public org.skriptlang.skript.lang.function.Parameter<?>[] getParameters() {
		return parameters.values().toArray(new org.skriptlang.skript.lang.function.Parameter<?>[0]);
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

	public String getName() {
		return name;
	}

	public boolean isLocal() {
		return local;
	}

	/**
	 * @return The namespace of this signature.
	 */
	String namespace() {
		return namespace;
	}

	/**
	 * @deprecated Use {@link #returnType()} instead.
	 */
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	public @Nullable ClassInfo<T> getReturnType() {
		if (returnType.isArray()) {
			//noinspection unchecked
			return (ClassInfo<T>) Classes.getExactClassInfo(returnType.componentType());
		} else {
			return Classes.getExactClassInfo(returnType);
		}
	}

	/**
	 * @return The return type of this signature. Returns {@code Void.class} for no return type.
	 */
	public @NotNull Class<T> returnType() {
		return returnType;
	}

	public boolean isSingle() {
		return !returnType.isArray();
	}

	/**
	 * @deprecated Unused.
	 */
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
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

			ClassInfo<T> ci;
			if (returnType.isArray()) {
				//noinspection unchecked
				ci = (ClassInfo<T>) Classes.getExactClassInfo(returnType.componentType());
			} else {
				ci = Classes.getExactClassInfo(returnType);
			}

			signatureBuilder.append(Utils.toEnglishPlural(ci.getCodeName(), returnType.isArray()));
		}

		return signatureBuilder.toString();
	}

}
