package org.skriptlang.skript.lang.parser;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.function.ExprFunctionCall;
import ch.njol.skript.lang.function.FunctionReference;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionParser extends SkriptParser {

    private final static String MULTIPLE_AND_OR = "List has multiple 'and' or 'or', will default to 'and'. Use brackets if you want to define multiple lists.";
    private final static String MISSING_AND_OR = "List is missing 'and' or 'or', defaulting to 'and'";

    public ExpressionParser(String input) {
        super(input);
    }

    public ExpressionParser(String input, ParsingConstraints constraints) {
        super(input, constraints);
    }

    public ExpressionParser(@NotNull String input, ParsingConstraints constraints, ParseContext context) {
        super(input, constraints, context);
    }

    public ExpressionParser(SkriptParser skriptParser, String input) {
        super(skriptParser, input);
    }

    public final <T> @Nullable Expression<? extends T> parse() {
        if (input.isEmpty())
            return null;

        var types = parsingConstraints.getValidReturnTypes();

        assert types != null;
        assert types.length > 0;
        assert types.length == 1 || !CollectionUtils.contains(types, Object.class);

        ParseLogHandler log = SkriptLogger.startParseLogHandler();
        try {
            Expression<? extends T> parsedExpression = parseSingleExpr(true, null);
            if (parsedExpression != null) {
                log.printLog();
                return parsedExpression;
            }
            log.clear();

            return this.parseExpressionList(log);
        } finally {
            log.stop();
        }
    }

    /**
     * Helper method to parse the input as a variable, taking into account flags and context
     * @param log The log handler to use for logging errors
     * @return A {@link Result} object containing the parsed variable or null if parsing failed,
     * 			as well as a boolean indicating whether an error occurred
     */
    @Contract("_ -> new")
    private <T> @NotNull Result<Variable<? extends T>> parseAsVariable(ParseLogHandler log) {
        // check if the context is valid for variable parsing
        if (context != ParseContext.DEFAULT && context != ParseContext.EVENT)
            return new Result<>(false, null);

        //noinspection unchecked
        Variable<? extends T> parsedVariable = (Variable<? extends T>) Variable.parse(input, parsingConstraints.getValidReturnTypes());
        if (parsedVariable != null) {
            if (!parsingConstraints.allowsNonLiterals()) {
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
    @Contract("_ -> new")
    private <T> @NotNull Result<ExprFunctionCall<T>> parseAsFunction(ParseLogHandler log) {
        // check if the context is valid for function parsing
        if (!parsingConstraints.allowsFunctionCalls() || context != ParseContext.DEFAULT && context != ParseContext.EVENT)
            return new Result<>(false, null);

        FunctionReference<T> functionReference = new FunctionParser(this).parse();
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
    @Contract("_ -> new")
    private <T> @NotNull Result<Expression<? extends T>> parseAsNonLiteral(ParseLogHandler log) {
        if (!parsingConstraints.allowsNonLiterals())
            return new Result<>(false, null);

        Expression<? extends T> parsedExpression;
        if (input.startsWith("“") || input.startsWith("”") || input.endsWith("”") || input.endsWith("“")) {
            Skript.error("Pretty quotes are not allowed, change to regular quotes (\")");
            return new Result<>(true, null);
        }
        // quoted string, strip quotes and parse as VariableString
        if (input.startsWith("\"") && input.length() != 1 && StringUtils.nextQuote(input, 1) == input.length() - 1) {
            //noinspection unchecked
            return new Result<>(false, (Expression<? extends T>) VariableString.newInstance(input.substring(1, input.length() - 1)));
        } else {
            //noinspection unchecked
            parsedExpression = (Expression<? extends T>) parse(SyntaxRegistry.EXPRESSION);
        }

        if (parsedExpression != null) { // Expression/VariableString parsing success
            Class<?> parsedReturnType = parsedExpression.getReturnType();
            for (Class<?> type : parsingConstraints.getValidReturnTypes()) {
                if (type.isAssignableFrom(parsedReturnType)) {
                    log.printLog();
                    return new Result<>(false, parsedExpression);
                }
            }

            // No directly same type found
            //noinspection unchecked
            Class<T>[] objTypes = (Class<T>[]) parsingConstraints.getValidReturnTypes();
            Expression<? extends T> convertedExpression = parsedExpression.getConvertedExpression(objTypes);
            if (convertedExpression != null) {
                log.printLog();
                return new Result<>(false, convertedExpression);
            }
            // Print errors, if we couldn't get the correct type
            log.printError(parsedExpression.toString(null, false) + " " + Language.get("is") + " " +
                    notOfType(parsingConstraints.getValidReturnTypes()), ErrorQuality.NOT_AN_EXPRESSION);
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
    @Contract("_,_,_ -> new")
    private <T> @NotNull Result<Expression<? extends T>> parseAsLiteral(ParseLogHandler log, boolean allowUnparsedLiteral, @Nullable LogEntry error) {
        if (!parsingConstraints.allowsLiterals())
            return new Result<>(false, null);

        // specified literal
        if (input.endsWith(")") && input.contains("(")) {
            Matcher classInfoMatcher = LITERAL_SPECIFICATION_PATTERN.matcher(input);
            if (classInfoMatcher.matches()) {
                String literalString = classInfoMatcher.group("literal");
                String unparsedClassInfo = Noun.stripDefiniteArticle(classInfoMatcher.group("classinfo"));
                Expression<? extends T> result = parseSpecifiedLiteral(literalString, unparsedClassInfo);
                if (result != null) {
                    log.printLog();
                    return new Result<>(false, result);
                }
            }
        }
        // if target is just Object.class, we can use unparsed literal.
        Class<?>[] types = parsingConstraints.getValidReturnTypes();
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
            String literalString,
            String unparsedClassInfo
    ) {
        ClassInfo<?> classInfo = Classes.parse(unparsedClassInfo, ClassInfo.class, context);
        if (classInfo == null) {
            Skript.error("A " + unparsedClassInfo  + " is not a valid type.");
            return null;
        }
        Parser<?> classInfoParser = classInfo.getParser();
        if (classInfoParser == null || !classInfoParser.canParse(context)) {
            Skript.error("A " + unparsedClassInfo  + " cannot be parsed.");
            return null;
        }
        if (!checkAcceptedType(classInfo.getC(), parsingConstraints.getValidReturnTypes())) {
            Skript.error(input + " " + Language.get("is") + " " + notOfType(parsingConstraints.getValidReturnTypes()));
            return null;
        }
        //noinspection unchecked
        T parsedObject = (T) classInfoParser.parse(literalString, context);
        if (parsedObject != null)
            return new SimpleLiteral<>(parsedObject, false, new UnparsedLiteral(literalString));
        return null;
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
            return new ExpressionParser(this, input.substring(1, input.length() - 1))
                    .parseSingleExpr(allowUnparsedLiteral, defaultError);
        }

        try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
            // attempt to parse the input as a variable
            Result<Variable<? extends T>> variableResult = parseAsVariable(log);
            if (variableResult.error() || variableResult.value() != null)
                return variableResult.value();
            log.clear();

            // attempt to parse the input as a function
            Result<ExprFunctionCall<T>> functionResult = parseAsFunction(log);
            if (functionResult.error() || functionResult.value() != null)
                return functionResult.value();
            log.clear();

            // attempt to parse the input as a non-literal expression
            Result<Expression<? extends T>> expressionResult = parseAsNonLiteral(log);
            if (expressionResult.error() || expressionResult.value() != null)
                return expressionResult.value();
            log.clear();

            // attempt to parse the input as a literal expression
            Result<Expression<? extends T>> literalResult = parseAsLiteral(log, allowUnparsedLiteral, defaultError);
            if (literalResult.error() || literalResult.value() != null)
                return literalResult.value();
            log.clear();

            // if all parsing attempts failed, return null
            log.printLog();
            return null;
        }
    }

	/*

	 * List parsing

	private record OrderedExprInfo(ExprInfo[] infos) { }

	@SafeVarargs
	private <T> @Nullable Expression<? extends T> parseExpressionList(ParseLogHandler log, Class<? extends T>... types) {
		//noinspection unchecked
		return (Expression<? extends T>) parseExpressionList_i(log, types);
	}

	private @Nullable Expression<?> parseExpressionList(ParseLogHandler log, ExprInfo info) {
		return parseExpressionList_i(log, info);
	}

	private @Nullable Expression<?> parseExpressionList(ParseLogHandler log, OrderedExprInfo info) {
		return parseExpressionList_i(log, info);
	}

	private @Nullable Expression<?> parseExpressionList_i(ParseLogHandler log, Object data) {
	 */

    <T> @Nullable Expression<? extends T> parseExpressionList(ParseLogHandler log) {
//        var types = parsingConstraints.getValidReturnTypes();
//        boolean isObject = types.length == 1 && types[0] == Object.class;
//        List<Expression<? extends T>> parsedExpressions = new ArrayList<>();
//        Kleenean and = Kleenean.UNKNOWN;
//        boolean isLiteralList = true;
//        Expression<? extends T> parsedExpression;
//
//        List<int[]> pieces = new ArrayList<>();
//        {
//            Matcher matcher = LIST_SPLIT_PATTERN.matcher(input);
//            int i = 0, j = 0;
//            for (; i >= 0 && i <= input.length(); i = StringUtils.next(input, i, context)) {
//                if (i == input.length() || matcher.region(i, input.length()).lookingAt()) {
//                    pieces.add(new int[] {j, i});
//                    if (i == input.length())
//                        break;
//                    j = i = matcher.end();
//                }
//            }
//            if (i != input.length()) {
//                assert i == -1 && context != ParseContext.COMMAND && context != ParseContext.PARSE : i + "; " + input;
//                log.printError("Invalid brackets/variables/text in '" + input + "'", ErrorQuality.NOT_AN_EXPRESSION);
//                return null;
//            }
//        }
//
//        if (pieces.size() == 1) { // not a list of expressions, and a single one has failed to parse above
//            if (input.startsWith("(") && input.endsWith(")") && StringUtils.next(input, 0, context) == input.length()) {
//                log.clear();
//                return new ExpressionParser(this, input.substring(1, input.length() - 1)).parse();
//            }
//            if (isObject && parsingConstraints.allowsLiterals()) { // single expression - can return an UnparsedLiteral now
//                log.clear();
//                //noinspection unchecked
//                return (Expression<? extends T>) new UnparsedLiteral(input, log.getError());
//            }
//            // results in useless errors most of the time
////				log.printError("'" + input + "' " + Language.get("is") + " " + notOfType(types), ErrorQuality.NOT_AN_EXPRESSION);
//            log.printError();
//            return null;
//        }
//
//        outer: for (int first = 0; first < pieces.size();) {
//            for (int last = 1; last <= pieces.size() - first; last++) {
//                if (first == 0 && last == pieces.size()) // i.e. the whole expression - already tried to parse above
//                    continue;
//                int start = pieces.get(first)[0], end = pieces.get(first + last - 1)[1];
//                String subExpr = input.substring(start, end).trim();
//                assert subExpr.length() < input.length() : subExpr;
//
//                if (subExpr.startsWith("(") && subExpr.endsWith(")") && StringUtils.next(subExpr, 0, context) == subExpr.length())
//                    parsedExpression = new ExpressionParser(this, subExpr).parse(); // only parse as possible expression list if its surrounded by brackets
//                else
//                    parsedExpression = new ExpressionParser(this, subExpr).parseSingleExpr(last == 1, log.getError()); // otherwise parse as a single expression only
//                if (parsedExpression != null) {
//                    isLiteralList &= parsedExpression instanceof Literal;
//                    parsedExpressions.add(parsedExpression);
//                    if (first != 0) {
//                        String delimiter = input.substring(pieces.get(first - 1)[1], start).trim().toLowerCase(Locale.ENGLISH);
//                        if (!delimiter.equals(",")) {
//                            boolean or = !delimiter.contains("nor") && delimiter.endsWith("or");
//                            if (and.isUnknown()) {
//                                and = Kleenean.get(!or); // nor is and
//                            } else {
//                                if (and != Kleenean.get(!or)) {
//                                    Skript.warning(MULTIPLE_AND_OR + " List: " + input);
//                                    and = Kleenean.TRUE;
//                                }
//                            }
//                        }
//                    }
//                    first += last;
//                    continue outer;
//                }
//            }
//            log.printError();
//            return null;
//        }
//
//        log.printLog(false);
//
//        if (parsedExpressions.size() == 1)
//            return parsedExpressions.get(0);
//
//        if (and.isUnknown() && !suppressMissingAndOrWarnings) {
//            ParserInstance parser = getParser();
//            Script currentScript = parser.isActive() ? parser.getCurrentScript() : null;
//            if (currentScript == null || !currentScript.suppressesWarning(ScriptWarning.MISSING_CONJUNCTION))
//                Skript.warning(MISSING_AND_OR + ": " + input);
//        }
//
//        Class<? extends T>[] exprReturnTypes = new Class[parsedExpressions.size()];
//        for (int i = 0; i < parsedExpressions.size(); i++)
//            exprReturnTypes[i] = parsedExpressions.get(i).getReturnType();
//
//        if (isLiteralList) {
//            //noinspection unchecked,SuspiciousToArrayCall
//            Literal<T>[] literals = parsedExpressions.toArray(new Literal[0]);
//            //noinspection unchecked
//            return new LiteralList<>(literals, (Class<T>) Classes.getSuperClassInfo(exprReturnTypes).getC(), exprReturnTypes, !and.isFalse());
//        } else {
//            //noinspection unchecked
//            Expression<T>[] expressions = parsedExpressions.toArray(new Expression[0]);
//            //noinspection unchecked
//            return new ExpressionList<>(expressions, (Class<T>) Classes.getSuperClassInfo(exprReturnTypes).getC(), exprReturnTypes, !and.isFalse());
//        }
        return null;
    }

    /**
     * A record that contains internal information about the success of a single parsing operation, to facilitate helper methods.
     * Not to be confused with {@link ParseResult}, which contains information about the parsing itself.
     * @param error Whether the parsing encountered an error and should exit.
     * @param value The value that was parsed, or null if the parsing failed.
     * @param <T> The type of the value that was parsed.
     */
    protected record Result<T>(boolean error, @Nullable T value) { }
}
