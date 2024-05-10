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
import ch.njol.skript.lang.parser.ParserInstance;
import org.jetbrains.annotations.ApiStatus;
import org.skriptlang.skript.lang.script.Script;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.effects.EffReturn;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.variables.Variables;

public class ScriptFunction<T> extends Function<T> {
	
	private final Trigger trigger;
	
	public ScriptFunction(Signature<T> sign, Script script, SectionNode node) {
		super(sign);

		ParserInstance parser = ParserInstance.get();
		Functions.currentFunction = this;
		try {
			trigger = new Trigger(
				script,
				"function " + sign.getName(),
				new SimpleEvent(),
				trigger -> {
					parser.pushReturnData(trigger, getReturnType(), isSingle());
					return ScriptLoader.loadItems(node);
				}
			);
		} finally {
			Functions.currentFunction = null;
			parser.popReturnData();
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
		return returnType != null ? trigger.getReturnValues(returnType.getC()) : null;
	}

	/**
	 * Should only be called by {@link EffReturn}.
	 * @deprecated Use {@link ch.njol.skript.lang.TriggerSection#setReturnValues(Object[])}
	 */
	@Deprecated
	@ApiStatus.Internal
	public final void setReturnValue(final @Nullable T[] value) {
		trigger.setReturnValues(value);
	}

	@Override
	public boolean resetReturnValue() {
		trigger.resetReturnValues();
		return true;
	}

}
