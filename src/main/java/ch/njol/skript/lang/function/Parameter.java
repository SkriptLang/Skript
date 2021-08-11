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

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import org.eclipse.jdt.annotation.Nullable;

public final class Parameter<T> {
	
	/**
	 * Name of this parameter. Will be used as name for the local variable
	 * that contains value of it inside function. This is always in lower case;
	 * variable names are case-insensitive.
	 */
	final String name;
	
	/**
	 * Type of the parameter.
	 */
	final ClassInfo<T> type;
	
	/**
	 * Expression that will provide default value of this parameter
	 * when the function is called.
	 */
	@Nullable
	final Expression<? extends T> def;
	
	/**
	 * Whether this parameter takes one or many values.
	 */
	final boolean single;
	
	@SuppressWarnings("null")
	public Parameter(String name, ClassInfo<T> type, boolean single, @Nullable Expression<? extends T> def) {
		this.name = name != null ? name.toLowerCase() : null;
		this.type = type;
		this.def = def;
		this.single = single;
	}
	
	/**
	 * Get the Type of this parameter.
	 * @return Type of the parameter
	 */
	public ClassInfo<T> getType() {
		return type;
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public static <T> Parameter<T> newInstance(String name, ClassInfo<T> type, boolean single, @Nullable String def) {
		if (!Variable.isValidVariableName(name, true, false)) {
			Skript.error("A parameter's name must be a valid variable name.");
			// ... because it will be made available as local variable
			return null;
		}
		Expression<? extends T> d = null;
		if (def != null) {
			RetainingLogHandler log = SkriptLogger.startRetainingLog();
			
			// Parse the default value expression
			try {
				d = new SkriptParser(def, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT).parseExpression(type.getC());
				if (d == null || LiteralUtils.hasUnparsedLiteral(d)) {
					log.printErrors("Can't understand this expression: " + def);
					return null;
				}
				log.printLog();
			} finally {
				log.stop();
			}
		}
		return new Parameter<>(name, type, single, d);
	}
	
	/**
	 * Get the name of this parameter.
	 * <p>Will be used as name for the local variable that contains value of it inside function.</p>
	 * @return Name of this parameter
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Get the Expression that will be used to provide the default value of this parameter when the function is called.
	 * @return Expression that will provide default value of this parameter
	 */
	@Nullable
	public Expression<? extends T> getDefaultExpression() {
		return def;
	}
	
	/**
	 * Get whether this parameter takes one or many values.
	 * @return True if this parameter takes one value, false otherwise
	 */
	public boolean isSingleValue() {
		return single;
	}
	
	@Override
	public String toString() {
		return name + ": " + Utils.toEnglishPlural(type.getCodeName(), !single) + (def != null ? " = " + def.toString(null, true) : "");
	}
	
}
