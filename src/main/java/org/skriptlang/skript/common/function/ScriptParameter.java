package org.skriptlang.skript.common.function;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import com.google.common.base.Preconditions;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * A parameter for a {@link DefaultFunction}.
 *
 * @param name         The name.
 * @param type         The type's class.
 * @param modifiers    The modifiers.
 * @param defaultValue The default value, or null if there is no default value.
 * @param <T>          The type.
 */
public record ScriptParameter<T>(String name, Class<T> type, Set<Modifier> modifiers,
								 @Nullable Expression<?> defaultValue)
		implements Parameter<T> {

	/**
	 * Parses a {@link ScriptParameter} from a script.
	 *
	 * @param name The name.
	 * @param type The class of the parameter.
	 * @param def  The default value, if present.
	 * @return A parsed parameter {@link ScriptParameter}, or null if parsing failed.
	 */
	public static Parameter<?> parse(@NotNull String name, @NotNull Class<?> type, @Nullable String def) {
		Preconditions.checkNotNull(name, "name cannot be null");
		Preconditions.checkNotNull(type, "type cannot be null");

		if (!Variable.isValidVariableName(name, true, false)) {
			Skript.error("Invalid parameter name: %s", name);
			return null;
		}

		Expression<?> defaultValue = null;
		if (def != null) {
			Class<?> target = Utils.getComponentType(type);

			// Parse the default value expression
			try (RetainingLogHandler log = SkriptLogger.startRetainingLog()) {
				defaultValue = new SkriptParser(def, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT).parseExpression(target);
				if (defaultValue == null || LiteralUtils.hasUnparsedLiteral(defaultValue)) {
					log.printErrors("Can't understand this expression: " + def);
					log.stop();
					return null;
				}
				log.printLog();
				log.stop();
			}
		}

		Set<Modifier> modifiers = new HashSet<>();
		if (defaultValue != null) {
			modifiers.add(Modifier.OPTIONAL);
		}
		if (type.isArray()) {
			modifiers.add(Modifier.KEYED);
		}

		return new ScriptParameter<>(name, type, defaultValue, modifiers.toArray(new Modifier[0]));
	}

	public ScriptParameter(String name, Class<T> type, Modifier... modifiers) {
		this(name, type, Set.of(modifiers), null);
	}

	public ScriptParameter(String name, Class<T> type, Expression<?> defaultValue, Modifier... modifiers) {
		this(name, type, Set.of(modifiers), defaultValue);
	}

	/**
	 * Evaluates the argument, using default values if no argument is passed.
	 *
	 * @param argument The argument.
	 * @param event The event.
	 * @return The evaluated result.
	 * @see Parameter#evaluate(Expression, Event)
	 */
	public Object[] evaluate(@Nullable Expression<? extends T> argument, Event event) {
		if (argument == null) {
			if (!hasModifier(Modifier.OPTIONAL)) {
				throw new IllegalStateException("This parameter is required, but no argument was provided");
			} else if (defaultValue == null) {
				throw new IllegalStateException("This parameter does not have a default value");
			}
			//noinspection unchecked
			return Parameter.super.evaluate((Expression<? extends T>) defaultValue, event);
		}

		return Parameter.super.evaluate(argument, event);
	}

	@Override
	public @NotNull String toString() {
		return toFormattedString();
	}

}
