package ch.njol.skript.lang.function;

import ch.njol.skript.classes.ClassInfo;
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
import org.skriptlang.skript.common.function.Parameters;
import org.skriptlang.skript.common.function.Signature.Modifier.Returns;

import java.util.*;

/**
 * Function signature: name, parameter types and a return type.
 */
public class Signature<T> implements org.skriptlang.skript.common.function.Signature<T> {

	private final String name;
	private final Parameters parameters;
	private final Set<Modifier> modifiers = new HashSet<>();

	final String namespace;

	/**
	 * The return type.
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
	 * An overriding contract for this function (e.g. to base its return on its arguments).
	 */
	final @Nullable Contract contract;

	/**
	 * @deprecated Use {@link Signature#Signature(String, Set, SequencedMap, Contract)} instead.
	 */
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
	public Signature(@Nullable String namespace, String name, Parameter<?>[] parameters, boolean local, @Nullable ClassInfo<T> returnType, boolean single, @Nullable Contract contract) {
		this.name = name;
		this.parameters = initParameters(parameters);
		this.namespace = namespace;
		if (local && namespace != null)
			modifiers.add(new Modifier.Local(namespace));
		if (returnType != null)
			modifiers.add(new Modifier.Returns<>(returnType.getC()));
		this.returnType = returnType;
		this.single = single;
		this.contract = contract;
		this.calls = Collections.newSetFromMap(new WeakHashMap<>());
	}

	/**
	 * @deprecated Use {@link Signature#Signature(String, Set, SequencedMap, Contract)} instead.
	 */
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
	public Signature(@Nullable String namespace, String name, Parameter<?>[] parameters, boolean local, @Nullable ClassInfo<T> returnType, boolean single, String stacktrace) {
		this(namespace, name, parameters, local, returnType, single, (Contract) null);
	}

	/**
	 * @deprecated Use {@link Signature#Signature(String, Set, SequencedMap, Contract)} instead.
	 */
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
	public Signature(String namespace, String name, Parameter<?>[] parameters, boolean local, ClassInfo<T> returnType, boolean single, String stacktrace, @Nullable Contract contract) {
		this(namespace, name, parameters, local, returnType, single, contract);
	}

	public Signature(@Nullable String namespace, String name, Parameters parameters, Class<T> returnType, boolean local, @Nullable Contract contract) {
		this.name = name;
		this.parameters = parameters;
		this.namespace = namespace;
		if (local && namespace != null)
			modifiers.add(new Modifier.Local(namespace));
		if (returnType != null) {
			//noinspection unchecked
			this.returnType = (ClassInfo<T>) Classes.getExactClassInfo(Utils.getComponentType(returnType));
			this.single = !returnType.isArray();
			modifiers.add(new Modifier.Returns<>(returnType));
		} else {
			this.returnType = null;
			this.single = true;
		}
		this.contract = contract;
		this.calls = Collections.newSetFromMap(new WeakHashMap<>());
	}

	public Signature(@Nullable String namespace, String name, Parameters parameters, Class<T> returnType, boolean local) {
		this(namespace, name, parameters, returnType, local, null);
	}

	public Signature(String namespace, String name, org.skriptlang.skript.common.function.Parameter<?>[] parameters, Class<T> returnType, boolean single, @Nullable Contract contract) {
		this(namespace, name, initParameters(parameters), returnType, false, contract);
	}

	public Signature(String name, Set<Modifier> modifiers, Parameters parameters, @Nullable Contract contract) {
		this.modifiers.addAll(modifiers);

		this.name = name;
		this.parameters = parameters;

		if (hasModifier(Modifier.Returns.class)) {
			Class<?> type = getModifier(Returns.class).type();
			//noinspection unchecked
			this.returnType = (ClassInfo<T>) Classes.getExactClassInfo(Utils.getComponentType(type));
			this.single = !type.isArray();
		} else {
			this.returnType = null;
			this.single = true;
		}

		if (hasModifier(Modifier.Local.class)) {
			this.namespace = getModifier(Modifier.Local.class).namespace();
		} else {
			this.namespace = null;
		}
		this.contract = contract;
		this.calls = Collections.newSetFromMap(new WeakHashMap<>());
	}

	private static Parameters initParameters(org.skriptlang.skript.common.function.Parameter<?>[] params) {
		SequencedMap<String, org.skriptlang.skript.common.function.Parameter<?>> map = new LinkedHashMap<>();
		for (org.skriptlang.skript.common.function.Parameter<?> parameter : params) {
			map.put(parameter.name(), parameter);
		}
		return new Parameters(map);
	}

	/**
	 * Converts a {@link org.skriptlang.skript.common.function.Parameter} to a {@link Parameter}.
	 *
	 * @param parameter The parameter to use to convert.
	 * @return The converted parameter.
	 */
	static Parameter<?> toOldParameter(org.skriptlang.skript.common.function.Parameter<?> parameter) {
		if (parameter == null) {
			return null;
		}

		ClassInfo<?> classInfo = Classes.getExactClassInfo(Utils.getComponentType(parameter.type()));
		return new Parameter<>(parameter.name(), classInfo, !parameter.type().isArray(), null,
				parameter.modifiers().toArray(new org.skriptlang.skript.common.function.Parameter.Modifier[0]));
	}

	/**
	 * @deprecated Use {@link #getParameter(String)} or {@link #parameters()} instead.
	 */
	@Deprecated(forRemoval = true, since = "2.14")
	public Parameter<?> getParameter(int index) {
		return getParameters()[index];
	}

	/**
	 * @deprecated Use {@link #parameters()} instead.
	 */
	@Deprecated(forRemoval = true, since = "2.14")
	public Parameter<?>[] getParameters() {
		return Arrays.stream(parameters.all())
				.map(Signature::toOldParameter)
				.toArray(Parameter[]::new);
	}

	@Override
	public @NotNull String name() {
		return name;
	}

	@Override
	public @NotNull Parameters parameters() {
		return parameters;
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
		return hasModifier(Modifier.Local.class);
	}

	public String namespace() {
		return namespace;
	}

	public @Nullable ClassInfo<T> getReturnType() {
		return returnType;
	}

	/**
	 * @return Whether this signature returns a single or multiple values.
	 */
	@Override
	public boolean isSingle() {
		return single;
	}

	@Override
	public @Unmodifiable @NotNull Set<Modifier> modifiers() {
		return Collections.unmodifiableSet(modifiers);
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
		List<org.skriptlang.skript.common.function.Parameter<?>> params = new LinkedList<>(List.of(parameters.all()));

		int i = parameters.size() - 1;
		for (org.skriptlang.skript.common.function.Parameter<?> parameter : Lists.reverse(params)) {
			if (!parameter.hasModifier(org.skriptlang.skript.common.function.Parameter.Modifier.Optional.class)) {
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
		return toFormattedString();
	}

	public String toString(boolean includeReturnType, boolean debug) {
		StringBuilder signatureBuilder = new StringBuilder();

		if (isLocal())
			signatureBuilder.append("local ");

		signatureBuilder.append(name);

		signatureBuilder.append('(')
				.append(StringUtils.join(parameters.all(), ", "))
				.append(')');

		if (includeReturnType && hasModifier(Modifier.Returns.class)) {
			signatureBuilder.append(" returns ");

			signatureBuilder.append(Utils.toEnglishPlural(returnType.getCodeName(), getModifier(Modifier.Returns.class).type().isArray()));
		}

		return signatureBuilder.toString();
	}

}