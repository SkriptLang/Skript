package ch.njol.skript.lang.function;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.*;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.common.function.DefaultFunction;
import org.skriptlang.skript.common.function.Parameter.Modifier.RangedModifier;
import org.skriptlang.skript.common.function.ScriptParameter;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @deprecated Use {@link ScriptParameter}
 * or {@link DefaultFunction.Builder#parameter(String, Class, Modifier...)} instead.
 */
@Deprecated(forRemoval = true, since = "2.14")
public final class Parameter<T> implements org.skriptlang.skript.common.function.Parameter<T> {

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

	private final Set<Modifier> modifiers;

	/**
	 * @deprecated Use {@link org.skriptlang.skript.common.function.Parameter}
	 * or {@link DefaultFunction.Builder#parameter(String, Class, Modifier...)}
	 * instead.
	 */
	final boolean keyed;

	/**
	 * @deprecated Use {@link DefaultFunction.Builder#parameter(String, Class, Modifier...)} instead.
	 */
	@Deprecated(since = "2.13", forRemoval = true)
	public Parameter(String name, ClassInfo<T> type, boolean single, @Nullable Expression<? extends T> def) {
		this(name, type, single, def, false);
	}

	/**
	 * @deprecated Use {@link org.skriptlang.skript.common.function.Parameter}
	 * or {@link DefaultFunction.Builder#parameter(String, Class, Modifier...)}
	 * instead.
	 */
	@Deprecated(since = "2.13", forRemoval = true)
	public Parameter(String name, ClassInfo<T> type, boolean single, @Nullable Expression<? extends T> def, boolean keyed) {
		this.name = name;
		this.type = type;
		this.def = def;
		this.single = single;
		this.keyed = keyed;
		this.modifiers = new HashSet<>();

		if (def != null) {
			modifiers.add(Modifier.OPTIONAL);
		}
		if (keyed) {
			modifiers.add(Modifier.KEYED);
		}
	}

	/**
	 * @deprecated Use {@link org.skriptlang.skript.common.function.Parameter}
	 * or {@link DefaultFunction.Builder#parameter(String, Class, Modifier...)}
	 * instead.
	 */
	@Deprecated(since = "2.13", forRemoval = true)
	public Parameter(String name, ClassInfo<T> type, boolean single, @Nullable Expression<? extends T> def, boolean keyed, boolean optional) {
		this.name = name;
		this.type = type;
		this.def = def;
		this.single = single;
		this.keyed = keyed;
		this.modifiers = new HashSet<>();

		if (optional) {
			modifiers.add(Modifier.OPTIONAL);
		}
		if (keyed) {
			modifiers.add(Modifier.KEYED);
		}
	}

	/**
	 * Constructs a new parameter for script functions.
	 *
	 * @param name The name.
	 * @param type The type of the parameter.
	 * @param single Whether the parameter is single.
	 * @param def The default value.
	 */
	Parameter(String name, ClassInfo<T> type, boolean single, @Nullable Expression<? extends T> def, Modifier... modifiers) {
		this.name = name;
		this.type = type;
		this.def = def;
		this.single = single;
		this.modifiers = Set.of(modifiers);
		this.keyed = this.modifiers.contains(Modifier.KEYED);
	}

	/**
	 * Returns whether this parameter is optional or not.
	 * @return Whether this parameter is optional or not.
	 */
	public boolean isOptional() {
		return modifiers.contains(Modifier.OPTIONAL);
	}

	/**
	 * @deprecated Use {@link #type()} instead.
	 */
	@Deprecated(forRemoval = true, since = "2.14")
	public ClassInfo<T> getType() {
		return type;
	}

	/**
	 * @deprecated Use {@link ScriptParameter#parse(String, Class, String)}} instead.
	 */
	@Deprecated(forRemoval = true, since = "2.14")
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

		Set<Modifier> modifiers = new HashSet<>();
		if (d != null) {
			modifiers.add(Modifier.OPTIONAL);
		}
		if (!single) {
			modifiers.add(Modifier.KEYED);
		}

		return new Parameter<>(name, type, single, d, modifiers.toArray(new Modifier[0]));
	}

	/**
	 * @deprecated Use {@link ch.njol.skript.structures.StructFunction.FunctionParser#parse(String, String, String, String, boolean)} instead.
	 */
	@Deprecated(forRemoval = true, since = "2.14")
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
	 * @deprecated Use {@link #name()} instead.
	 */
	@Deprecated(forRemoval = true, since = "2.13")
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
	public boolean equals(Object o) {
		if (!(o instanceof Parameter<?> parameter)) {
			return false;
		}

		return modifiers.equals(parameter.modifiers)
			&& single == parameter.single
			&& name.equals(parameter.name)
			&& type.equals(parameter.type)
			&& Objects.equals(def, parameter.def);
	}

	@Override
	public String toString() {
		return toString(Skript.debug());
	}

	// toString output format:
	// name: type between min and max = default
	//
	// Example:
	// ns: numbers between 0 and 100 = 3
	public String toString(boolean debug) {
		String result = name + ": " + Utils.toEnglishPlural(type.getCodeName(), !single);
		if (this.hasModifier(Modifier.RANGED)) {
			RangedModifier<?> range = this.getModifier(RangedModifier.class);
			result += " between " + Classes.toString(range.getMin()) + " and " + Classes.toString(range.getMax());
		}
		result += (def != null ? " = " + def.toString(null, debug) : "");
		return result;
	}

	@Override
	public @NotNull String name() {
		return name;
	}

	@Override
	public @NotNull Class<T> type() {
		//noinspection unchecked
		return (Class<T>) Signature.getReturns(single, type.getC());
	}

	@Override
	public @Unmodifiable @NotNull Set<Modifier> modifiers() {
		return Collections.unmodifiableSet(modifiers);
	}

	@Override
	public boolean isSingle() {
		return single;
	}

}
