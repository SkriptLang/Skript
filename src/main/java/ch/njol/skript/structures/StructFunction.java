package ch.njol.skript.structures;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Signature;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.Utils.PluralResult;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.function.Parameter;
import org.skriptlang.skript.lang.function.ScriptParameter;
import org.skriptlang.skript.lang.structure.Structure;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Name("Function")
@Description({
	"Functions are structures that can be executed with arguments/parameters to run code.",
	"They can also return a value to the trigger that is executing the function.",
	"Note that local functions come before global functions execution"
})
@Examples({
	"function sayMessage(message: text):",
	"\tbroadcast {_message} # our message argument is available in '{_message}'",
	"",
	"local function giveApple(amount: number) :: item:",
	"\treturn {_amount} of apple",
	"",
	"function getPoints(p: player) returns number:",
	"\treturn {points::%{_p}%}"
})
@Since("2.2, 2.7 (local functions)")
public class StructFunction extends Structure {

	public static final Priority PRIORITY = new Priority(400);

	private static final Pattern SIGNATURE_PATTERN =
		Pattern.compile("^(?:local )?function (" + Functions.functionNamePattern + ")\\((.*?)\\)(?:\\s*(?:::| returns )\\s*(.+))?$");
	private static final AtomicBoolean VALIDATE_FUNCTIONS = new AtomicBoolean();

	static {
		Skript.registerStructure(StructFunction.class,
			"[:local] function <.+>"
		);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private SectionNode source;
	@Nullable
	private Signature<?> signature;
	private boolean local;

	@Override
	public boolean init(Literal<?>[] literals, int matchedPattern, ParseResult parseResult, @Nullable EntryContainer entryContainer) {
		assert entryContainer != null; // cannot be null for non-simple structures
		this.source = entryContainer.getSource();
		local = parseResult.hasTag("local");
		return true;
	}

	@Override
	public boolean preLoad() {
		// match signature against pattern
		// noinspection ConstantConditions - entry container cannot be null as this structure is not simple
		String rawSignature = source.getKey();
		assert rawSignature != null;
		rawSignature = ScriptLoader.replaceOptions(rawSignature);
		Matcher matcher = SIGNATURE_PATTERN.matcher(rawSignature);
		if (!matcher.matches()) {
			Skript.error("Invalid function signature: " + rawSignature);
			return false;
		}

		// parse signature
		getParser().setCurrentEvent((local ? "local " : "") + "function", FunctionEvent.class);
		signature = FunctionParser.parse(
			getParser().getCurrentScript().getConfig().getFileName(),
			matcher.group(1), matcher.group(2), matcher.group(3), local
		);
		getParser().deleteCurrentEvent();

		// attempt registration
		return signature != null && Functions.registerSignature(signature) != null;
	}

	@Override
	public boolean load() {
		ParserInstance parser = getParser();
		parser.setCurrentEvent((local ? "local " : "") + "function", FunctionEvent.class);

		assert signature != null;
		// noinspection ConstantConditions - entry container cannot be null as this structure is not simple
		Functions.loadFunction(parser.getCurrentScript(), source, signature);

		parser.deleteCurrentEvent();

		VALIDATE_FUNCTIONS.set(true);

		return true;
	}

	@Override
	public boolean postLoad() {
		if (VALIDATE_FUNCTIONS.get()) {
			VALIDATE_FUNCTIONS.set(false);
			Functions.validateFunctions();
		}
		return true;
	}

	@Override
	public void unload() {
		assert signature != null;
		Functions.unregisterFunction(signature);
		VALIDATE_FUNCTIONS.set(true);
	}

	@Override
	public Priority getPriority() {
		return PRIORITY;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (local ? "local " : "") + "function";
	}

	public static class FunctionParser {

		/**
		 * Parses the signature from the given arguments.
		 *
		 * @param script  Script file name (<b>might</b> be used for some checks).
		 * @param name    The name of the function.
		 * @param args    The parameters of the function.
		 * @param returns The return type of the function
		 * @param local   If the signature of function is local.
		 * @return Parsed signature or null if something went wrong.
		 * @see Functions#registerSignature(Signature)
		 */
		public static @Nullable Signature<?> parse(String script, String name, String args, @Nullable String returns, boolean local) {
			LinkedHashMap<String, Parameter<?>> parameters = parseParameters(args);
			if (parameters == null)
				return null;

			// Parse return type if one exists
			Class<?> returnType;
			ClassInfo<?> returnClass;

			if (returns == null) {
				returnType = null;
			} else {
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
			}

			return new Signature<>(script, name, parameters, local, returnType, null);
		}

		private final static Pattern SCRIPT_PARAMETER_PATTERN =
			Pattern.compile("^\\s*(?<name>[^:(){}\",]+?)\\s*:\\s*(?<type>[a-zA-Z ]+?)\\s*(?:\\s*=\\s*(?<def>.+))?\\s*$");

		private static LinkedHashMap<String, Parameter<?>> parseParameters(String args) {
			LinkedHashMap<String, Parameter<?>> params = new LinkedHashMap<>();

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
					Matcher n = SCRIPT_PARAMETER_PATTERN.matcher(arg);
					if (!n.matches()) {
						Skript.error("The " + StringUtils.fancyOrderNumber(params.size() + 1) + " argument's definition is invalid. It should look like 'name: type' or 'name: type = default value'.");
						return null;
					}

					String paramName = n.group("name");
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

					ClassInfo<?> c = Classes.getClassInfoFromUserInput(n.group("type"));
					PluralResult result = Utils.isPlural(n.group("type"));

					if (c == null)
						c = Classes.getClassInfoFromUserInput(result.updated());

					if (c == null) {
						Skript.error("Cannot recognise the type '%s'", n.group("type"));
						return null;
					}

					String variableName = paramName.endsWith("*") ? paramName.substring(0, paramName.length() - 3) +
						(!result.plural() ? "::1" : "") : paramName;

					Class<?> type;
					if (result.plural()) {
						type = c.getC().arrayType();
					} else {
						type = c.getC();
					}

					Parameter<?> parameter = ScriptParameter.parse(variableName, type, n.group("def"));

					if (parameter == null)
						return null;

					params.put(variableName, parameter);

					j = i + 1;
				}
				if (i == args.length())
					break;
			}
			return params;
		}

	}

}
