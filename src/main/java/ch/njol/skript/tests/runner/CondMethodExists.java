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
package ch.njol.skript.tests.runner;

import ch.njol.skript.conditions.base.PropertyCondition;
import org.apache.commons.lang.StringUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Name("Method Exists")
@Description("Checks if a method exists")
@Examples("if method \"org.bukkit.Bukkit#getPluginCommand(java.lang.String)")
@Since("INSERT VERSION")
public class CondMethodExists extends PropertyCondition<String> {

	private final static Pattern SIGNATURE_PATTERN = Pattern.compile("(?<class>.+)#(?<name>.+)\\((?<params>.*)\\)");

	static {
		Skript.registerCondition(CondMethodExists.class, "method[s] %strings% [dont:do(esn't|n't)] exist[s]");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<String> signatures;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		signatures = (Expression<String>) exprs[0];
		setExpr(signatures);
		setNegated(parseResult.hasTag("dont"));
		return true;
	}

	@Override
	public boolean check(String signature) {
		Matcher sigMatcher = SIGNATURE_PATTERN.matcher(signature);
		if (!sigMatcher.matches())
			return false;

		try {
			Class<?> clazz = Class.forName(sigMatcher.group("class"));
			List<Class<?>> parameters = new ArrayList<>();
			String rawParameters = sigMatcher.group("params");
			if (!StringUtils.isBlank(rawParameters)) {
				for (String parameter : rawParameters.split(",")) {
					parameters.add(parseClass(parameter.trim()));
				}
			}
			return Skript.methodExists(clazz, sigMatcher.group("name"), parameters.toArray(new Class[0]));
		} catch (ClassNotFoundException exception) {
			return false;
		}
	}

	@Override
	protected String getPropertyName() {
		return "method exists";
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "method " + signatures.toString(event, debug) + " exists";
	}

	private static Class<?> parseClass(String clazz) throws ClassNotFoundException {
		if (clazz.endsWith("[]")) {
			Class<?> baseClass = parseClass(clazz.substring(0, clazz.length() - 2));
			return Array.newInstance(baseClass, 0).getClass();
		}
		switch (clazz) {
			case "byte":
				return byte.class;
			case "short":
				return short.class;
			case "int":
				return int.class;
			case "long":
				return long.class;
			case "float":
				return float.class;
			case "double":
				return double.class;
			case "boolean":
				return boolean.class;
			case "char":
				return char.class;
			default:
				return Class.forName(clazz);
		}
	}

}
