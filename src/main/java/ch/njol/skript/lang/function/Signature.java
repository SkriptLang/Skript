/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.lang.function;

import ch.njol.skript.classes.ClassInfo;
import org.eclipse.jdt.annotation.Nullable;
import ch.njol.skript.util.Contract;

import java.util.Collection;
import java.util.Collections;
import java.util.WeakHashMap;

/**
 * Function signature: name, parameter types and a return type.
 */
public class Signature<T> {
	
	/**
	 * Name of the script that the function is inside.
	 */
	final String script;
	
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
	@Nullable
	final ClassInfo<T> returnType;
	
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
	@Nullable
	final String originClassPath;

	/**
	 * An overriding contract for this function (e.g. to base its return on its arguments).
	 */
	@Nullable
	final Contract contract;

	public Signature(String script,
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

	@Nullable
	public ClassInfo<T> getReturnType() {
		return returnType;
	}
	
	public boolean isSingle() {
		return single;
	}

	public String getOriginClassPath() {
		return originClassPath;
	}

	@Nullable
	public Contract getContract() {
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
			if (parameters[i].def == null)
				return i + 1;
		}
		return 0; // No-args function
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
}
