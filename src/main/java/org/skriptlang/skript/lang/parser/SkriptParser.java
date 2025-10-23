
package org.skriptlang.skript.lang.parser;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.DefaultExpressionUtils.DefaultExpressionError;
import ch.njol.skript.lang.SkriptParser.ExprInfo;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.DefaultValueData;
import ch.njol.skript.lang.parser.ParseStackOverflowException;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.parser.ParsingStack;
import ch.njol.skript.lang.simplification.Simplifiable;
import ch.njol.skript.localization.Language;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.patterns.MalformedPatternException;
import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.patterns.SkriptPattern;
import ch.njol.skript.patterns.TypePatternElement;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.experiment.ExperimentSet;
import org.skriptlang.skript.lang.experiment.ExperimentalSyntax;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry.Key;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class SkriptParser {

	@Deprecated(since = "INSERT VERSION")
	public static final int PARSE_EXPRESSIONS = 1;
	@Deprecated(since = "INSERT VERSION")
	public static final int PARSE_LITERALS = 2;
	@Deprecated(since = "INSERT VERSION")
	public static final int ALL_FLAGS = PARSE_EXPRESSIONS | PARSE_LITERALS;

	private static final Map<String, SkriptPattern> patterns = new ConcurrentHashMap<>();


	/**
	 * Matches ',', 'and', 'or', etc. as well as surrounding whitespace.
	 * <p>
	 * group 1 is null for ',', otherwise it's one of and/or/nor (not necessarily lowercase).
	 */
	public static final Pattern LIST_SPLIT_PATTERN = Pattern.compile("\\s*,?\\s+(and|n?or)\\s+|\\s*,\\s*", Pattern.CASE_INSENSITIVE);
	public static final Pattern OR_PATTERN = Pattern.compile("\\sor\\s", Pattern.CASE_INSENSITIVE);
	protected boolean suppressMissingAndOrWarnings = SkriptConfig.disableMissingAndOrWarnings.value();

	protected ParsingConstraints parsingConstraints;
	protected final String input;
	public final ParseContext context;

	public final boolean doSimplification = SkriptConfig.simplifySyntaxesOnParse.value();


	/**
	 * Constructs a new SkriptParser object that can be used to parse the given expression.
	 * Parses expressions and literals using {@link ParseContext#DEFAULT}.
	 * <p>
	 * A SkriptParser can be re-used indefinitely for the given expression, but to parse a new expression a new SkriptParser has to be created.
	 *
	 * @param input The text to parse.
	 */
	protected SkriptParser(String input) {
		this(input, ParsingConstraints.all());
	}

	/**
	 * Constructs a new SkriptParser object that can be used to parse the given expression.
	 * Parses using {@link ParseContext#DEFAULT}.
	 * <p>
	 * A SkriptParser can be re-used indefinitely for the given expression, but to parse a new expression a new SkriptParser has to be created.
	 *
	 * @param constraints The constraints under which to parse.
	 * @param input The text to parse.
	 */
	protected SkriptParser(String input, ParsingConstraints constraints) {
		this(input, constraints, ParseContext.DEFAULT);
	}

	/**
	 * Constructs a new SkriptParser object that can be used to parse the given expression.
	 * <p>
	 * A SkriptParser can be re-used indefinitely for the given expression, but to parse a new expression a new SkriptParser has to be created.
	 *
	 * @param input The text to parse.
	 * @param constraints The constraints under which to parse.
	 * @param context The parse context.
	 */
	protected SkriptParser(@NotNull String input, ParsingConstraints constraints, ParseContext context) {
		this.input = input.trim();
		this.parsingConstraints = constraints;
		this.context = context;
	}

	/**
	 * Constructs a new SkriptParser object that can be used to parse the given expression.
	 * <p>
	 * A SkriptParser can be re-used indefinitely for the given expression, but to parse a new expression a new SkriptParser has to be created.
	 *
	 * @param other The other SkriptParser to copy input, constraints, and context from.
	 */
	protected SkriptParser(@NotNull SkriptParser other) {
		this(other.input, other.parsingConstraints, other.context);
		this.suppressMissingAndOrWarnings = other.suppressMissingAndOrWarnings;
	}

	protected SkriptParser(@NotNull SkriptParser other, String input) {
		this(input, other.parsingConstraints, other.context);
		this.suppressMissingAndOrWarnings = other.suppressMissingAndOrWarnings;
	}

	/**
	 * Parses a string as one of the given syntax elements.
	 * <p>
	 * Can print an error.
	 *
	 * @param <I> The {@link SyntaxInfo} type associated with the given
	 * 			{@link Key}.
	 * @param <E> The type of the returned {@link SyntaxElement}, which should be equivalent to the class
	 *              returned by {@link SyntaxInfo#type()}.
	 * @param input The raw string input to be parsed.
	 * @param parsingConstraints A {@link ParsingConstraints} object containing all the allowed syntaxes.
	 * @param expectedTypeKey A {@link Key} that determines what
	 *                           kind of syntax is expected as a result of the parsing.
	 * @param context The context under which to parse this string.
	 * @param defaultError The default error to use if no other error is encountered during parsing.
	 * @return A parsed, initialized {@link SyntaxElement}, or null if parsing failed.
	 */
	public static <E extends SyntaxElement, I extends SyntaxInfo<E>> @Nullable E parse(
		String input,
		@NotNull ParsingConstraints parsingConstraints,
		Key<I> expectedTypeKey,
		ParseContext context,
		@Nullable String defaultError
	) {
		Iterator<I> uncheckedIterator = Skript.instance().syntaxRegistry().syntaxes(expectedTypeKey).iterator();

		return SkriptParser.parse(
			input,
			parsingConstraints,
			uncheckedIterator,
			context,
			defaultError
		);
	}

	/**
	 * Parses a string as one of the given syntax elements.
	 * <p>
	 * Can print an error.
	 *
	 * @param <E> The type of the returned {@link SyntaxElement}, which should be equivalent to the class
	 *              returned by {@link SyntaxInfo#type()}.
	 * @param input The raw string input to be parsed.
	 * @param parsingConstraints A {@link ParsingConstraints} object containing all the allowed syntaxes.
	 * @param allowedSyntaxes An {@link Iterator} over {@link SyntaxElementInfo} objects that represent the allowed syntaxes.
	 * @param context The context under which to parse this string.
	 * @param defaultError The default error to use if no other error is encountered during parsing.
	 * @return A parsed, initialized {@link SyntaxElement}, or null if parsing failed.
	 */
	public static <E extends SyntaxElement> @Nullable E parse(
		String input,
		@NotNull ParsingConstraints parsingConstraints,
		Iterator<? extends SyntaxInfo<? extends E>> allowedSyntaxes,
		ParseContext context,
		@Nullable String defaultError
	) {
		input = input.trim();
		if (input.isEmpty()) {
			Skript.error(defaultError);
			return null;
		}
		try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
			E element = new SkriptParser(input, parsingConstraints, context).parse(allowedSyntaxes);
			if (element != null) {
				log.printLog();
				return element;
			}
			log.printError(defaultError);
			return null;
		}
	}

	/**
	 * @deprecated use {@link #parse(String, ParsingConstraints, Iterator, ParseContext, String)} with
	 * 	{@link ParsingConstraints#allowLiterals(boolean)} set to false and {@link ParseContext#DEFAULT}.
	 */
	@Deprecated
	public static <T extends SyntaxElement> @Nullable T parseStatic(
		String expr,
		Iterator<? extends SyntaxInfo<? extends T>> source,
		@Nullable String defaultError
	) {
		return parse(expr, ParsingConstraints.all().allowNonLiterals(false), source, ParseContext.DEFAULT, defaultError);
	}

	@Deprecated
	public static <T extends SyntaxElement> @Nullable T parseStatic(
		String expr,
		Iterator<? extends SyntaxInfo<? extends T>> source,
		ParseContext parseContext,
		@Nullable String defaultError
	) {
		return parse(expr, ParsingConstraints.all().allowNonLiterals(false), source, parseContext, defaultError);
	}


	/**
	 * Attempts to parse this parser's input against the given syntax.
	 * Prints parse errors (i.e. must start a ParseLog before calling this method)
	 * {@link #parse(Key)} is preferred for parsing against a specific syntax.
	 *
	 * @param allowedSyntaxes The iterator of {@link SyntaxElementInfo} objects to parse against.
	 * @return A parsed {@link SyntaxElement} with its {@link SyntaxElement#init(Expression[], int, Kleenean, SkriptParser.ParseResult)}
	 * 			method having been run and returned true. If no successful parse can be made, null is returned.
	 * @param <E> The type of {@link SyntaxElement} that will be returned.
	 */
	@ApiStatus.Internal
	public <E extends SyntaxElement> @Nullable E parse(@NotNull Iterator<? extends SyntaxInfo<? extends E>> allowedSyntaxes) {
		allowedSyntaxes = parsingConstraints.constrainIterator(allowedSyntaxes);
		try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
			// for each allowed syntax
			while (allowedSyntaxes.hasNext()) {
				SyntaxInfo<? extends E> info = allowedSyntaxes.next();
				// check each of its patterns
				int patternIndex = 0;
				for (String pattern : info.patterns()) {
					log.clear();
					E element = parse(info, pattern, patternIndex);
					// return if this pattern parsed successfully
					if (element != null) {
						log.printLog();
						return element;
					}
					patternIndex++;
				}
			}
			log.printError();
			return null;
		}
	}

	/**
	 * Attempts to parse this parser's input against the given syntax type.
	 * Prints parse errors (i.e. must start a ParseLog before calling this method).
	 *
	 * @param expectedTypeKey A {@link Key} that determines what
	 *                           kind of syntax is expected as a result of the parsing.
	 * @return A parsed {@link SyntaxElement} with its {@link SyntaxElement#init(Expression[], int, Kleenean, SkriptParser.ParseResult)}
	 * 			method having been run and returned true. If no successful parse can be made, null is returned.
	 * @param <E> The type of {@link SyntaxElement} that will be returned.
	 */
	@ApiStatus.Internal
	public <E extends SyntaxElement, I extends SyntaxInfo<? extends E>> @Nullable E parse(Key<I> expectedTypeKey) {
		Iterator<SyntaxElementInfo<E>> uncheckedIterator = new Iterator<>() {

			private final Iterator<I> iterator = Skript.instance().syntaxRegistry().syntaxes(expectedTypeKey).iterator();

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			@Contract(" -> new")
			public @NotNull SyntaxElementInfo<E> next() {
				return SyntaxElementInfo.fromModern(iterator.next());
			}
		};

		return parse(uncheckedIterator);
	}

	/**
	 * Attempts to parse this parser's input against the given pattern.
	 * Prints parse errors (i.e. must start a ParseLog before calling this method).
	 * @return A parsed {@link SyntaxElement} with its {@link SyntaxElement#init(Expression[], int, Kleenean, ParseResult)}
	 * 			method having been run and returned true. If no successful parse can be made, null is returned.
	 * @param <E> The type of {@link SyntaxElement} that will be returned.
	 */
	private <E extends SyntaxElement> @Nullable E parse(@NotNull SyntaxInfo<? extends E> info, String pattern, int patternIndex) {
		ParsingStack parsingStack = getParser().getParsingStack();
		ParseResult parseResult;
		try {
			// attempt to parse with the given pattern
			parsingStack.push(new ParsingStack.Element(info, patternIndex));
			parseResult = parseAgainstPattern(pattern);
		} catch (MalformedPatternException exception) {
			// if the pattern failed to compile:
			String message = "pattern compiling exception, element class: " + info.type().getName();
			try {
				JavaPlugin providingPlugin = JavaPlugin.getProvidingPlugin(info.type());
				message += " (provided by " + providingPlugin.getName() + ")";
			} catch (IllegalArgumentException | IllegalStateException ignored) {}

			throw new RuntimeException(message, exception);
		} catch (StackOverflowError e) {
			// Parsing caused a stack overflow, possibly due to too long lines
			throw new ParseStackOverflowException(e, new ParsingStack(parsingStack));
		} finally {
			// Recursive parsing call done, pop the element from the parsing stack
			ParsingStack.Element stackElement = parsingStack.pop();
			assert stackElement.syntaxElementInfo() == info && stackElement.patternIndex() == patternIndex;
		}

		// if parsing was successful, attempt to populate default expressions
		if (parseResult == null || !populateDefaultExpressions(parseResult, pattern))
			return null;

		E element;
		// construct instance
		try {
			element = info.type().getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException("Failed to create instance of " + info.type().getName(), e);
		}

		// if default expr population succeeded, try to init element.
		if (initializeElement(element, patternIndex, parseResult)) {
			if (doSimplification && element instanceof Simplifiable<?> simplifiable)
				//noinspection unchecked
				return (E) simplifiable.simplify();
			return element;
		}

		return null;
	}

	/**
	 * Runs through all the initialization checks and steps for the given element, finalizing in a call to {@link SyntaxElement#init(Expression[], int, Kleenean, ParseResult)}.
	 * @param element The element to initialize.
	 * @param patternIndex The index of the pattern that was matched.
	 * @param parseResult The parse result from parsing this element.
	 * @return Whether the element was successfully initialized.
	 */
	private boolean initializeElement(SyntaxElement element, int patternIndex, ParseResult parseResult) {
		if (!checkRestrictedEvents(element, parseResult))
			return false;

		if (!checkExperimentalSyntax(element))
			return false;

		// try to initialize the element
		boolean success = element.preInit() && element.init(parseResult.exprs, patternIndex, getParser().getHasDelayBefore(), parseResult);
		if (success) {
			// Check if any expressions are 'UnparsedLiterals' and if applicable for multiple info warning.
			for (Expression<?> expr : parseResult.exprs) {
				if (expr instanceof UnparsedLiteral unparsedLiteral && unparsedLiteral.multipleWarning())
					break;
			}
			return true;
		}
		return false;
	}

	/**
	 * Attempts to match this parser's input against the given pattern. Any sub-elements (expressions) will be
	 * parsed and initialized. Default values will not be populated.
	 * Prints parse errors (i.e. must start a ParseLog before calling this method).
	 * @return A {@link ParseResult} containing the results of the parsing, if successful. Null otherwise.
	 * @see #parse(SyntaxInfo, String, int)
	 */
	private @Nullable ParseResult parseAgainstPattern(String pattern) throws MalformedPatternException {
		SkriptPattern skriptPattern = patterns.computeIfAbsent(pattern, PatternCompiler::compile);
		ch.njol.skript.patterns.MatchResult matchResult = skriptPattern.match(input, parsingConstraints.asParseFlags(), context);
		if (matchResult == null)
			return null;
		return matchResult.toParseResult();
	}

	/**
	 * Given a parseResult, populates any default expressions that need to be filled.
	 * If no such default expression can be found, false will be returned.
	 * @param parseResult The parse result to populate.
	 * @param pattern The pattern to use to locate required default expressions.
	 * @return true if population was successful, false otherwise.
	 */
	private boolean populateDefaultExpressions(@NotNull ParseResult parseResult, String pattern) {
		assert parseResult.source != null; // parse results from parseAgainstPattern have a source
		List<TypePatternElement> types = null;
		for (int i = 0; i < parseResult.exprs.length; i++) {
			if (parseResult.exprs[i] == null) {
				if (types == null)
					types = parseResult.source.getElements(TypePatternElement.class);
				ExprInfo exprInfo = types.get(i).getExprInfo();
				if (!exprInfo.isOptional) {
					List<DefaultExpression<?>> exprs = getDefaultExpressions(exprInfo, pattern);
					DefaultExpression<?> matchedExpr = null;
					for (DefaultExpression<?> expr : exprs) {
						if (expr.init()) {
							matchedExpr = expr;
							break;
						}
					}
					if (matchedExpr == null)
						return false;
					parseResult.exprs[i] = matchedExpr;
				}
			}
		}
		return true;
	}


	/**
	 * Returns the {@link DefaultExpression} from the first {@link ClassInfo} stored in {@code exprInfo}.
	 *
	 * @param exprInfo The {@link ExprInfo} to check for {@link DefaultExpression}.
	 * @param pattern The pattern used to create {@link ExprInfo}.
	 * @return {@link DefaultExpression}.
	 * @throws SkriptAPIException If the {@link DefaultExpression} is not valid, produces an error message for the reasoning of failure.
	 */
	private static @NotNull DefaultExpression<?> getDefaultExpression(ExprInfo exprInfo, String pattern) {
		DefaultValueData data = getParser().getData(DefaultValueData.class);
		ClassInfo<?> classInfo = exprInfo.classes[0];
		DefaultExpression<?> expr = data.getDefaultValue(classInfo.getC());
		if (expr == null)
			expr = classInfo.getDefaultExpression();

		DefaultExpressionError errorType = DefaultExpressionUtils.isValid(expr, exprInfo, 0);
		if (errorType == null) {
			assert expr != null;
			return expr;
		}

		throw new SkriptAPIException(errorType.getError(List.of(classInfo.getCodeName()), pattern));
	}

	/**
	 * Returns all {@link DefaultExpression}s from all the {@link ClassInfo}s embedded in {@code exprInfo} that are valid.
	 *
	 * @param exprInfo The {@link ExprInfo} to check for {@link DefaultExpression}s.
	 * @param pattern The pattern used to create {@link ExprInfo}.
	 * @return All available {@link DefaultExpression}s.
	 * @throws SkriptAPIException If no {@link DefaultExpression}s are valid, produces an error message for the reasoning of failure.
	 */
	static @NotNull List<DefaultExpression<?>> getDefaultExpressions(ExprInfo exprInfo, String pattern) {
		if (exprInfo.classes.length == 1)
			return new ArrayList<>(List.of(getDefaultExpression(exprInfo, pattern)));

		DefaultValueData data = getParser().getData(DefaultValueData.class);

		EnumMap<DefaultExpressionError, List<String>> failed = new EnumMap<>(DefaultExpressionError.class);
		List<DefaultExpression<?>> passed = new ArrayList<>();
		for (int i = 0; i < exprInfo.classes.length; i++) {
			ClassInfo<?> classInfo = exprInfo.classes[i];
			DefaultExpression<?> expr = data.getDefaultValue(classInfo.getC());
			if (expr == null)
				expr = classInfo.getDefaultExpression();

			String codeName = classInfo.getCodeName();
			DefaultExpressionError errorType = DefaultExpressionUtils.isValid(expr, exprInfo, i);

			if (errorType != null) {
				failed.computeIfAbsent(errorType, list -> new ArrayList<>()).add(codeName);
			} else {
				passed.add(expr);
			}
		}

		if (!passed.isEmpty())
			return passed;

		List<String> errors = new ArrayList<>();
		for (Map.Entry<DefaultExpressionError, List<String>> entry : failed.entrySet()) {
			String error = entry.getKey().getError(entry.getValue(), pattern);
			errors.add(error);
		}
		throw new SkriptAPIException(StringUtils.join(errors, "\n"));
	}

	/**
	 * Checks whether the given element is restricted to specific events, and if so, whether the current event is allowed.
	 * Prints errors.
	 * @param element The syntax element to check.
	 * @param parseResult The parse result for error information.
	 * @return True if the element is allowed in the current event, false otherwise.
	 */
	private static boolean checkRestrictedEvents(SyntaxElement element, ParseResult parseResult) {
		if (element instanceof EventRestrictedSyntax eventRestrictedSyntax) {
			Class<? extends Event>[] supportedEvents = eventRestrictedSyntax.supportedEvents();
			if (!getParser().isCurrentEvent(supportedEvents)) {
				Skript.error("'" + parseResult.expr + "' can only be used in "
						+ EventRestrictedSyntax.supportedEventsNames(supportedEvents));
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks that {@code element} is an {@link ExperimentalSyntax} and, if so, ensures that its requirements are satisfied by the current {@link ExperimentSet}.
	 * @param element The {@link SyntaxElement} to check.
	 * @return {@code True} if the {@link SyntaxElement} is not an {@link ExperimentalSyntax} or is satisfied.
	 */
	private static <T extends SyntaxElement> boolean checkExperimentalSyntax(T element) {
		if (!(element instanceof ExperimentalSyntax experimentalSyntax))
			return true;
		ExperimentSet experiments = getParser().getExperimentSet();
		return experimentalSyntax.isSatisfiedBy(experiments);
	}

	/**
	 * @see ParserInstance#get()
	 */
	protected static ParserInstance getParser() {
		return ParserInstance.get();
	}

	@Contract("-> this")
	public SkriptParser suppressMissingAndOrWarnings() {
		suppressMissingAndOrWarnings = true;
		return this;
	}

	/**
	 * @param types The types to include in the message
	 * @return "not an x" or "neither an x, a y nor a z"
	 */
	public static String notOfType(Class<?>... types) {
		if (types.length == 1) {
			Class<?> type = types[0];
			assert type != null;
			return Language.get("not") + " " + Classes.getSuperClassInfo(type).getName().withIndefiniteArticle();
		} else {
			StringBuilder message = new StringBuilder(Language.get("neither") + " ");
			for (int i = 0; i < types.length; i++) {
				if (i != 0) {
					if (i != types.length - 1) {
						message.append(", ");
					} else {
						message.append(" ").append(Language.get("nor")).append(" ");
					}
				}
				Class<?> c = types[i];
				assert c != null;
				ClassInfo<?> classInfo = Classes.getSuperClassInfo(c);
				// if there's a registered class info,
				if (classInfo != null) {
					// use the article,
					message.append(classInfo.getName().withIndefiniteArticle());
				} else {
					// otherwise fallback to class name
					message.append(c.getName());
				}
			}
			return message.toString();
		}
	}

	public static String notOfType(ClassInfo<?>... types) {
		if (types.length == 1) {
			return Language.get("not") + " " + types[0].getName().withIndefiniteArticle();
		} else {
			StringBuilder message = new StringBuilder(Language.get("neither") + " ");
			for (int i = 0; i < types.length; i++) {
				if (i != 0) {
					if (i != types.length - 1) {
						message.append(", ");
					} else {
						message.append(" ").append(Language.get("nor")).append(" ");
					}
				}
				message.append(types[i].getName().withIndefiniteArticle());
			}
			return message.toString();
		}
	}

}
