package org.skriptlang.skript.common.function;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.Utils.PluralResult;
import ch.njol.util.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.SequencedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parsing for function structures.
 */
public class FunctionParser {

	/**
	 * Parses the signature from the given arguments.
	 *
	 * @param script  Script file name (<b>might</b> be used for some checks).
	 * @param name    The name of the function.
	 * @param args    The parameters of the function.
	 * @param returns The return type of the function, or null if the function should not return anything.
	 * @param local   If the signature of function is local.
	 * @return Parsed signature or null if something went wrong.
	 * @see Functions#registerSignature(ch.njol.skript.lang.function.Signature)
	 */
	public static @Nullable ch.njol.skript.lang.function.Signature<?> parse(String script, String name, String args, @Nullable String returns, boolean local) {
		Parameters parameters = parseParameters(args);
		if (parameters == null)
			return null;

		// Parse return type if one exists
		ClassInfo<?> returnClass;
		Class<?> returnType = null;

		if (returns != null) {
			returnClass = Classes.getClassInfoFromUserInput(returns);
			PluralResult result = Utils.isPlural(returns);

			if (returnClass == null)
				returnClass = Classes.getClassInfoFromUserInput(result.updated());

			if (returnClass == null) {
				Skript.error("Cannot recognise the type '" + returns + "'");
				return null;
			}

			if (result.plural()) {
				returnType = returnClass.getC().arrayType();
			} else {
				returnType = returnClass.getC();
			}

			return new ch.njol.skript.lang.function.Signature<>(script, name, parameters, returnType, local);
		}

		return new ch.njol.skript.lang.function.Signature<>(script, name, parameters, returnType, local);
	}

	/**
	 * Represents the pattern used for the parameter definition in a script function declaration.
	 * <p>
	 * The first group specifies the name of the parameter. The name may contain any characters
	 * but a colon, parenthesis, curly braces, double quotes, or a comma. Then, after a colon,
	 * the type is specified in the {@code type} group. If a default value is present, this is specified
	 * with the least amount of tokens as possible.
	 * </p>
	 * <p>
	 * Format:
	 * name: type = default
	 * [^:(){}\",]+?: [a-zA-Z ]+? = .+
	 * </p>
	 */
	private final static Pattern SCRIPT_PARAMETER_PATTERN =
			Pattern.compile("^\\s*(?<name>[^:(){}\",]+?)\\s*:\\s*(?<type>[a-zA-Z ]+?)\\s*(?:\\s*=\\s*(?<def>.+))?\\s*$");

	private static Parameters parseParameters(String args) {
		SequencedMap<String, Parameter<?>> params = new LinkedHashMap<>();

		boolean caseInsensitive = SkriptConfig.caseInsensitiveVariables.value();

		if (args.isEmpty()) // Zero-argument function
			return new Parameters(params);

		int j = 0;
		for (int i = 0; i <= args.length(); i = SkriptParser.next(args, i, ParseContext.DEFAULT)) {
			if (i == -1) {
				Skript.error("Invalid text/variables/parentheses in the arguments of this function");
				return null;
			}

			if (i != args.length() && args.charAt(i) != ',') {
				continue;
			}

			String arg = args.substring(j, i);

			// One or more arguments for this function
			Matcher matcher = SCRIPT_PARAMETER_PATTERN.matcher(arg);
			if (!matcher.matches()) {
				Skript.error("The " + StringUtils.fancyOrderNumber(params.size() + 1) + " argument's definition is invalid. It should look like 'name: type' or 'name: type = default value'.");
				return null;
			}

			String paramName = matcher.group("name");
			// for comparing without affecting the original name, in case the config option for case insensitivity changes.
			String lowerParamName = paramName.toLowerCase(Locale.ENGLISH);
			for (String otherName : params.keySet()) {
				// only force lowercase if we don't care about case in variables
				otherName = caseInsensitive ? otherName.toLowerCase(Locale.ENGLISH) : otherName;
				if (otherName.equals(caseInsensitive ? lowerParamName : paramName)) {
					Skript.error("Each argument's name must be unique, but the name '" + paramName + "' occurs at least twice.");
					return null;
				}
			}

			ClassInfo<?> classInfo = Classes.getClassInfoFromUserInput(matcher.group("type"));
			PluralResult result = Utils.isPlural(matcher.group("type"));

			if (classInfo == null)
				classInfo = Classes.getClassInfoFromUserInput(result.updated());

			if (classInfo == null) {
				Skript.error("Cannot recognise the type '%s'", matcher.group("type"));
				return null;
			}

			String variableName = paramName.endsWith("*") ? paramName.substring(0, paramName.length() - 3) +
					(!result.plural() ? "::1" : "") : paramName;

			Class<?> type;
			if (result.plural()) {
				type = classInfo.getC().arrayType();
			} else {
				type = classInfo.getC();
			}

			Parameter<?> parameter = ScriptParameter.parse(variableName, type, matcher.group("def"));

			if (parameter == null)
				return null;

			params.put(variableName, parameter);

			j = i + 1;
			if (i == args.length())
				break;
		}
		return new Parameters(params);
	}

}