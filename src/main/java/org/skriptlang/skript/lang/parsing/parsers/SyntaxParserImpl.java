package org.skriptlang.skript.lang.parsing.parsers;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.parser.DefaultValueData;
import ch.njol.skript.lang.parser.ParseStackOverflowException;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.parser.ParsingStack;
import ch.njol.skript.lang.simplification.Simplifiable;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.patterns.MalformedPatternException;
import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.patterns.SkriptPattern;
import ch.njol.skript.patterns.TypePatternElement;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.iterator.CheckedIterator;
import com.google.common.base.Preconditions;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.Skript;
import org.skriptlang.skript.lang.parsing.ParsingContext;
import org.skriptlang.skript.lang.parsing.constraints.Constraints;
import org.skriptlang.skript.log.runtime.RuntimeError;
import org.skriptlang.skript.log.runtime.RuntimeErrorCatcher;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * A parser with configurable constraints and context for parsing Skript syntax elements.
 * Implementations should use {@link SyntaxParserImpl} as a base class, which provides default implementations for {@link SyntaxParser}.
 * @param <P> the type of the parser subclass
 */
public class SyntaxParserImpl<P extends SyntaxParser<P>> implements SyntaxParser<P> {

	private static final Map<String, SkriptPattern> patterns = new ConcurrentHashMap<>();

	/**
	 * Matches ',', 'and', 'or', etc. as well as surrounding whitespace.
	 * <p>
	 * group 1 is null for ',', otherwise it's one of and/or/nor (not necessarily lowercase).
	 */
	protected boolean suppressMissingAndOrWarnings = SkriptConfig.disableMissingAndOrWarnings.value();
	public final boolean doSimplification = SkriptConfig.simplifySyntaxesOnParse.value();

	protected Constraints localConstraints;
	protected ParseContext context;
	protected String input;
	protected String defaultError;
	protected final Skript skript;

	protected SyntaxParserImpl(Skript skript) {
		this.context = ParseContext.DEFAULT;
		this.skript = skript;
		this.localConstraints = null;
	}

	public SyntaxParserImpl(@NotNull SyntaxParser<?> other) {
		this.context = other.parseContext();
		this.input = other.input();
		this.defaultError = other.defaultError();
		this.skript = other.skript();
		this.localConstraints = other.constraints();
		if (other instanceof SyntaxParserImpl<?> otherImpl)
			this.suppressMissingAndOrWarnings = otherImpl.suppressMissingAndOrWarnings;
	}

	@Override
	public <E extends SyntaxElement> @Nullable E parse(@NotNull Iterator<? extends SyntaxInfo<? extends E>> allowedSyntaxes) {
		return getParser().withConstraints(localConstraints, (constraints) -> parse(constraints, allowedSyntaxes));
	}

	@Override
	public <E extends SyntaxElement, I extends SyntaxInfo<? extends E>> @Nullable E parse(SyntaxRegistry.Key<I> expectedTypeKey) {
		Iterator<? extends I> candidateSyntaxes = skript.syntaxRegistry().syntaxes(expectedTypeKey).iterator();
		return parse(candidateSyntaxes);
	}

	/**
	 * Asserts that the parser is ready to parse. Implementations should call this method before attempting to parse.
	 */
	protected void assertReadyToParse() {
		Preconditions.checkNotNull(input, "Input string must be set before parsing.");
		Preconditions.checkNotNull(localConstraints, "Parsing constraints must be set before parsing.");
		Preconditions.checkNotNull(context, "Parse context must be set before parsing.");
	}

	@Contract(pure = true)
	public String input() {
		return input;
	}

	@Contract("_ -> this")
	@SuppressWarnings("unchecked")
	public P input(@NotNull String input) {
		this.input = input;
		return (P) this;
	}

	@Contract(pure = true)
	public Constraints constraints() {
		return localConstraints;
	}

	@Contract("_ -> this")
	@SuppressWarnings("unchecked")
	public P constraints(@NotNull Constraints localConstraints) {
		this.localConstraints = localConstraints;
		return (P) this;
	}

	@Contract(pure = true)
	public ParseContext parseContext() {
		return context;
	}

	@Contract("_ -> this")
	@SuppressWarnings("unchecked")
	public P parseContext(@NotNull ParseContext context) {
		this.context = context;
		return (P) this;
	}

	@Contract(pure = true)
	public @Nullable String defaultError() {
		return defaultError;
	}

	@Contract("_ -> this")
	@SuppressWarnings("unchecked")
	public P defaultError(@Nullable String defaultError) {
		this.defaultError = defaultError;
		return (P) this;
	}

	@Override
	public Skript skript() {
		return skript;
	}

	// --------------------------------------------------------------------------------
	// PARSING LOGIC
	// --------------------------------------------------------------------------------

	/**
	 * Internal parse method that performs the actual parsing logic. Most implementations shou
	 * @param constraints The combined constraints to use for parsing.
	 * @param allowedSyntaxes The iterator of syntaxes to parse against.
	 * @param <E> The type of {@link SyntaxElement} that will be returned.
	 * @return A parsed {@link SyntaxElement} if successful, null otherwise.
	 */
	protected <E extends SyntaxElement> @Nullable E parse(
		Constraints constraints,
		Iterator<? extends SyntaxInfo<? extends E>> allowedSyntaxes
	) {
		ParsingContext ctx = ParserInstance.getContext();
		allowedSyntaxes = new CheckedIterator<>(allowedSyntaxes, info -> constraints.acceptsInfo(info, ctx));
		try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
			// for each allowed syntax
			while (allowedSyntaxes.hasNext()) {
				SyntaxInfo<? extends E> info = allowedSyntaxes.next();
				// check each of its patterns
				int patternIndex = 0;
				for (String pattern : info.patterns()) {
					log.clear();
					E element = parse(constraints, info, pattern, patternIndex);
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
	 * Attempts to parse this parser's input against the given pattern.
	 * Prints parse errors (i.e. must start a ParseLog before calling this method).
	 * @return A parsed {@link SyntaxElement} with its {@link SyntaxElement#init(Expression[], int, Kleenean, SkriptParser.ParseResult)}
	 * 			method having been run and returned true. If no successful parse can be made, null is returned.
	 * @param <E> The type of {@link SyntaxElement} that will be returned.
	 */
	private <E extends SyntaxElement> @Nullable E parse(
		Constraints constraints,
		@NotNull SyntaxInfo<? extends E> info,
		String pattern,
		int patternIndex
	) {
		ParsingStack parsingStack = getParser().getParsingStack();
		SkriptParser.ParseResult parseResult;
		try {
			// attempt to parse with the given pattern
			parsingStack.push(new ParsingStack.Element(info, patternIndex));
			parseResult = parseAgainstPattern(constraints, pattern);
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

		ParsingContext ctx = ParserInstance.getContext();

		// check pre-init constraints (read-only, outside attempt())
		if (!constraints.acceptsPreInit(info, element, parseResult, ctx))
			return null;

		// wrap init() in attempt() to roll back hasDelayBefore/hintManager on failure
		return ctx.attempt(() -> {
			if (!initializeElement(element, patternIndex, parseResult))
				return null;

			@SuppressWarnings("unchecked")
			E result = (doSimplification && element instanceof Simplifiable<?> simplifiable)
				? (E) simplify(simplifiable)
				: element;

			if (result == null)
				return null;

			// check post-init constraints
			if (!constraints.acceptsPostInit(result, parseResult, ctx))
				return null;

			return result;
		});
	}

	/**
	 * Runs through all the initialization checks and steps for the given element, finalizing in a call to {@link SyntaxElement#init(Expression[], int, Kleenean, SkriptParser.ParseResult)}.
	 * @param element The element to initialize.
	 * @param patternIndex The index of the pattern that was matched.
	 * @param parseResult The parse result from parsing this element.
	 * @return Whether the element was successfully initialized.
	 */
	private boolean initializeElement(SyntaxElement element, int patternIndex, SkriptParser.ParseResult parseResult) {
		// try to initialize the element
		boolean success = element.preInit() && element.init(parseResult.exprs, patternIndex, ParserInstance.getContext().getHasDelayBefore(), parseResult);
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
	 * Returns a simplified version of element, unless a runtime error is thrown, in which case a parse error is printed
	 * and null is returned.
	 * @param element The element to simplify
	 * @return The simplified element, or null if simplification failed. Elements unable to simplify will return themselves.
	 * @param <T> The element type.
	 */
	private <T extends SyntaxElement> @Nullable T simplify(@NotNull Simplifiable<T> element) {
		// add runtime consumer to catch runtime errors and turn them into parse time errors
		T simplified;
		try (RuntimeErrorCatcher catcher = new RuntimeErrorCatcher().start()) {
			simplified = element.simplify();
			// we can assume that if a single simplification throws many errors, the first will be at least somewhat representative
			boolean hasError = false;
			boolean hasWarning = false;
			for (RuntimeError err : catcher.getCachedErrors()) {
				if (!hasError && err.level() == Level.SEVERE) {
					ch.njol.skript.Skript.error(err.error());
					hasError = true;
				} else if (!hasWarning && err.level() == Level.WARNING) {
					ch.njol.skript.Skript.warning(err.error());
					hasWarning = true;
				}
				if (hasError && hasWarning) break;
			}
			if (hasError)
				return null;
			return simplified;
		}
	}

	/**
	 * Attempts to match this parser's input against the given pattern. Any sub-elements (expressions) will be
	 * parsed and initialized. Default values will not be populated.
	 * Prints parse errors (i.e. must start a ParseLog before calling this method).
	 * @return A {@link SkriptParser.ParseResult} containing the results of the parsing, if successful. Null otherwise.
	 * @see #parse(Constraints, SyntaxInfo, String, int)
	 */
	private @Nullable SkriptParser.ParseResult parseAgainstPattern(Constraints constraints, String pattern) throws MalformedPatternException {
		SkriptPattern skriptPattern = patterns.computeIfAbsent(pattern, PatternCompiler::compile);
		ch.njol.skript.patterns.MatchResult matchResult = skriptPattern.match(input, constraints.asParseFlags(), context);
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
	private boolean populateDefaultExpressions(@NotNull SkriptParser.ParseResult parseResult, String pattern) {
		assert parseResult.source != null; // parse results from parseAgainstPattern have a source
		List<TypePatternElement> types = null;
		for (int i = 0; i < parseResult.exprs.length; i++) {
			if (parseResult.exprs[i] == null) {
				if (types == null)
					types = parseResult.source.getElements(TypePatternElement.class);
				SkriptParser.ExprInfo exprInfo = types.get(i).getExprInfo();
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
	 * @param exprInfo The {@link SkriptParser.ExprInfo} to check for {@link DefaultExpression}.
	 * @param pattern The pattern used to create {@link SkriptParser.ExprInfo}.
	 * @return {@link DefaultExpression}.
	 * @throws SkriptAPIException If the {@link DefaultExpression} is not valid, produces an error message for the reasoning of failure.
	 */
	private static @NotNull DefaultExpression<?> getDefaultExpression(SkriptParser.ExprInfo exprInfo, String pattern) {
		DefaultValueData data = getParser().getData(DefaultValueData.class);
		ClassInfo<?> classInfo = exprInfo.classes[0];
		DefaultExpression<?> expr = data.getDefaultValue(classInfo.getC());
		if (expr == null)
			expr = classInfo.getDefaultExpression();

		DefaultExpressionUtils.DefaultExpressionError errorType = DefaultExpressionUtils.isValid(expr, exprInfo, 0);
		if (errorType == null) {
			assert expr != null;
			return expr;
		}

		throw new SkriptAPIException(errorType.getError(List.of(classInfo.getCodeName()), pattern));
	}

	/**
	 * Returns all {@link DefaultExpression}s from all the {@link ClassInfo}s embedded in {@code exprInfo} that are valid.
	 *
	 * @param exprInfo The {@link SkriptParser.ExprInfo} to check for {@link DefaultExpression}s.
	 * @param pattern The pattern used to create {@link SkriptParser.ExprInfo}.
	 * @return All available {@link DefaultExpression}s.
	 * @throws SkriptAPIException If no {@link DefaultExpression}s are valid, produces an error message for the reasoning of failure.
	 */
	public static @NotNull List<DefaultExpression<?>> getDefaultExpressions(SkriptParser.ExprInfo exprInfo, String pattern) {
		if (exprInfo.classes.length == 1)
			return new ArrayList<>(List.of(getDefaultExpression(exprInfo, pattern)));

		DefaultValueData data = getParser().getData(DefaultValueData.class);

		EnumMap<DefaultExpressionUtils.DefaultExpressionError, List<String>> failed = new EnumMap<>(DefaultExpressionUtils.DefaultExpressionError.class);
		List<DefaultExpression<?>> passed = new ArrayList<>();
		for (int i = 0; i < exprInfo.classes.length; i++) {
			ClassInfo<?> classInfo = exprInfo.classes[i];
			DefaultExpression<?> expr = data.getDefaultValue(classInfo.getC());
			if (expr == null)
				expr = classInfo.getDefaultExpression();

			String codeName = classInfo.getCodeName();
			DefaultExpressionUtils.DefaultExpressionError errorType = DefaultExpressionUtils.isValid(expr, exprInfo, i);

			if (errorType != null) {
				failed.computeIfAbsent(errorType, list -> new ArrayList<>()).add(codeName);
			} else {
				passed.add(expr);
			}
		}

		if (!passed.isEmpty())
			return passed;

		List<String> errors = new ArrayList<>();
		for (Map.Entry<DefaultExpressionUtils.DefaultExpressionError, List<String>> entry : failed.entrySet()) {
			String error = entry.getKey().getError(entry.getValue(), pattern);
			errors.add(error);
		}
		throw new SkriptAPIException(StringUtils.join(errors, "\n"));
	}

	/**
	 * @see ParserInstance#get()
	 */
	protected static ParserInstance getParser() {
		return ParserInstance.get();
	}

}
