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
package ch.njol.skript.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Lists;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.ExprTabCompletions;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.Utils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;

/**
 * Represents an argument of a command
 */
public class Argument<T> {

	private transient WeakHashMap<Event, T[]> current = new WeakHashMap<>();

	@Nullable
	private final String name;

	@Nullable
	private final Expression<? extends T> defaultExpression;
	private final ClassInfo<T> type;
	private final boolean optional;
	private final boolean single;
	private final int index;

	private Argument(@Nullable String name, @Nullable Expression<? extends T> defaultExpression, ClassInfo<T> type, boolean single, int index, boolean optional) {
		this.defaultExpression = defaultExpression;
		this.optional = optional;
		this.single = single;
		this.index = index;
		this.type = type;
		this.name = name;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <T> Argument<T> newInstance(@Nullable String name, ClassInfo<T> type, @Nullable String defaultExpressionInput, int index, boolean single, boolean forceOptional) {
		if (name != null && !Variable.isValidVariableName(name, false, false)) {
			Skript.error("An argument's name must be a valid variable name, and cannot be a list variable.");
			return null;
		}
		Expression<? extends T> defaultExpression = null;
		if (defaultExpressionInput != null) {
			RetainingLogHandler log = SkriptLogger.startRetainingLog();
			try {
				if (defaultExpressionInput.startsWith("%") && defaultExpressionInput.endsWith("%")) {
					defaultExpression = new SkriptParser("" + defaultExpressionInput.substring(1, defaultExpressionInput.length() - 1), SkriptParser.PARSE_EXPRESSIONS, ParseContext.COMMAND).parseExpression(type.getC());
					if (defaultExpression == null) {
						log.printErrors("Can't understand this expression: " + defaultExpressionInput + "");
						return null;
					}
				} else {
					if (type.getC() == String.class) {
						if (defaultExpressionInput.startsWith("\"") && defaultExpressionInput.endsWith("\""))
							defaultExpression = (Expression<? extends T>) VariableString.newInstance("" + defaultExpressionInput.substring(1, defaultExpressionInput.length() - 1));
						else
							defaultExpression = (Expression<? extends T>) new SimpleLiteral<>(defaultExpressionInput, false);
					} else {
						defaultExpression = new SkriptParser(defaultExpressionInput, SkriptParser.PARSE_LITERALS, ParseContext.DEFAULT).parseExpression(type.getC());
					}
					if (defaultExpression == null) {
						log.printErrors("Can't understand this expression: '" + defaultExpressionInput + "'");
						return null;
					}
				}
				log.printLog();
			} finally {
				log.stop();
			}
		}
		return new Argument<>(name, defaultExpression, type, single, index, defaultExpressionInput != null || forceOptional);
	}

	@Override
	public String toString() {
		Expression<? extends T> defaultExpression = this.defaultExpression;
		return "<" + (name != null ? name + ": " : "") + Utils.toEnglishPlural(type.getCodeName(), !single) + (defaultExpression == null ? "" : " = " + defaultExpression.toString()) + ">";
	}

	public boolean isOptional() {
		return optional;
	}

	public void setToDefault(ScriptCommandEvent event) {
		if (defaultExpression != null) {
			if (defaultExpression instanceof ExprTabCompletions) {
				set(event, CollectionUtils.array(defaultExpression.getArray(event)[0]));
			} else {
				set(event, defaultExpression.getArray(event));
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void set(ScriptCommandEvent event, Object[] objects) {
		if (!(type.getC().isAssignableFrom(objects.getClass().getComponentType())))
			throw new IllegalArgumentException();
		current.put(event, (T[]) objects);
		String name = this.name;
		if (name != null) {
			if (single) {
				if (objects.length > 0)
					Variables.setVariable(name, objects[0], event, true);
			} else {
				for (int i = 0; i < objects.length; i++)
					Variables.setVariable(name + "::" + (i + 1), objects[i], event, true);
			}
		}
	}

	/**
	 * @return possible tab completions if the default expression is {@link ExprTabCompletions}
	 */
	public List<String> getTabCompletions(Event event) {
		if (defaultExpression instanceof ExprTabCompletions && defaultExpression.getReturnType() == String.class)
			return Lists.newArrayList((String[]) defaultExpression.getArray(event));
		return Collections.emptyList();
	}

	@Nullable
	public T[] getCurrent(Event event) {
		return current.get(event);
	}

	public Class<T> getType() {
		return type.getC();
	}

	public boolean isSingle() {
		return single;
	}

	public int getIndex() {
		return index;
	}

}
