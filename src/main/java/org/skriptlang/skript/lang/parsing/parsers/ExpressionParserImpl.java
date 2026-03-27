package org.skriptlang.skript.lang.parsing.parsers;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.function.ExprFunctionCall;
import org.skriptlang.skript.common.function.FunctionReference;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ClassInfoReference;
import ch.njol.util.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.parsing.sites.ExpressionSite;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ExpressionParserImpl extends SyntaxParserImpl<ExpressionParserImpl> implements ExpressionParser<ExpressionParserImpl> {

    private final static String MULTIPLE_AND_OR = "List has multiple 'and' or 'or', will default to 'and'. Use brackets if you want to define multiple lists.";
    private final static String MISSING_AND_OR = "List is missing 'and' or 'or', defaulting to 'and'";

	protected ExpressionParserImpl(org.skriptlang.skript.Skript skript) {
		super(skript);
	}

	public ExpressionParserImpl(SyntaxParser<?> other) {
		super(other);
	}


	public final <T> @Nullable Expression<? extends T> parse() {
        return null;
    }

	@Override
    public <T> @Nullable Expression<? extends T> parse(ExpressionSite site) {
        if (input.isEmpty())
            return null;

        var types = site.getReturnTypes();

        assert types != null;
        assert !types.isEmpty();

        try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
            Expression<? extends T> parsedExpression = parseSingleExpr(site,true, null);
            if (parsedExpression != null) {
                log.printLog();
                return parsedExpression;
            }
            log.clear();

            return null; // this.parseExpressionList(log);
        }
    }

    /**
     * Helper method to parse the input as a variable, taking into account flags and context
     * @param log The log handler to use for logging errors
     * @return A {@link Result} object containing the parsed variable or null if parsing failed,
     * 			as well as a boolean indicating whether an error occurred
     */
    @Contract("_,_ -> new")
    private <T> @NotNull Result<Variable<? extends T>> parseAsVariable(
		ExpressionSite site,
		ParseLogHandler log
	) {
        // check if the context is valid for variable parsing
        if (context != ParseContext.DEFAULT && context != ParseContext.EVENT)
            return new Result<>(false, null);

        //noinspection unchecked
        Variable<? extends T> parsedVariable = (Variable<? extends T>) Variable.parse(input, getValidReturnTypes(site));
        if (parsedVariable != null) {
            if (!site.allowsNonLiterals()) {
                // TODO: this error pops up a lot when it isn't relevant, improve this
                Skript.error("Variables cannot be used here.");
                log.printError();
                return new Result<>(true, null);
            }
            log.printLog();
            return new Result<>(false, parsedVariable);
        } else if (log.hasError()) {
            log.printError();
            return new Result<>(true, null);
        }
        return new Result<>(false, null);
    }

    /**
     * Helper method to parse the input as a function, taking into account flags and context
     * @param log The log handler to use for logging errors
     * @return A {@link Result} object containing the parsed function or null if parsing failed,
     * 			as well as a boolean indicating whether an error occurred
     * @param <T> The supertype that the function is expected to return
     */
    @Contract("_,_ -> new")
    private <T> @NotNull Result<ExprFunctionCall<T>> parseAsFunction(
		ExpressionSite site,
		ParseLogHandler log
	) {
        // check if the context is valid for function parsing
        if (context != ParseContext.DEFAULT && context != ParseContext.EVENT)
            return new Result<>(false, null);

        FunctionReference<T> functionReference = new FunctionParserImpl(this).parse();
        if (functionReference != null) {
            log.printLog();
            return new Result<>(false, new ExprFunctionCall<>(functionReference));
        } else if (log.hasError()) {
            log.printError();
            return new Result<>(true, null);
        }
        return new Result<>(false, null);
    }

    /**
     * Helper method to parse the input as a non-literal expression, taking into account flags and context
     * @param log The log handler to use for logging errors
     * @return A {@link Result} object containing the parsed expression or null if parsing failed,
     * 			as well as a boolean indicating whether an error occurred
     * @param <T> The supertype that the expression is expected to return
     */
    @Contract("_,_ -> new")
    private <T> @NotNull Result<Expression<? extends T>> parseAsNonLiteral(
		ExpressionSite site,
		ParseLogHandler log
	) {
        if (!site.allowsNonLiterals())
            return new Result<>(false, null);

        Expression<? extends T> parsedExpression;
        if (input.startsWith("“") || input.startsWith("”") || input.endsWith("”") || input.endsWith("“")) {
            Skript.error("Pretty quotes are not allowed, change to regular quotes (\")");
            return new Result<>(true, null);
        }

        if (input.startsWith("\"") && input.length() != 1 && StringUtils.nextQuote(input, 1) == input.length() - 1) {
			// quoted string, strip quotes and parse as VariableString
            //noinspection unchecked
            return new Result<>(false, (Expression<? extends T>) VariableString.newInstance(input.substring(1, input.length() - 1)));
        } else {
			// parse generally
			//TODO: apply site constraints to parse
            //noinspection unchecked
            parsedExpression = (Expression<? extends T>) parse(SyntaxRegistry.EXPRESSION);
        }

        if (parsedExpression != null) {
			// Expression/VariableString parsing success

			// Verify return types
			// Assume parsedExpression returns X, Y, and Z
			// The accepted types are A, B, C
			//
			// We need to ensure that at least one of X/Y/Z is either A/B/C
			// AND
			// that any type that matches also matches in plurality
			// e.g. if we accept a list of strings but a singular number, then
			//      if parsedExpression is plural, it can only return strings.
			//      if it returns number or any superclass of number, it is only allowed
			//      to return a single value.


			List<ClassInfoReference> wrongPlurality = new ArrayList<>();
            Class<?> parsedReturnType = parsedExpression.getReturnType();
            for (ClassInfoReference infoReference : site.getReturnTypes()) {
                Class<?> type = infoReference.getClassInfo().getC();
				// check types
				if (type.isAssignableFrom(parsedReturnType)) {
					continue;
                }
				// check plurality
				boolean single = infoReference.isPlural().isFalse();
				if (single && !parsedExpression.isSingle()) {
					break;
				}
				log.printLog();
				return new Result<>(false, parsedExpression);
            }

			// if any types matched in


            // No directly same type found
            //noinspection unchecked
            Class<T>[] objTypes = (Class<T>[]) getValidReturnTypes(site);
            Expression<? extends T> convertedExpression = parsedExpression.getConvertedExpression(objTypes);
            if (convertedExpression != null) {
                log.printLog();
                return new Result<>(false, convertedExpression);
            }
            // Print errors, if we couldn't get the correct type
            log.printError(parsedExpression.toString(null, false) + " " + Language.get("is") + " " +
                    SkriptParser.notOfType(getValidReturnTypes(site)), ErrorQuality.NOT_AN_EXPRESSION);
            return new Result<>(true, null);
        }
        return new Result<>(false, null);
    }

    private static final String INVALID_LSPEC_CHARS = "[^,():/\"'\\[\\]}{]";
    private static final Pattern LITERAL_SPECIFICATION_PATTERN = Pattern.compile("(?<literal>" + INVALID_LSPEC_CHARS + "+) \\((?<classinfo>[\\w\\p{L} ]+)\\)");

    /**
     * Helper method to parse the input as a literal expression, taking into account flags and context
     * @param log The log handler to use for logging errors
     * @return A {@link Result} object containing the parsed expression or null if parsing failed,
     * 			as well as a boolean indicating whether an error occurred
     * @param <T> The supertype that the expression is expected to return
     */
    @Contract("_,_,_,_ -> new")
    private <T> @NotNull Result<Expression<? extends T>> parseAsLiteral(
		ExpressionSite site,
		ParseLogHandler log,
		boolean allowUnparsedLiteral,
		@Nullable LogEntry error) {
        if (!site.allowsLiterals())
            return new Result<>(false, null);

        // specified literal
        if (input.endsWith(")") && input.contains("(")) {
            Matcher classInfoMatcher = LITERAL_SPECIFICATION_PATTERN.matcher(input);
            if (classInfoMatcher.matches()) {
                String literalString = classInfoMatcher.group("literal");
                String unparsedClassInfo = Noun.stripDefiniteArticle(classInfoMatcher.group("classinfo"));
                Expression<? extends T> result = parseSpecifiedLiteral(site, literalString, unparsedClassInfo);
                if (result != null) {
                    log.printLog();
                    return new Result<>(false, result);
                }
            }
        }
        // if target is just Object.class, we can use unparsed literal.
        Class<?>[] types = getValidReturnTypes(site);
        if (types.length == 1 && types[0] == Object.class) {
            if (!allowUnparsedLiteral) {
                log.printError();
                return new Result<>(true, null);
            }
            //noinspection unchecked
            return new Result<>(false, (Expression<? extends T>) getUnparsedLiteral(log, error));
        }

        // attempt more specific parsing
        boolean containsObjectClass = false;
        for (Class<?> type : types) {
            log.clear();
            if (type == Object.class) {
                // If 'Object.class' is an option, needs to be treated as previous behavior
                // But we also want to be sure every other 'ClassInfo' is attempted to be parsed beforehand
                containsObjectClass = true;
                continue;
            }
            //noinspection unchecked
            T parsedObject = (T) Classes.parse(input, type, context);
            if (parsedObject != null) {
                log.printLog();
                return new Result<>(false, new SimpleLiteral<>(parsedObject, false));
            }
        }
        if (allowUnparsedLiteral && containsObjectClass)
            //noinspection unchecked
            return new Result<>(false, (Expression<? extends T>) getUnparsedLiteral(log, error));

        // literal string
        if (input.startsWith("\"") && input.endsWith("\"") && input.length() > 1) {
            for (Class<?> type : types) {
                if (!type.isAssignableFrom(String.class))
                    continue;
                VariableString string = VariableString.newInstance(input.substring(1, input.length() - 1));
                if (string instanceof LiteralString)
                    //noinspection unchecked
                    return new Result<>(false, (Expression<? extends T>) string);
                break;
            }
        }
        log.printError();
        return new Result<>(false, null);
    }

    /**
     * If {@link #input} is a valid literal expression, will return {@link UnparsedLiteral}.
     * @param log The current {@link ParseLogHandler}.
     * @param error A {@link LogEntry} containing a default error to be printed if failed to retrieve.
     * @return {@link UnparsedLiteral} or {@code null}.
     */
    private @Nullable UnparsedLiteral getUnparsedLiteral(
            ParseLogHandler log,
            @Nullable LogEntry error
    )  {
        // Do check if a literal with this name actually exists before returning an UnparsedLiteral
        if (Classes.parseSimple(input, Object.class, context) == null) {
            log.printError();
            return null;
        }
        log.clear();
        LogEntry logError = log.getError();
        return new UnparsedLiteral(input, logError != null && (error == null || logError.quality > error.quality) ? logError : error);
    }

    /**
     * <p>
     *     With ambiguous literals being used in multiple {@link ClassInfo}s, users can specify which one they want
     *     in the format of 'literal (classinfo)'; Example: black (wolf variant)
     *     This checks to ensure the given 'classinfo' exists, is parseable, and is of the accepted types that is required.
     *     If so, the literal section of the input is parsed as the given classinfo and the result returned.
     * </p>
     * @param literalString A {@link String} representing a literal
     * @param unparsedClassInfo A {@link String} representing a class info
     * @return {@link SimpleLiteral} or {@code null} if any checks fail
     */
    private <T> @Nullable Expression<? extends T> parseSpecifiedLiteral(
			ExpressionSite site,
            String literalString,
            String unparsedClassInfo
    ) {
        ClassInfo<?> classInfo = Classes.parse(unparsedClassInfo, ClassInfo.class, context);
        if (classInfo == null) {
            Skript.error("A " + unparsedClassInfo  + " is not a valid type.");
            return null;
        }
        ch.njol.skript.classes.Parser<?> classInfoParser = classInfo.getParser();
        if (classInfoParser == null || !classInfoParser.canParse(context)) {
            Skript.error("A " + unparsedClassInfo  + " cannot be parsed.");
            return null;
        }
        if (!checkAcceptedType(classInfo.getC(), getValidReturnTypes(site))) {
            Skript.error(input + " " + Language.get("is") + " " + SkriptParser.notOfType(getValidReturnTypes(site)));
            return null;
        }
        //noinspection unchecked
        T parsedObject = (T) classInfoParser.parse(literalString, context);
        if (parsedObject != null)
            return new SimpleLiteral<>(parsedObject, false, new UnparsedLiteral(literalString));
        return null;
    }

    /**
     * Extracts the valid return types from an {@link ExpressionSite} as a {@code Class<?>[]} array.
     */
    private Class<?>[] getValidReturnTypes(ExpressionSite site) {
        return site.getReturnTypes().stream()
            .map(ref -> ref.getClassInfo().getC())
            .toArray(Class[]::new);
    }

    /**
     * Check if the provided {@code clazz} is an accepted type from any class of {@code types}.
     * @param clazz The {@link Class} to check
     * @param types The {@link Class}es that are accepted
     * @return true if {@code clazz} is of a {@link Class} from {@code types}
     */
    private boolean checkAcceptedType(Class<?> clazz, Class<?> ... types) {
        for (Class<?> targetType : types) {
            if (targetType.isAssignableFrom(clazz))
                return true;
        }
        return false;
    }

    /**
     * Parses the input as a singular expression that has a return type matching one of the given types.
     * @param allowUnparsedLiteral Whether to allow unparsed literals to be returned
     * @param defaultError The default error to log if the expression cannot be parsed
     * @return The parsed expression, or null if the given input could not be parsed as an expression
     * @param <T> The return supertype of the expression
     */
    private <T> @Nullable Expression<? extends T> parseSingleExpr(
			ExpressionSite site,
            boolean allowUnparsedLiteral,
            @Nullable LogEntry defaultError
    ) {
        if (input.isEmpty())
            return null;

        // strip "(" and ")" from the input if the input is properly enclosed
        // do not do this for COMMAND or PARSE context for some reason
        if (context != ParseContext.COMMAND
                && context != ParseContext.PARSE
                && input.startsWith("(") && input.endsWith(")")
                && ch.njol.skript.lang.SkriptParser.next(input, 0, context) == input.length()
        ) {
            return new ExpressionParserImpl(this)
					.input(input.substring(1, input.length() - 1))
                    .parseSingleExpr(site, allowUnparsedLiteral, defaultError);
        }

        try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
            // attempt to parse the input as a variable
            Result<Variable<? extends T>> variableResult = parseAsVariable(site, log);
            if (variableResult.error() || variableResult.value() != null)
                return variableResult.value();
            log.clear();

            // attempt to parse the input as a function
            Result<ExprFunctionCall<T>> functionResult = parseAsFunction(site, log);
            if (functionResult.error() || functionResult.value() != null)
                return functionResult.value();
            log.clear();

            // attempt to parse the input as a non-literal expression
            Result<Expression<? extends T>> expressionResult = parseAsNonLiteral(site, log);
            if (expressionResult.error() || expressionResult.value() != null)
                return expressionResult.value();
            log.clear();

            // attempt to parse the input as a literal expression
            Result<Expression<? extends T>> literalResult = parseAsLiteral(site, log, allowUnparsedLiteral, defaultError);
            if (literalResult.error() || literalResult.value() != null)
                return literalResult.value();
            log.clear();

            // if all parsing attempts failed, return null
            log.printLog();
            return null;
        }
    }

	@Override
	public <E extends SyntaxElement> @Nullable E parse(@NotNull Iterator<? extends SyntaxInfo<? extends E>> candidateSyntaxes) {
		return null;
	}

	// todo: list parsing

    /**
     * A record that contains internal information about the success of a single parsing operation, to facilitate helper methods.
     * Not to be confused with {@link ch.njol.skript.lang.SkriptParser.ParseResult}, which contains information about the parsing itself.
     * @param error Whether the parsing encountered an error and should exit.
     * @param value The value that was parsed, or null if the parsing failed.
     * @param <T> The type of the value that was parsed.
     */
    protected record Result<T>(boolean error, @Nullable T value) { }
}
