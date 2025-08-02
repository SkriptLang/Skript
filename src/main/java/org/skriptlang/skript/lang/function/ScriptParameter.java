package org.skriptlang.skript.lang.function;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.LiteralUtils;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * A parameter for a {@link DefaultFunction}.
 *
 * @param name The name.
 * @param type The type's class.
 * @param modifiers The modifiers.
 * @param defaultValue The default value, or null if there is no default value.
 * @param <T> The type.
 */
public record ScriptParameter<T>(String name, Class<T> type, Set<Modifier> modifiers, Expression<?> defaultValue)
	implements Parameter<T> {

	public ScriptParameter(String name, Class<T> type, Modifier... modifiers) {
		this(name, type, Set.of(modifiers), null);
	}

	public ScriptParameter(String name, Class<T> type, Expression<?> defaultValue, Modifier... modifiers) {
		this(name, type, Set.of(modifiers), defaultValue);
	}

	/**
	 * Parses a {@link ScriptParameter} from a script.
	 *
	 * @param name The name.
	 * @param type The class of the parameter.
	 * @param def The default value, if present.
	 * @return A parsed parameter {@link ScriptParameter}, or null if parsing failed.
	 */
	public static Parameter<?> parse(@NotNull String name, @NotNull Class<?> type, @Nullable String def) {
		Preconditions.checkNotNull(name, "name cannot be null");
		Preconditions.checkNotNull(type, "type cannot be null");

		if (!Variable.isValidVariableName(name, true, false)) {
			Skript.error("A parameter's name must be a valid variable name.");
			return null;
		}

		Expression<?> d = null;
		if (def != null) {
			Class<?> target;
			if (type.isArray()) {
                target = type.componentType();
			} else {
				target = type;
			}

			// Parse the default value expression
			try (RetainingLogHandler log = SkriptLogger.startRetainingLog()) {
				//noinspection unchecked
				d = new SkriptParser(def, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT).parseExpression(target);
				if (d == null || LiteralUtils.hasUnparsedLiteral(d)) {
					log.printErrors("Can't understand this expression: " + def);
					log.stop();
					return null;
				}
				log.printLog();
				log.stop();
			}
		}

		Set<Modifier> modifiers = new HashSet<>();
		if (d != null) {
			modifiers.add(Modifier.OPTIONAL);
		}
		if (type.isArray()) {
			modifiers.add(Modifier.KEYED);
		}

		return new ScriptParameter<>(name, type, d, modifiers.toArray(new Modifier[0]));
	}

}
