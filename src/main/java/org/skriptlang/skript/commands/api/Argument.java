package org.skriptlang.skript.commands.api;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.log.RetainingLogHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public class Argument<T> {

	private final @Nullable String name;
	private final Class<T> type;

	private final boolean optional;
	private final boolean single;

	private final @Nullable Expression<? extends T> defaultValue;

	// TODO in the future this map should be replaced and argument values
	// should be stored on a argument-value map on each execution's "TriggerContext"
	private final Map<ScriptCommandEvent, T[]> valueMap = new WeakHashMap<>();

	private Argument(
		@Nullable String name, Class<T> type,
		boolean optional, boolean single,
		@Nullable Expression<? extends T> defaultValue
	) {
		this.name = name;
		this.type = type;
		this.optional = optional;
		this.single = single;
		this.defaultValue = defaultValue;
	}

	@ApiStatus.Internal
	public static <T> @Nullable Argument<T> of(
		@Nullable String name, Class<T> type,
		boolean optional, boolean single,
		@Nullable String defaultExpression
	) {
		if (name != null && !Variable.isValidVariableName(name, false, false)) {
			Skript.error("An argument's name must be a valid variable name, and cannot be a list variable.");
			return null;
		}

		// parse the default expression (if it was provided)
		Expression<? extends T> parsedDefaultExpression = null;
		if (defaultExpression != null) {
			try (RetainingLogHandler log = new RetainingLogHandler()) {

				if (defaultExpression.startsWith("%") && defaultExpression.endsWith("%")) {
					// attempt to parse this as an expression

					//noinspection unchecked
					parsedDefaultExpression = new SkriptParser(
						defaultExpression.substring(1, defaultExpression.length() - 1),
						SkriptParser.PARSE_EXPRESSIONS,
						ParseContext.COMMAND
					).parseExpression(type);

				} else {
					// attempt to parse this as a literal

					if (type == String.class) { // this is a string literal
						if (defaultExpression.startsWith("\"") && defaultExpression.endsWith("\"")) {
							//noinspection unchecked
							parsedDefaultExpression =
								(Expression<? extends T>) VariableString.newInstance(defaultExpression.substring(1, defaultExpression.length() - 1));
						} else {
							//noinspection unchecked
							parsedDefaultExpression =
								(Expression<? extends T>) new SimpleLiteral<>(defaultExpression, false);
						}
					} else { // any other kind of literal
						// TODO is ParseContext.DEFAULT correct?
						//noinspection unchecked
						parsedDefaultExpression = new SkriptParser(
							defaultExpression, SkriptParser.PARSE_LITERALS, ParseContext.DEFAULT
						).parseExpression(type);
					}

				}

				if (parsedDefaultExpression == null) {
					log.printErrors("Can't understand this expression: '" + defaultExpression + "'");
					return null;
				}

				log.printLog();
			}
		}

		optional = defaultExpression != null || optional;
		return new Argument<>(name, type, optional, single, parsedDefaultExpression);
	}

	public @Nullable String getName() {
		return name;
	}

	public Class<T> getType() {
		return type;
	}

	public boolean isOptional() {
		return optional;
	}

	public boolean isSingle() {
		return single;
	}

	public void setValues(ScriptCommandEvent event, T[] values) {
		valueMap.put(event, values);
	}

	public T @Nullable [] getValues(ScriptCommandEvent event) {
		return valueMap.getOrDefault(event, defaultValue != null ? defaultValue.getArray(event) : null);
	}

}
