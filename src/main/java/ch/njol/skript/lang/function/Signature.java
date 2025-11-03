package ch.njol.skript.lang.function;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.Contract;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.function.Parameter.Modifier;

import java.util.Collection;
import java.util.Collections;
import java.util.WeakHashMap;

/**
 * Function signature: name, parameter types and a return type.
 */
public class Signature<T> implements org.skriptlang.skript.common.function.Signature<T> {

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
	final Parameter<?>[] parameters;

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

	public Signature(@Nullable String script,
					 String name,
					 Parameter<?>[] parameters, boolean local,
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

	/**
	 * Creates a new signature.
	 *
	 * @param script The script of this signature.
	 * @param name The name of the function.
	 * @param parameters The parameters.
	 * @param returnType The return type class.
	 * @param contract A {@link Contract} that may belong to this signature.
	 */
	public Signature(@Nullable String script,
					 String name,
					 org.skriptlang.skript.common.function.Parameter<?>[] parameters,
					 @Nullable Class<T> returnType,
					 boolean single,
					 @Nullable Contract contract) {
		this.parameters = new Parameter[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			org.skriptlang.skript.common.function.Parameter<?> parameter = parameters[i];
			this.parameters[i] = new Parameter<>(parameter.name(),
				getClassInfo(parameter.type()), parameter.single(),
				null,
				parameter.modifiers().toArray(new Modifier[0]));
		}

		this.script = script;
		this.name = name;
		this.local = script != null;
		if (returnType != null) {
			//noinspection unchecked
			this.returnType = (ClassInfo<T>) getClassInfo(returnType);
		} else {
			this.returnType = null;
		}
		this.single = single;
		this.contract = contract;
		this.originClassPath = "";

		calls = Collections.newSetFromMap(new WeakHashMap<>());
	}

	/**
	 * Returns the {@link ClassInfo} of the non-array type of {@code cls}.
	 *
	 * @param cls The class.
	 * @param <T> The type of class.
	 * @return The non-array {@link ClassInfo} of {@code cls}.
	 */
	private static <T> ClassInfo<T> getClassInfo(Class<T> cls) {
		ClassInfo<T> classInfo;
		if (cls.isArray()) {
			//noinspection unchecked
			classInfo = (ClassInfo<T>) Classes.getExactClassInfo(cls.componentType());
		} else {
			classInfo = Classes.getExactClassInfo(cls);
		}
		return classInfo;
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

	@SuppressWarnings("null")
	public Parameter<?> getParameter(int index) {
		return parameters[index];
	}

	public Parameter<?>[] getParameters() {
		return parameters;
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

	/**
	 * @deprecated Unused and unsafe.
	 */
	@Deprecated(forRemoval = true, since = "2.13")
	public String getOriginClassPath() {
		return originClassPath;
	}

	public @Nullable Contract getContract() {
		return contract;
	}

	/**
	 * Gets maximum number of parameters that the function described by this
	 * signature is able to take.
	 * @return Maximum number of parameters.
	 */
	public int getMaxParameters() {
		return parameters.length;
	}

	/**
	 * Gets minimum number of parameters that the function described by this
	 * signature is able to take. Parameters that have default values and do
	 * not have any parameters that are mandatory after them, are optional.
	 * @return Minimum number of parameters required.
	 */
	public int getMinParameters() {
		for (int i = parameters.length - 1; i >= 0; i--) {
			if (!parameters[i].isOptional())
				return i + 1;
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

		signatureBuilder.append('(');
		int lastParameterIndex = parameters.length - 1;
		for (int i = 0; i < parameters.length; i++) {
			signatureBuilder.append(parameters[i].toString(debug));
			if (i != lastParameterIndex)
				signatureBuilder.append(", ");
		}
		signatureBuilder.append(')');

		if (includeReturnType && returnType != null) {
			signatureBuilder.append(" :: ");
			signatureBuilder.append(Utils.toEnglishPlural(returnType.getCodeName(), !single));
		}

		return signatureBuilder.toString();
	}

}
