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
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Parameter<T> {

	public final static Pattern PARAM_PATTERN = Pattern.compile("^\\s*([^:(){}\",]+?)\\s*:\\s*([a-zA-Z ]+?)\\s*(?:\\s*=\\s*(.+))?\\s*$");

	/**
	 * Name of this parameter. Will be used as name for the local variable
	 * that contains value of it inside function.
	 * If {@link SkriptConfig#caseInsensitiveVariables} is {@code true},
	 * then the valid variable names may not necessarily match this string in casing.
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
	final @Nullable Expression<? extends T> def;
	
	/**
	 * Whether this parameter takes one or many values.
	 */
	final boolean single;
	
	@SuppressWarnings("null")
	public Parameter(String name, ClassInfo<T> type, boolean single, @Nullable Expression<? extends T> def) {
		this.name = name;
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

	public static <T> @Nullable Parameter<T> newInstance(String name, ClassInfo<T> type, boolean single, @Nullable String def) {
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
				//noinspection unchecked
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
	 * Parses function parameters from a string. The string should look something like this:
	 * <pre>"something: string, something else: number = 12"</pre>
	 * @param args The string to parse.
	 * @return The parsed parameters
	 */
	public static @Nullable List<Parameter<?>> parse(String args) {
		List<Parameter<?>> params = new ArrayList<>();
		boolean caseInsensitive = SkriptConfig.caseInsensitiveVariables.value();
		int j = 0;
		for (int i = 0; i <= args.length(); i = SkriptParser.next(args, i, ParseContext.DEFAULT)) {
			if (i == -1) {
				Skript.error("Invalid text/variables/parentheses in the arguments of this function");
				return null;
			}
			if (i == args.length() || args.charAt(i) == ',') {
				String arg = args.substring(j, i);

				if (args.isEmpty()) // Zero-argument function
					break;

				// One or more arguments for this function
				Matcher n = PARAM_PATTERN.matcher(arg);
				if (!n.matches()) {
					Skript.error("The " + StringUtils.fancyOrderNumber(params.size() + 1) + " argument's definition is invalid. It should look like 'name: type' or 'name: type = default value'.");
					return null;
				}
				String paramName = "" + n.group(1);
				// for comparing without affecting the original name, in case the config option for case insensitivity changes.
				String lowerParamName = paramName.toLowerCase(Locale.ENGLISH);
				for (Parameter<?> p : params) {
					// only force lowercase if we don't care about case in variables
					String otherName = caseInsensitive ? p.name.toLowerCase(Locale.ENGLISH) : p.name;
					if (otherName.equals(caseInsensitive ? lowerParamName : paramName)) {
						Skript.error("Each argument's name must be unique, but the name '" + paramName + "' occurs at least twice.");
						return null;
					}
				}
				ClassInfo<?> c;
				c = Classes.getClassInfoFromUserInput("" + n.group(2));
				NonNullPair<String, Boolean> pl = Utils.getEnglishPlural("" + n.group(2));
				if (c == null)
					c = Classes.getClassInfoFromUserInput(pl.getFirst());
				if (c == null) {
					Skript.error("Cannot recognise the type '" + n.group(2) + "'");
					return null;
				}
				String rParamName = paramName.endsWith("*") ? paramName.substring(0, paramName.length() - 3) +
					(!pl.getSecond() ? "::1" : "") : paramName;
				Parameter<?> p = Parameter.newInstance(rParamName, c, !pl.getSecond(), n.group(3));
				if (p == null)
					return null;
				params.add(p);

				j = i + 1;
			}
			if (i == args.length())
				break;
		}
		return params;
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
	public @Nullable Expression<? extends T> getDefaultExpression() {
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
