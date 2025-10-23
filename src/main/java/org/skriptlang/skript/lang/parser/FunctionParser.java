package org.skriptlang.skript.lang.parser;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.function.FunctionReference;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FunctionParser extends SkriptParser {

	private final static Pattern FUNCTION_CALL_PATTERN = Pattern.compile("(" + Functions.functionNamePattern + ")\\((.*)\\)");

	public FunctionParser(String input) {
		super(input);
	}

	public FunctionParser(String input, ParsingConstraints constraints) {
		super(input, constraints);
	}

	public FunctionParser(@NotNull String input, ParsingConstraints constraints, ParseContext context) {
		super(input, constraints, context);
	}

	public FunctionParser(@NotNull SkriptParser other) {
		super(other);
	}

	public final <T> @Nullable FunctionReference<T> parse() {
		if (context != ParseContext.DEFAULT && context != ParseContext.EVENT)
			return null;
		var returnTypes = parsingConstraints.getValidReturnTypes();
		AtomicBoolean unaryArgument = new AtomicBoolean(false);
		try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
			Matcher matcher = FUNCTION_CALL_PATTERN.matcher(input);
			if (!matcher.matches()) {
				log.printLog();
				return null;
			}

			String functionName = matcher.group(1);
			String args = matcher.group(2);
			Expression<?>[] params;

			// Check for incorrect quotes, e.g. "myFunction() + otherFunction()" being parsed as one function
			// See https://github.com/SkriptLang/Skript/issues/1532
			for (int i = 0; i < args.length(); i = ch.njol.skript.lang.SkriptParser.next(args, i, context)) {
				if (i == -1) {
					log.printLog();
					return null;
				}
			}

			if (!parsingConstraints.allowsNonLiterals()) {
				Skript.error("Functions cannot be used here (or there is a problem with your arguments).");
				log.printError();
				return null;
			}
			ExpressionParser exprParser = new ExpressionParser(args,
				parsingConstraints.copy()
					.allowLiterals(true)
					.constrainReturnTypes(Object.class),
				context);
			exprParser.suppressMissingAndOrWarnings();

			params = this.getFunctionArguments(exprParser::parse, args, unaryArgument);
			if (params == null) {
				log.printError();
				return null;
			}

			ParserInstance parser = getParser();
			Script currentScript = parser.isActive() ? parser.getCurrentScript() : null;
			//noinspection unchecked
			FunctionReference<T> functionReference = (FunctionReference<T>) new FunctionReference<>(functionName, SkriptLogger.getNode(),
				currentScript != null ? currentScript.getConfig().getFileName() : null, returnTypes, params);//.toArray(new Expression[params.size()]));
			attempt_list_parse:
			if (unaryArgument.get() && !functionReference.validateParameterArity(true)) {
				try (ParseLogHandler ignored = SkriptLogger.startParseLogHandler()) {

					exprParser.suppressMissingAndOrWarnings();
					params = this.getFunctionArguments(() -> exprParser.parseExpressionList(ignored), args, unaryArgument);
					ignored.clear();
					if (params == null)
						break attempt_list_parse;
				}
				//noinspection unchecked
				functionReference = (FunctionReference<T>) new FunctionReference<>(functionName, SkriptLogger.getNode(),
					currentScript != null ? currentScript.getConfig().getFileName() : null, returnTypes, params);
			}
			if (!functionReference.validateFunction(true)) {
				log.printError();
				return null;
			}
			log.printLog();
			return functionReference;
		}
	}

	private Expression<?> @Nullable [] getFunctionArguments(Supplier<Expression<?>> parsing, @NotNull String args, AtomicBoolean unary) {
		Expression<?>[] params;
		if (!args.isEmpty()) {
			Expression<?> parsedExpression = parsing.get();
			if (parsedExpression == null)
				return null;
			if (parsedExpression instanceof ExpressionList<?> expressionList) {
				if (!parsedExpression.getAnd()) {
					Skript.error("Function arguments must be separated by commas and optionally an 'and', but not an 'or'."
						+ " Put the 'or' into a second set of parentheses if you want to make it a single parameter, e.g. 'give(player, (sword or axe))'");
					return null;
				}
				params = expressionList.getExpressions();
			} else {
				unary.set(true);
				params = new Expression[] {parsedExpression};
			}
		} else {
			params = new Expression[0];
		}
		return params;
	}

}
