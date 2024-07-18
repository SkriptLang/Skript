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
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ReturnHandler;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.skriptlang.skript.lang.script.Script;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.variables.Variables;

public class ScriptFunction<T> extends Function<T> implements ReturnHandler<T> {

	private final Trigger trigger;

	private boolean returnValueSet;
	private T @Nullable [] returnValues;

	/**
	 * @deprecated use {@link ScriptFunction#ScriptFunction(Signature, SectionNode)}
	 */
	@Deprecated
	public ScriptFunction(Signature<T> sign, Script script, SectionNode node) {
		this(sign, node);
	}
	
	public ScriptFunction(Signature<T> sign, SectionNode node) {
		super(sign);

		Functions.currentFunction = this;
		try {
			trigger = loadReturnableTrigger(node, "function " + sign.getName(), new SimpleEvent());
		} finally {
			Functions.currentFunction = null;
		}
		trigger.setLineNumber(node.getLine());
	}

	// REMIND track possible types of local variables (including undefined variables) (consider functions, commands, and EffChange) - maybe make a general interface for this purpose
	// REM: use patterns, e.g. {_a%b%} is like "a.*", and thus subsequent {_axyz} may be set and of that type.
	@Override
	public T @Nullable [] execute(final FunctionEvent<?> e, final Object[][] params) {
		Parameter<?>[] parameters = getSignature().getParameters();
		for (int i = 0; i < parameters.length; i++) {
			Parameter<?> p = parameters[i];
			Object[] val = params[i];
			if (p.single && val.length > 0) {
				Variables.setVariable(p.name, val[0], e, true);
			} else {
				for (int j = 0; j < val.length; j++) {
					Variables.setVariable(p.name + "::" + (j + 1), val[j], e, true);
				}
			}
		}
		
		trigger.execute(e);
		ClassInfo<T> returnType = getReturnType();
		return returnType != null ? returnValues : null;
	}

	/**
	 * @deprecated Use {@link ScriptFunction#returnValues(Event, Expression)}
	 */
	@Deprecated
	@ApiStatus.Internal
	public final void setReturnValue(@Nullable T[] values) {
		assert !returnValueSet;
		returnValueSet = true;
		this.returnValues = values;
	}

	@Override
	public boolean resetReturnValue() {
		returnValueSet = false;
		returnValues = null;
		return true;
	}

	@Override
	public final void returnValues(Event event, Expression<? extends T> value) {
		assert !returnValueSet;
		returnValueSet = true;
		this.returnValues = value.getArray(event);
	}

	@Override
	public final boolean isSingleReturnValue() {
		return isSingle();
	}

	@Override
	public final @Nullable Class<? extends T> returnValueType() {
		return getReturnType() != null ? getReturnType().getC() : null;
	}

}
