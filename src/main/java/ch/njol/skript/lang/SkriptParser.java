/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.command.Argument;
import ch.njol.skript.command.Commands;
import ch.njol.skript.command.ScriptCommand;
import ch.njol.skript.command.ScriptCommandEvent;
import ch.njol.skript.expressions.ExprParse;
import ch.njol.skript.lang.function.ExprFunctionCall;
import ch.njol.skript.lang.function.FunctionReference;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.patterns.MalformedPatternException;
import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.patterns.SkriptPattern;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.primitives.Booleans;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.script.ScriptWarning;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

/**
 * Used for parsing my custom patterns.<br>
 * <br>
 * Note: All parse methods print one error at most xor any amount of warnings and lower level log messages. If the given string doesn't match any pattern then nothing is printed.
 * 
 * @author Peter Güttinger
 */
public class SkriptParser {
	
	final String expr;
	
	public final static int PARSE_EXPRESSIONS = 1;
	public final static int PARSE_LITERALS = 2;
	public final static int ALL_FLAGS = PARSE_EXPRESSIONS | PARSE_LITERALS;
	private final int flags;
	
	public final ParseContext context;
	
	public SkriptParser(final String expr) {
		this(expr, ALL_FLAGS);
	}
	
	public SkriptParser(final String expr, final int flags) {
		this(expr, flags, ParseContext.DEFAULT);
	}
	
	/**
	 * Constructs a new SkriptParser object that can be used to parse the given expression.
	 * <p>
	 * A SkriptParser can be re-used indefinitely for the given expression, but to parse a new expression a new SkriptParser has to be created.
	 * 
	 * @param expr The expression to parse
	 * @param flags Some parse flags ({@link #PARSE_EXPRESSIONS}, {@link #PARSE_LITERALS})
	 * @param context The parse context
	 */
	public SkriptParser(final String expr, final int flags, final ParseContext context) {
		assert expr != null;
		assert (flags & ALL_FLAGS) != 0;
		this.expr = "" + expr.trim();
		this.flags = flags;
		this.context = context;
	}
	
	public SkriptParser(final SkriptParser other, final String expr) {
		this(expr, other.flags, other.context);
	}
	
	public final static String wildcard = "[^\"]*?(?:\"[^\"]*?\"[^\"]*?)*?";
	public final static String stringMatcher = "\"[^\"]*?(?:\"\"[^\"]*)*?\"";
	
	public final static class ParseResult {
		public final Expression<?>[] exprs;
		public final List<MatchResult> regexes = new ArrayList<>(1);
		public String expr;
		/**
		 * Defaults to 0. Any marks encountered in the pattern will be XORed with the existing value, in particular if only one mark is encountered this value will be set to that
		 * mark.
		 */
		public int mark = 0;
		public List<String> tags = new ArrayList<>();
		
		public ParseResult(final SkriptParser parser, final String pattern) {
			expr = parser.expr;
			exprs = new Expression<?>[countUnescaped(pattern, '%') / 2];
		}

		public ParseResult(String expr, Expression<?>[] expressions) {
			this.expr = expr;
			this.exprs = expressions;
		}

		public boolean hasTag(String tag) {
			return tags.contains(tag);
		}
	}

	/**
	 * Parses a single literal, i.e. not lists of literals.
	 * <p>
	 * Prints errors.
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public static <T> Literal<? extends T> parseLiteral(String expr, final Class<T> c, final ParseContext context) {
		expr = "" + expr.trim();
		if (expr.isEmpty())
			return null;
		return new UnparsedLiteral(expr).getConvertedExpression(context, c);
	}
	
	/**
	 * Parses a string as one of the given syntax elements.
	 * <p>
	 * Can print an error.
	 */
	@Nullable
	public static <T extends SyntaxElement> T parse(String expr, final Iterator<? extends SyntaxElementInfo<T>> source, final @Nullable String defaultError) {
		expr = "" + expr.trim();
		if (expr.isEmpty()) {
			Skript.error(defaultError);
			return null;
		}
		final ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			final T e = new SkriptParser(expr).parse(source);
			if (e != null) {
				log.printLog();
				return e;
			}
			log.printError(defaultError);
			return null;
		} finally {
			log.stop();
		}
	}
	
	@Nullable
	public static <T extends SyntaxElement> T parseStatic(String expr, Iterator<? extends SyntaxElementInfo<? extends T>> source, @Nullable String defaultError) {
		return parseStatic(expr, source, ParseContext.DEFAULT, defaultError);
	}

	@Nullable
	public static <T extends SyntaxElement> T parseStatic(String expr, Iterator<? extends SyntaxElementInfo<? extends T>> source, ParseContext parseContext, @Nullable String defaultError) {
		expr = expr.trim();
		if (expr.isEmpty()) {
			Skript.error(defaultError);
			return null;
		}

		ParseLogHandler log = SkriptLogger.startParseLogHandler();
		T e;
		try {
			e = new SkriptParser(expr, PARSE_LITERALS, parseContext).parse(source);
			if (e != null) {
				log.printLog();
				return e;
			}
			log.printError(defaultError);
			return null;
		} finally {
			log.stop();
		}
	}

	@Nullable
	private <T extends SyntaxElement> T parse(Iterator<? extends SyntaxElementInfo<? extends T>> source) {
		ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			while (source.hasNext()) {
				SyntaxElementInfo<? extends T> info = source.next();
				patternsLoop: for (int i = 0; i < info.patterns.length; i++) {
					log.clear();
					try {
						String pattern = info.patterns[i];
						assert pattern != null;
						ParseResult res;
						try {
							res = parse_i(pattern, 0, 0);
						} catch (MalformedPatternException e) {
							String message = "pattern compiling exception, element class: " + info.c.getName();
							try {
								JavaPlugin providingPlugin = JavaPlugin.getProvidingPlugin(info.c);
								message += " (provided by " + providingPlugin.getName() + ")";
							} catch (IllegalArgumentException | IllegalStateException ignored) {}
							throw new RuntimeException(message, e);

						}
						if (res != null) {
							int x = -1;
							for (int j = 0; (x = nextUnescaped(pattern, '%', x + 1)) != -1; j++) {
								int x2 = nextUnescaped(pattern, '%', x + 1);
								if (res.exprs[j] == null) {
									String name = pattern.substring(x + 1, x2);
									if (!name.startsWith("-")) {
										ExprInfo vi = getExprInfo(name);
										DefaultExpression<?> expr = vi.classes[0].getDefaultExpression();
										if (expr == null)
											throw new SkriptAPIException("The class '" + vi.classes[0].getCodeName() + "' does not provide a default expression. Either allow null (with %-" + vi.classes[0].getCodeName() + "%) or make it mandatory [pattern: " + info.patterns[i] + "]");
										if (!(expr instanceof Literal) && (vi.flagMask & PARSE_EXPRESSIONS) == 0)
											throw new SkriptAPIException("The default expression of '" + vi.classes[0].getCodeName() + "' is not a literal. Either allow null (with %-*" + vi.classes[0].getCodeName() + "%) or make it mandatory [pattern: " + info.patterns[i] + "]");
										if (expr instanceof Literal && (vi.flagMask & PARSE_LITERALS) == 0)
											throw new SkriptAPIException("The default expression of '" + vi.classes[0].getCodeName() + "' is a literal. Either allow null (with %-~" + vi.classes[0].getCodeName() + "%) or make it mandatory [pattern: " + info.patterns[i] + "]");
										if (!vi.isPlural[0] && !expr.isSingle())
											throw new SkriptAPIException("The default expression of '" + vi.classes[0].getCodeName() + "' is not a single-element expression. Change your pattern to allow multiple elements or make the expression mandatory [pattern: " + info.patterns[i] + "]");
										if (vi.time != 0 && !expr.setTime(vi.time))
											throw new SkriptAPIException("The default expression of '" + vi.classes[0].getCodeName() + "' does not have distinct time states. [pattern: " + info.patterns[i] + "]");
										if (!expr.init())
											continue patternsLoop;
										res.exprs[j] = expr;
									}
								}
								x = x2;
							}
							T t = info.c.newInstance();
							if (t.init(res.exprs, i, getParser().getHasDelayBefore(), res)) {
								log.printLog();
								return t;
							}
						}
					} catch (final InstantiationException | IllegalAccessException e) {
						assert false;
					}
				}
			}
			log.printError();
			return null;
		} finally {
			log.stop();
		}
	}
	
	@SuppressWarnings("null")
	private final static Pattern varPattern = Pattern.compile("((the )?var(iable)? )?\\{.+\\}", Pattern.CASE_INSENSITIVE);
	
	/**
	 * Prints errors
	 */
	@Nullable
	private static <T> Variable<T> parseVariable(final String expr, final Class<? extends T>[] returnTypes) {
		if (varPattern.matcher(expr).matches()) {
			String variableName = "" + expr.substring(expr.indexOf('{') + 1, expr.lastIndexOf('}'));
			boolean inExpression = false;
			int variableDepth = 0;
			for (char c : variableName.toCharArray()) {
				if (c == '%' && variableDepth == 0)
					inExpression = !inExpression;
				if (inExpression) {
					if (c == '{') {
						variableDepth++;
					} else if (c == '}')
						variableDepth--;
				}

				if (!inExpression && (c == '{' || c == '}'))
					return null;
			}
			return Variable.newInstance(variableName, returnTypes);
		}
		return null;
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Nullable
	private final <T> Expression<? extends T> parseSingleExpr(final boolean allowUnparsedLiteral, @Nullable final LogEntry error, final Class<? extends T>... types) {
		assert types.length > 0;
		assert types.length == 1 || !CollectionUtils.contains(types, Object.class);
		if (expr.isEmpty())
			return null;
		if (context != ParseContext.COMMAND && expr.startsWith("(") && expr.endsWith(")") && next(expr, 0, context) == expr.length())
			return new SkriptParser(this, "" + expr.substring(1, expr.length() - 1)).parseSingleExpr(allowUnparsedLiteral, error, types);
		final ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			if (context == ParseContext.DEFAULT || context == ParseContext.EVENT) {
				final Variable<? extends T> var = parseVariable(expr, types);
				if (var != null) {
					if ((flags & PARSE_EXPRESSIONS) == 0) {
						Skript.error("Variables cannot be used here.");
						log.printError();
						return null;
					}
					log.printLog();
					return var;
				} else if (log.hasError()) {
					log.printError();
					return null;
				}
				final FunctionReference<T> fr = parseFunction(types);
				if (fr != null) {
					log.printLog();
					return new ExprFunctionCall(fr);
				} else if (log.hasError()) {
					log.printError();
					return null;
				}
			}
			log.clear();
			if ((flags & PARSE_EXPRESSIONS) != 0) {
				final Expression<?> e;
				if (expr.startsWith("\"") && expr.length() != 1 && nextQuote(expr, 1) == expr.length() - 1) {
					e = VariableString.newInstance("" + expr.substring(1, expr.length() - 1));
				} else {
					e = (Expression<?>) parse(expr, (Iterator) Skript.getExpressions(types), null);
				}
				if (e != null) { // Expression/VariableString parsing success
					for (final Class<? extends T> t : types) {
						// Check return type against everything that expression accepts
						if (t.isAssignableFrom(e.getReturnType())) {
							log.printLog();
							return (Expression<? extends T>) e;
						}
					}
					
					// No directly same type found
					Class<T>[] objTypes = (Class<T>[]) types; // Java generics... ?
					final Expression<? extends T> r = e.getConvertedExpression(objTypes);
					if (r != null) {
						log.printLog();
						return r;
					}
					// Print errors, if we couldn't get the correct type
					log.printError(e.toString(null, false) + " " + Language.get("is") + " " + notOfType(types), ErrorQuality.NOT_AN_EXPRESSION);
					return null;
				}
				log.clear();
			}
			if ((flags & PARSE_LITERALS) == 0) {
				log.printError();
				return null;
			}
			if (types[0] == Object.class) {
				// Do check if a literal with this name actually exists before returning an UnparsedLiteral
				if (!allowUnparsedLiteral || Classes.parseSimple(expr, Object.class, context) == null) {
					log.printError();
					return null;
				}
				log.clear();
				final LogEntry e = log.getError();
				return (Literal<? extends T>) new UnparsedLiteral(expr, e != null && (error == null || e.quality > error.quality) ? e : error);
			}
			for (final Class<? extends T> c : types) {
				log.clear();
				assert c != null;
				final T t = Classes.parse(expr, c, context);
				if (t != null) {
					log.printLog();
					return new SimpleLiteral<>(t, false);
				}
			}
			log.printError();
			return null;
		} finally {
			log.stop();
		}
	}
	
	@Nullable
	private final Expression<?> parseSingleExpr(final boolean allowUnparsedLiteral, @Nullable final LogEntry error, final ExprInfo vi) {
		if (expr.isEmpty()) // Empty expressions return nothing, obviously
			return null;
		
		// Command special parsing
		if (context != ParseContext.COMMAND && expr.startsWith("(") && expr.endsWith(")") && next(expr, 0, context) == expr.length())
			return new SkriptParser(this, "" + expr.substring(1, expr.length() - 1)).parseSingleExpr(allowUnparsedLiteral, error, vi);
		final ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			// Construct types array which contains all potential classes
			final Class<?>[] types = new Class[vi.classes.length]; // This may contain nulls!
			boolean hasSingular = false;
			boolean hasPlural = false;
			
			// Another array for all potential types, but this time without any nulls
			// (indexes do not align with other data in ExprInfo)
			final Class<?>[] nonNullTypes = new Class[vi.classes.length];
			
			int nonNullIndex = 0;
			for (int i = 0; i < types.length; i++) {
				if ((flags & vi.flagMask) == 0) { // Flag mask invalidates this, skip it
					continue;
				}
				
				// Plural/singular checks
				// TODO move them elsewhere, this method needs to be as fast as possible
				if (vi.isPlural[i])
					hasPlural = true;
				else
					hasSingular = true;
				
				// Actually put class to types[i]
				types[i] = vi.classes[i].getC();
				
				// Handle nonNullTypes data fill
				nonNullTypes[nonNullIndex] = types[i];
				nonNullIndex++;
			}
			
			boolean onlyPlural = false;
			boolean onlySingular = false;
			if (hasSingular && !hasPlural)
				onlySingular = true;
			else if (!hasSingular && hasPlural)
				onlyPlural = true;
			
			if (context == ParseContext.DEFAULT || context == ParseContext.EVENT) {
				// Attempt to parse variable first
				if (onlySingular || onlyPlural) { // No mixed plurals/singulars possible
					final Variable<?> var = parseVariable(expr, nonNullTypes);
					if (var != null) { // Parsing succeeded, we have a variable
						// If variables cannot be used here, it is now allowed
						if ((flags & PARSE_EXPRESSIONS) == 0) {
							Skript.error("Variables cannot be used here.");
							log.printError();
							return null;
						}
						
						// Plural/singular sanity check
						if (hasSingular && !var.isSingle()) {
							Skript.error("'" + expr + "' can only accept a single value of any type, not more", ErrorQuality.SEMANTIC_ERROR);
							return null;
						}
						
						log.printLog();
						return var;
					} else if (log.hasError()) {
						log.printError();
						return null;
					}
				} else { // Mixed plurals/singulars
					final Variable<?> var = parseVariable(expr, types);
					if (var != null) { // Parsing succeeded, we have a variable
						// If variables cannot be used here, it is now allowed
						if ((flags & PARSE_EXPRESSIONS) == 0) {
							Skript.error("Variables cannot be used here.");
							log.printError();
							return null;
						}
						
						// Plural/singular sanity check
						//
						// It's (currently?) not possible to detect this at parse time when there are multiple
						// acceptable types and only some of them are single, since variables, global especially,
						// can hold any possible type, and the type used can only be 100% known at runtime
						//
						// TODO:
						// despite of that, we should probably implement a runtime check for this somewhere
						// before executing the syntax element (perhaps even exceptionally with a console warning,
						// otherwise users may have some hard time debugging the plurality issues) - currently an
						// improper use in a script would result in an exception
						if (((vi.classes.length == 1 && !vi.isPlural[0]) || Booleans.contains(vi.isPlural, true))
								&& !var.isSingle()) {
							Skript.error("'" + expr + "' can only accept a single "
									+ Classes.toString(Stream.of(vi.classes).map(ci -> ci.getName().toString()).toArray(), false)
									+ ", not more", ErrorQuality.SEMANTIC_ERROR);
							return null;
						}
						
						log.printLog();
						return var;
					} else if (log.hasError()) {
						log.printError();
						return null;
					}
				}
				
				// If it wasn't variable, do same for function call
				final FunctionReference<?> fr = parseFunction(types);
				if (fr != null) {
					log.printLog();
					return new ExprFunctionCall<>(fr);
				} else if (log.hasError()) {
					log.printError();
					return null;
				}
			}
			log.clear();
			if ((flags & PARSE_EXPRESSIONS) != 0) {
				final Expression<?> e;
				if (expr.startsWith("\"") && expr.length() != 1 && nextQuote(expr, 1) == expr.length() - 1) {
					e = VariableString.newInstance("" + expr.substring(1, expr.length() - 1));
				} else {
					e = (Expression<?>) parse(expr, (Iterator) Skript.getExpressions(types), null);
				}
				if (e != null) { // Expression/VariableString parsing success
					Class<?> returnType = e.getReturnType(); // Sometimes getReturnType does non-trivial costly operations
					assert returnType != null;
					for (int i = 0; i < types.length; i++) {
						final Class<?> t = types[i];
						if (t == null) // Ignore invalid (null) types
							continue;
						
						// Check return type against everything that expression accepts
						if (t.isAssignableFrom(returnType)) {
							if (!vi.isPlural[i] && !e.isSingle()) { // Wrong number of arguments
								if (context == ParseContext.COMMAND) {
									Skript.error(Commands.m_too_many_arguments.toString(vi.classes[i].getName().getIndefiniteArticle(), vi.classes[i].getName().toString()), ErrorQuality.SEMANTIC_ERROR);
									return null;
								} else {
									Skript.error("'" + expr + "' can only accept a single " + vi.classes[i].getName() + ", not more", ErrorQuality.SEMANTIC_ERROR);
									return null;
								}
							}
							
							log.printLog();
							return e;
						}
					}
					
					if (onlySingular && !e.isSingle()) {
						Skript.error("'" + expr + "' can only accept singular expressions, not plural", ErrorQuality.SEMANTIC_ERROR);
						return null;
					}
					
					// No directly same type found
					Expression<?> r = e.getConvertedExpression((Class<Object>[]) types);
					if (r != null) {
						log.printLog();
						return r;
					}

					// Print errors, if we couldn't get the correct type
					log.printError(e.toString(null, false) + " " + Language.get("is") + " " + notOfType(types), ErrorQuality.NOT_AN_EXPRESSION);
					return null;
				}
				log.clear();
			}
			if ((flags & PARSE_LITERALS) == 0) {
				log.printError();
				return null;
			}
			if (vi.classes[0].getC() == Object.class) {
				// Do check if a literal with this name actually exists before returning an UnparsedLiteral
				if (!allowUnparsedLiteral || Classes.parseSimple(expr, Object.class, context) == null) {
					log.printError();
					return null;
				}
				log.clear();
				final LogEntry e = log.getError();
				return new UnparsedLiteral(expr, e != null && (error == null || e.quality > error.quality) ? e : error);
			}
			for (final ClassInfo<?> ci : vi.classes) {
				log.clear();
				assert ci.getC() != null;
				final Object t = Classes.parse(expr, ci.getC(), context);
				if (t != null) {
					log.printLog();
					return new SimpleLiteral<>(t, false, new UnparsedLiteral(expr));
				}
			}
			log.printError();
			return null;
		} finally {
			log.stop();
		}
	}
	
	/**
	 * Matches ',', 'and', 'or', etc. as well as surrounding whitespace.
	 * <p>
	 * group 1 is null for ',', otherwise it's one of and/or/nor (not necessarily lowercase).
	 */
	@SuppressWarnings("null")
	public static final Pattern LIST_SPLIT_PATTERN = Pattern.compile("\\s*,?\\s+(and|n?or)\\s+|\\s*,\\s*", Pattern.CASE_INSENSITIVE);
	public static final Pattern OR_PATTERN = Pattern.compile("\\sor\\s", Pattern.CASE_INSENSITIVE);

	private final static String MULTIPLE_AND_OR = "List has multiple 'and' or 'or', will default to 'and'. Use brackets if you want to define multiple lists.";
	private final static String MISSING_AND_OR = "List is missing 'and' or 'or', defaulting to 'and'";
	
	private boolean suppressMissingAndOrWarnings = SkriptConfig.disableMissingAndOrWarnings.value();
	
	private SkriptParser suppressMissingAndOrWarnings() {
		suppressMissingAndOrWarnings = true;
		return this;
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public final <T> Expression<? extends T> parseExpression(final Class<? extends T>... types) {
		if (expr.length() == 0)
			return null;
		
		assert types != null && types.length > 0;
		assert types.length == 1 || !CollectionUtils.contains(types, Object.class);
		
		final boolean isObject = types.length == 1 && types[0] == Object.class;
		final ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			final Expression<? extends T> r = parseSingleExpr(true, null, types);
			if (r != null) {
				log.printLog();
				return r;
			}
			log.clear();
			
			final List<Expression<? extends T>> ts = new ArrayList<>();
			Kleenean and = Kleenean.UNKNOWN;
			boolean isLiteralList = true;
			
			final List<int[]> pieces = new ArrayList<>();
			{
				final Matcher m = LIST_SPLIT_PATTERN.matcher(expr);
				int i = 0, j = 0;
				for (; i >= 0 && i <= expr.length(); i = next(expr, i, context)) {
					if (i == expr.length() || m.region(i, expr.length()).lookingAt()) {
						pieces.add(new int[] {j, i});
						if (i == expr.length())
							break;
						j = i = m.end();
					}
				}
				if (i != expr.length()) {
					assert i == -1 && context != ParseContext.COMMAND : i + "; " + expr;
					log.printError("Invalid brackets/variables/text in '" + expr + "'", ErrorQuality.NOT_AN_EXPRESSION);
					return null;
				}
			}
			
			if (pieces.size() == 1) { // not a list of expressions, and a single one has failed to parse above
				if (expr.startsWith("(") && expr.endsWith(")") && next(expr, 0, context) == expr.length()) {
					log.clear();
					return new SkriptParser(this, "" + expr.substring(1, expr.length() - 1)).parseExpression(types);
				}
				if (isObject && (flags & PARSE_LITERALS) != 0) { // single expression - can return an UnparsedLiteral now
					log.clear();
					return (Expression<? extends T>) new UnparsedLiteral(expr, log.getError());
				}
				// results in useless errors most of the time
//				log.printError("'" + expr + "' " + Language.get("is") + " " + notOfType(types), ErrorQuality.NOT_AN_EXPRESSION);
				log.printError();
				return null;
			}

			// `b` is the first piece included, `a` is the last
			outer: for (int b = 0; b < pieces.size();) {
				for (int a = 1; a <= pieces.size() - b; a++) {
					if (b == 0 && a == pieces.size()) // i.e. the whole expression - already tried to parse above
						continue;
					final int x = pieces.get(b)[0], y = pieces.get(b + a - 1)[1];
					final String subExpr = "" + expr.substring(x, y).trim();
					assert subExpr.length() < expr.length() : subExpr;
					
					final Expression<? extends T> t;
					
					if (subExpr.startsWith("(") && subExpr.endsWith(")") && next(subExpr, 0, context) == subExpr.length())
						t = new SkriptParser(this, subExpr).parseExpression(types); // only parse as possible expression list if its surrounded by brackets
					else
						t = new SkriptParser(this, subExpr).parseSingleExpr(a == 1, log.getError(), types); // otherwise parse as a single expression only
					if (t != null) {
						isLiteralList &= t instanceof Literal;
						ts.add(t);
						if (b != 0) {
							String delimiter = expr.substring(pieces.get(b - 1)[1], x).trim().toLowerCase(Locale.ENGLISH);
							if (!delimiter.equals(",")) {
								boolean or = !delimiter.contains("nor") && delimiter.endsWith("or");
								if (and.isUnknown()) {
									and = Kleenean.get(!or); // nor is and
								} else {
									if (and != Kleenean.get(!or)) {
										Skript.warning(MULTIPLE_AND_OR + " List: " + expr);
										and = Kleenean.TRUE;
									}
								}
							}
						}
						b += a;
						continue outer;
					}
				}
				log.printError();
				return null;
			}

			log.printLog(false);
			
			if (ts.size() == 1)
				return ts.get(0);
			
			if (and.isUnknown() && !suppressMissingAndOrWarnings) {
				ParserInstance parser = getParser();
				Script currentScript = parser.isActive() ? parser.getCurrentScript() : null;
				if (currentScript == null || !currentScript.suppressesWarning(ScriptWarning.MISSING_CONJUNCTION))
					Skript.warning(MISSING_AND_OR + ": " + expr);
			}
			
			final Class<? extends T>[] exprRetTypes = new Class[ts.size()];
			for (int i = 0; i < ts.size(); i++)
				exprRetTypes[i] = ts.get(i).getReturnType();
			
			if (isLiteralList) {
				final Literal<T>[] ls = ts.toArray(new Literal[ts.size()]);
				assert ls != null;
				return new LiteralList<>(ls, (Class<T>) Utils.getSuperType(exprRetTypes), !and.isFalse());
			} else {
				final Expression<T>[] es = ts.toArray(new Expression[ts.size()]);
				assert es != null;
				return new ExpressionList<>(es, (Class<T>) Utils.getSuperType(exprRetTypes), !and.isFalse());
			}
		} finally {
			log.stop();
		}
	}
	
	@Nullable
	public final Expression<?> parseExpression(final ExprInfo vi) {
		if (expr.length() == 0)
			return null;
		
		final boolean isObject = vi.classes.length == 1 && vi.classes[0].getC() == Object.class;
		final ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			// Attempt to parse a single expression
			final Expression<?> r = parseSingleExpr(true, null, vi);
			if (r != null) {
				log.printLog();
				return r;
			}
			log.clear();
			
			final List<Expression<?>> ts = new ArrayList<>();
			Kleenean and = Kleenean.UNKNOWN;
			boolean isLiteralList = true;
			
			final List<int[]> pieces = new ArrayList<>();
			{
				final Matcher m = LIST_SPLIT_PATTERN.matcher(expr);
				int i = 0, j = 0;
				for (; i >= 0 && i <= expr.length(); i = next(expr, i, context)) {
					if (i == expr.length() || m.region(i, expr.length()).lookingAt()) {
						pieces.add(new int[] {j, i});
						if (i == expr.length())
							break;
						j = i = m.end();
					}
				}
				if (i != expr.length()) {
					assert i == -1 && context != ParseContext.COMMAND : i + "; " + expr;
					log.printError("Invalid brackets/variables/text in '" + expr + "'", ErrorQuality.NOT_AN_EXPRESSION);
					return null;
				}
			}
			
			if (pieces.size() == 1) { // not a list of expressions, and a single one has failed to parse above
				if (expr.startsWith("(") && expr.endsWith(")") && next(expr, 0, context) == expr.length()) {
					log.clear();
					return new SkriptParser(this, "" + expr.substring(1, expr.length() - 1)).parseExpression(vi);
				}
				if (isObject && (flags & PARSE_LITERALS) != 0) { // single expression - can return an UnparsedLiteral now
					log.clear();
					return new UnparsedLiteral(expr, log.getError());
				}
				// results in useless errors most of the time
//				log.printError("'" + expr + "' " + Language.get("is") + " " + notOfType(types), ErrorQuality.NOT_AN_EXPRESSION);
				log.printError();
				return null;
			}

			// Early check if this can be parsed as a list.
			// The only case where multiple expressions are allowed, is when it is an 'or' list
			if (!vi.isPlural[0] && !OR_PATTERN.matcher(expr).find()) {
				log.printError();
				return null;
			}

			// `b` is the first piece included, `a` is the last
			outer: for (int b = 0; b < pieces.size();) {
				for (int a = 1; a <= pieces.size() - b; a++) {
					if (b == 0 && a == pieces.size()) // i.e. the whole expression - already tried to parse above
						continue;
					final int x = pieces.get(b)[0], y = pieces.get(b + a - 1)[1];
					final String subExpr = "" + expr.substring(x, y).trim();
					assert subExpr.length() < expr.length() : subExpr;
					
					final Expression<?> t;
					
					if (subExpr.startsWith("(") && subExpr.endsWith(")") && next(subExpr, 0, context) == subExpr.length())
						t = new SkriptParser(this, subExpr).parseExpression(vi); // only parse as possible expression list if its surrounded by brackets
					else
						t = new SkriptParser(this, subExpr).parseSingleExpr(a == 1, log.getError(), vi); // otherwise parse as a single expression only
					if (t != null) {
						isLiteralList &= t instanceof Literal;
						ts.add(t);
						if (b != 0) {
							String delimiter = expr.substring(pieces.get(b - 1)[1], x).trim().toLowerCase(Locale.ENGLISH);
							if (!delimiter.equals(",")) {
								boolean or = !delimiter.contains("nor") && delimiter.endsWith("or");
								if (and.isUnknown()) {
									and = Kleenean.get(!or); // nor is and
								} else {
									if (and != Kleenean.get(!or)) {
										Skript.warning(MULTIPLE_AND_OR + " List: " + expr);
										and = Kleenean.TRUE;
									}
								}
							}
						}
						b += a;
						continue outer;
					}
				}
				log.printError();
				return null;
			}

			// Check if multiple values are accepted
			// If not, only 'or' lists are allowed
			// (both 'and' and potentially 'and' lists will not be accepted)
			if (!vi.isPlural[0] && !and.isFalse()) {
				// List cannot be used in place of a single value here
				log.printError();
				return null;
			}

			log.printLog(false);
			
			if (ts.size() == 1) {
				return ts.get(0);
			}
			
			if (and.isUnknown() && !suppressMissingAndOrWarnings) {
				ParserInstance parser = getParser();
				Script currentScript = parser.isActive() ? parser.getCurrentScript() : null;
				if (currentScript == null || !currentScript.suppressesWarning(ScriptWarning.MISSING_CONJUNCTION))
					Skript.warning(MISSING_AND_OR + ": " + expr);
			}
			
			final Class<?>[] exprRetTypes = new Class[ts.size()];
			for (int i = 0; i < ts.size(); i++)
				exprRetTypes[i] = ts.get(i).getReturnType();
			
			if (isLiteralList) {
				final Literal<?>[] ls = ts.toArray(new Literal[ts.size()]);
				assert ls != null;
				return new LiteralList(ls, Utils.getSuperType(exprRetTypes), !and.isFalse());
			} else {
				final Expression<?>[] es = ts.toArray(new Expression[ts.size()]);
				assert es != null;
				return new ExpressionList(es, Utils.getSuperType(exprRetTypes), !and.isFalse());
			}
		} finally {
			log.stop();
		}
	}
	
	@SuppressWarnings("null")
	private final static Pattern functionCallPattern = Pattern.compile("(" + Functions.functionNamePattern + ")\\((.*)\\)");
	
	/**
	 * @param types The required return type or null if it is not used (e.g. when calling a void function)
	 * @return The parsed function, or null if the given expression is not a function call or is an invalid function call (check for an error to differentiate these two)
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public final <T> FunctionReference<T> parseFunction(final @Nullable Class<? extends T>... types) {
		if (context != ParseContext.DEFAULT && context != ParseContext.EVENT)
			return null;
		final ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			final Matcher m = functionCallPattern.matcher(expr);
			if (!m.matches()) {
				log.printLog();
				return null;
			}

			String functionName = "" + m.group(1);
			String args = m.group(2);
			Expression<?>[] params;
			
			// Check for incorrect quotes, e.g. "myFunction() + otherFunction()" being parsed as one function
			// See https://github.com/SkriptLang/Skript/issues/1532
			for (int i = 0; i < args.length(); i = next(args, i, context)) {
				if (i == -1) {
					log.printLog();
					return null;
				}
			}
			
			if ((flags & PARSE_EXPRESSIONS) == 0) {
				Skript.error("Functions cannot be used here (or there is a problem with your arguments).");
				log.printError();
				return null;
			}
			
			if (args.length() != 0) {
				final Expression<?> ps = new SkriptParser(args, flags | PARSE_LITERALS, context).suppressMissingAndOrWarnings().parseExpression(Object.class);
				if (ps == null) {
					log.printError();
					return null;
				}
				if (ps instanceof ExpressionList) {
					if (!ps.getAnd()) {
						Skript.error("Function arguments must be separated by commas and optionally an 'and', but not an 'or'."
								+ " Put the 'or' into a second set of parentheses if you want to make it a single parameter, e.g. 'give(player, (sword or axe))'");
						log.printError();
						return null;
					}
					params = ((ExpressionList<?>) ps).getExpressions();
				} else {
					params = new Expression[] {ps};
				}
			} else {
				params = new Expression[0];
			}

//			final List<Expression<?>> params = new ArrayList<Expression<?>>();
//			if (args.length() != 0) {
//				final int p = 0;
//				int j = 0;
//				for (int i = 0; i != -1 && i <= args.length(); i = next(args, i, context)) {
//					if (i == args.length() || args.charAt(i) == ',') {
//						final Expression<?> e = new SkriptParser("" + args.substring(j, i).trim(), flags | PARSE_LITERALS, context).parseExpression(function.getParameter(p).getType().getC());
//						if (e == null) {
//							log.printError("Can't understand this expression: '" + args.substring(j, i) + "'", ErrorQuality.NOT_AN_EXPRESSION);
//							return null;
//						}
//						params.add(e);
//						j = i + 1;
//					}
//				}
//			}
//			@SuppressWarnings("null")

			ParserInstance parser = getParser();
			Script currentScript = parser.isActive() ? parser.getCurrentScript() : null;
			final FunctionReference<T> e = new FunctionReference<>(functionName, SkriptLogger.getNode(),
					currentScript != null ? currentScript.getConfig().getFileName() : null, types, params);//.toArray(new Expression[params.size()]));
			if (!e.validateFunction(true)) {
				log.printError();
				return null;
			}
			log.printLog();
			return e;
		} finally {
			log.stop();
		}
	}
	
	/**
	 * Prints parse errors (i.e. must start a ParseLog before calling this method)
	 */
	public static boolean parseArguments(final String args, final ScriptCommand command, final ScriptCommandEvent event) {
		final SkriptParser parser = new SkriptParser(args, PARSE_LITERALS, ParseContext.COMMAND);
		final ParseResult res = parser.parse_i(command.getPattern(), 0, 0);
		if (res == null)
			return false;
		
		final List<Argument<?>> as = command.getArguments();
		assert as.size() == res.exprs.length;
		for (int i = 0; i < res.exprs.length; i++) {
			if (res.exprs[i] == null)
				as.get(i).setToDefault(event);
			else
				as.get(i).set(event, res.exprs[i].getArray(event));
		}
		return true;
	}
	
	/**
	 * Parses the text as the given pattern as {@link ParseContext#COMMAND}.
	 * <p>
	 * Prints parse errors (i.e. must start a ParseLog before calling this method)
	 */
	@Nullable
	public static ParseResult parse(final String text, final String pattern) {
		return new SkriptParser(text, PARSE_LITERALS, ParseContext.COMMAND).parse_i(pattern, 0, 0);
	}
	
	/**
	 * Finds the closing bracket of the group at <tt>start</tt> (i.e. <tt>start</tt> has to be <i>in</i> a group).
	 * 
	 * @param pattern
	 * @param closingBracket The bracket to look for, e.g. ')'
	 * @param openingBracket A bracket that opens another group, e.g. '('
	 * @param start This must not be the index of the opening bracket!
	 * @param isGroup Whether <tt>start</tt> is assumed to be in a group (will print an error if this is not the case, otherwise it returns <tt>pattern.length()</tt>)
	 * @return The index of the next bracket
	 * @throws MalformedPatternException If the group is not closed
	 */
	public static int nextBracket(final String pattern, final char closingBracket, final char openingBracket, final int start, final boolean isGroup) throws MalformedPatternException {
		int n = 0;
		for (int i = start; i < pattern.length(); i++) {
			if (pattern.charAt(i) == '\\') {
				i++;
				continue;
			} else if (pattern.charAt(i) == closingBracket) {
				if (n == 0) {
					if (!isGroup)
						throw new MalformedPatternException(pattern, "Unexpected closing bracket '" + closingBracket + "'");
					return i;
				}
				n--;
			} else if (pattern.charAt(i) == openingBracket) {
				n++;
			}
		}
		if (isGroup)
			throw new MalformedPatternException(pattern, "Missing closing bracket '" + closingBracket + "'");
		return -1;
	}
	
	/**
	 * Gets the next occurrence of a character in a string that is not escaped with a preceding backslash.
	 *
	 * @param pattern
	 * @param c The character to search for
	 * @param from The index to start searching from
	 * @return The next index where the character occurs unescaped or -1 if it doesn't occur.
	 */
	private static int nextUnescaped(final String pattern, final char c, final int from) {
		for (int i = from; i < pattern.length(); i++) {
			if (pattern.charAt(i) == '\\') {
				i++;
			} else if (pattern.charAt(i) == c) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Counts how often the given character occurs in the given string, ignoring any escaped occurrences of the character.
	 * 
	 * @param pattern
	 * @param c The character to search for
	 * @return The number of unescaped occurrences of the given character
	 */
	static int countUnescaped(final String pattern, final char c) {
		return countUnescaped(pattern, c, 0, pattern.length());
	}
	
	static int countUnescaped(final String pattern, final char c, final int start, final int end) {
		assert start >= 0 && start <= end && end <= pattern.length() : start + ", " + end + "; " + pattern.length();
		int r = 0;
		for (int i = start; i < end; i++) {
			final char x = pattern.charAt(i);
			if (x == '\\') {
				i++;
			} else if (x == c) {
				r++;
			}
		}
		return r;
	}
	
	/**
	 * Find the next unescaped (i.e. single) double quote in the string.
	 * 
	 * @param s
	 * @param from Index after the starting quote
	 * @return Index of the end quote
	 */
	private static int nextQuote(final String s, final int from) {
		boolean inExpression = false;
		for (int i = from; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '"' && !inExpression) {
				if (i == s.length() - 1 || s.charAt(i + 1) != '"')
					return i;
				i++;
			} else if (c == '%') {
				inExpression = !inExpression;
			}
		}
		return -1;
	}
	
	/**
	 * @param cs
	 * @return "not an x" or "neither an x, a y nor a z"
	 */
	public static String notOfType(final Class<?>... cs) {
		if (cs.length == 1) {
			final Class<?> c = cs[0];
			assert c != null;
			return Language.get("not") + " " + Classes.getSuperClassInfo(c).getName().withIndefiniteArticle();
		} else {
			final StringBuilder b = new StringBuilder(Language.get("neither") + " ");
			for (int k = 0; k < cs.length; k++) {
				if (k != 0) {
					if (k != cs.length - 1)
						b.append(", ");
					else
						b.append(" " + Language.get("nor") + " ");
				}
				final Class<?> c = cs[k];
				assert c != null;
				b.append(Classes.getSuperClassInfo(c).getName().withIndefiniteArticle());
			}
			return "" + b.toString();
		}
	}
	
	public static String notOfType(final ClassInfo<?>... cs) {
		if (cs.length == 1) {
			return Language.get("not") + " " + cs[0].getName().withIndefiniteArticle();
		} else {
			final StringBuilder b = new StringBuilder(Language.get("neither") + " ");
			for (int k = 0; k < cs.length; k++) {
				if (k != 0) {
					if (k != cs.length - 1)
						b.append(", ");
					else
						b.append(" " + Language.get("nor") + " ");
				}
				b.append(cs[k].getName().withIndefiniteArticle());
			}
			return "" + b.toString();
		}
	}
	
	/**
	 * Returns the next character in the expression, skipping strings,
	 * variables and parentheses
	 * (unless {@code context} is {@link ParseContext#COMMAND}).
	 * 
	 * @param expr The expression to traverse.
	 * @param startIndex The index to start at.
	 * @return The next index (can be expr.length()), or -1 if
	 * an invalid string, variable or bracket is found
	 * or if {@code startIndex >= expr.length()}.
	 * @throws StringIndexOutOfBoundsException if {@code startIndex < 0}.
	 */
	public static int next(String expr, int startIndex, ParseContext context) {
		if (startIndex < 0)
			throw new StringIndexOutOfBoundsException(startIndex);

		int exprLength = expr.length();
		if (startIndex >= exprLength)
			return -1;

		if (context == ParseContext.COMMAND)
			return startIndex + 1;

		int j;
		switch (expr.charAt(startIndex)) {
			case '"':
				j = nextQuote(expr, startIndex + 1);
				return j < 0 ? -1 : j + 1;
			case '{':
				j = VariableString.nextVariableBracket(expr, startIndex + 1);
				return j < 0 ? -1 : j + 1;
			case '(':
				for (j = startIndex + 1; j >= 0 && j < exprLength; j = next(expr, j, context)) {
					if (expr.charAt(j) == ')')
						return j + 1;
				}
				return -1;
			default:
				return startIndex + 1;
		}
	}

	/**
	 * Returns the next occurrence of the needle in the haystack.
	 * Similar to {@link #next(String, int, ParseContext)}, this method skips
	 * strings, variables and parentheses (unless <tt>context</tt> is {@link ParseContext#COMMAND}).
	 *
	 * @param haystack The string to search in.
	 * @param needle The string to search for.
	 * @param startIndex The index to start in within the haystack.
	 * @param caseSensitive Whether this search will be case-sensitive.
	 * @return The next index representing the first character of the needle.
	 * May return -1 if an invalid string, variable or bracket is found or if <tt>startIndex >= hatsack.length()</tt>.
	 * @throws StringIndexOutOfBoundsException if <tt>startIndex < 0</tt>.
	 */
	public static int nextOccurrence(String haystack, String needle, int startIndex, ParseContext parseContext, boolean caseSensitive) {
		if (startIndex < 0)
			throw new StringIndexOutOfBoundsException(startIndex);
		if (parseContext == ParseContext.COMMAND)
			return haystack.indexOf(needle, startIndex);

		int haystackLength = haystack.length();
		if (startIndex >= haystackLength)
			return -1;

		if (!caseSensitive) {
			haystack = haystack.toLowerCase(Locale.ENGLISH);
			needle = needle.toLowerCase(Locale.ENGLISH);
		}

		char firstChar = needle.charAt(0);
		boolean startsWithSpecialChar = firstChar == '"' || firstChar == '{' || firstChar == '(';

		while (startIndex < haystackLength) {

			char c = haystack.charAt(startIndex);

			if (startsWithSpecialChar) { // Early check before special character handling
				if (haystack.startsWith(needle, startIndex))
					return startIndex;
			}

			switch (c) {
				case '"':
					startIndex = nextQuote(haystack, startIndex + 1);
					if (startIndex < 0)
						return -1;
					break;
				case '{':
					startIndex = VariableString.nextVariableBracket(haystack, startIndex + 1);
					if (startIndex < 0)
						return -1;
					break;
				case '(':
					startIndex = next(haystack, startIndex, parseContext); // Use other function to skip to right after closing parentheses
					if (startIndex < 0)
						return -1;
					break;
			}

			if (haystack.startsWith(needle, startIndex))
				return startIndex;

			startIndex++;
		}

		return -1;
	}

	private static final Map<String, SkriptPattern> patterns = new ConcurrentHashMap<>();

	@Nullable
	private ParseResult parse_i(String pattern, int i, int j) {
		if (i != 0 || j != 0)
			throw new IllegalArgumentException();
		SkriptPattern skriptPattern = patterns.computeIfAbsent(pattern, PatternCompiler::compile);
		ch.njol.skript.patterns.MatchResult matchResult = skriptPattern.match(expr, flags, context);
		if (matchResult == null)
			return null;
		return matchResult.toParseResult();
	}

	/**
	 * Validates a user-defined pattern (used in {@link ExprParse}).
	 * 
	 * @param pattern
	 * @return The pattern with %codenames% and a boolean array that contains whether the expressions are plural or not
	 */
	@Nullable
	public static NonNullPair<String, boolean[]> validatePattern(final String pattern) {
		final List<Boolean> ps = new ArrayList<>();
		int groupLevel = 0, optionalLevel = 0;
		final Deque<Character> groups = new LinkedList<>();
		final StringBuilder b = new StringBuilder(pattern.length());
		int last = 0;
		for (int i = 0; i < pattern.length(); i++) {
			final char c = pattern.charAt(i);
			if (c == '(') {
				groupLevel++;
				groups.addLast(c);
			} else if (c == '|') {
				if (groupLevel == 0 || groups.peekLast() != '(' && groups.peekLast() != '|')
					return error("Cannot use the pipe character '|' outside of groups. Escape it if you want to match a literal pipe: '\\|'");
				groups.removeLast();
				groups.addLast(c);
			} else if (c == ')') {
				if (groupLevel == 0 || groups.peekLast() != '(' && groups.peekLast() != '|')
					return error("Unexpected closing group bracket ')'. Escape it if you want to match a literal bracket: '\\)'");
				if (groups.peekLast() == '(')
					return error("(...|...) groups have to contain at least one pipe character '|' to separate it into parts. Escape the brackets if you want to match literal brackets: \"\\(not a group\\)\"");
				groupLevel--;
				groups.removeLast();
			} else if (c == '[') {
				optionalLevel++;
				groups.addLast(c);
			} else if (c == ']') {
				if (optionalLevel == 0 || groups.peekLast() != '[')
					return error("Unexpected closing optional bracket ']'. Escape it if you want to match a literal bracket: '\\]'");
				optionalLevel--;
				groups.removeLast();
			} else if (c == '<') {
				final int j = pattern.indexOf('>', i + 1);
				if (j == -1)
					return error("Missing closing regex bracket '>'. Escape the '<' if you want to match a literal bracket: '\\<'");
				try {
					Pattern.compile(pattern.substring(i + 1, j));
				} catch (final PatternSyntaxException e) {
					return error("Invalid Regular Expression '" + pattern.substring(i + 1, j) + "': " + e.getLocalizedMessage());
				}
				i = j;
			} else if (c == '>') {
				return error("Unexpected closing regex bracket '>'. Escape it if you want to match a literal bracket: '\\>'");
			} else if (c == '%') {
				final int j = pattern.indexOf('%', i + 1);
				if (j == -1)
					return error("Missing end sign '%' of expression. Escape the percent sign to match a literal '%': '\\%'");
				final NonNullPair<String, Boolean> p = Utils.getEnglishPlural("" + pattern.substring(i + 1, j));
				final ClassInfo<?> ci = Classes.getClassInfoFromUserInput(p.getFirst());
				if (ci == null)
					return error("The type '" + p.getFirst() + "' could not be found. Please check your spelling or escape the percent signs if you want to match literal %s: \"\\%not an expression\\%\"");
				ps.add(p.getSecond());
				b.append(pattern.substring(last, i + 1));
				b.append(Utils.toEnglishPlural(ci.getCodeName(), p.getSecond()));
				last = j;
				i = j;
			} else if (c == '\\') {
				if (i == pattern.length() - 1)
					return error("Pattern must not end in an unescaped backslash. Add another backslash to escape it, or remove it altogether.");
				i++;
			}
		}
		b.append(pattern.substring(last));
		final boolean[] plurals = new boolean[ps.size()];
		for (int i = 0; i < plurals.length; i++)
			plurals[i] = ps.get(i);
		return new NonNullPair<>("" + b.toString(), plurals);
	}
	
	@Nullable
	private static NonNullPair<String, boolean[]> error(final String error) {
		Skript.error("Invalid pattern: " + error);
		return null;
	}
	
	private final static Message m_quotes_error = new Message("skript.quotes error");
	private final static Message m_brackets_error = new Message("skript.brackets error");
	
	public static boolean validateLine(final String line) {
		if (StringUtils.count(line, '"') % 2 != 0) {
			Skript.error(m_quotes_error.toString());
			return false;
		}
		for (int i = 0; i < line.length(); i = next(line, i, ParseContext.DEFAULT)) {
			if (i == -1) {
				Skript.error(m_brackets_error.toString());
				return false;
			}
		}
		return true;
	}
	
	public static class ExprInfo {
		public ExprInfo(final int length) {
			classes = new ClassInfo[length];
			isPlural = new boolean[length];
		}
		
		public final ClassInfo<?>[] classes;
		public final boolean[] isPlural;
		public boolean isOptional;
		public int flagMask = ~0;
		public int time = 0;
	}
	
	private static final Map<String,ExprInfo> exprInfoCache = new HashMap<>();
	
	private static ExprInfo getExprInfo(String s) throws IllegalArgumentException, SkriptAPIException {
		ExprInfo r = exprInfoCache.get(s);
		if (r == null) {
			r = createExprInfo(s);
			exprInfoCache.put(s, r);
		}
		
		return r;
	}
	
	private static ExprInfo createExprInfo(String s) throws IllegalArgumentException, SkriptAPIException {
		final ExprInfo r = new ExprInfo(StringUtils.count(s, '/') + 1);
		r.isOptional = s.startsWith("-");
		if (r.isOptional)
			s = "" + s.substring(1);
		if (s.startsWith("*")) {
			s = "" + s.substring(1);
			r.flagMask &= ~PARSE_EXPRESSIONS;
		} else if (s.startsWith("~")) {
			s = "" + s.substring(1);
			r.flagMask &= ~PARSE_LITERALS;
		}
		if (!r.isOptional) {
			r.isOptional = s.startsWith("-");
			if (r.isOptional)
				s = "" + s.substring(1);
		}
		final int a = s.indexOf("@");
		if (a != -1) {
			r.time = Integer.parseInt(s.substring(a + 1));
			s = "" + s.substring(0, a);
		}
		final String[] classes = s.split("/");
		assert classes.length == r.classes.length;
		for (int i = 0; i < classes.length; i++) {
			final NonNullPair<String, Boolean> p = Utils.getEnglishPlural("" + classes[i]);
			r.classes[i] = Classes.getClassInfo(p.getFirst());
			r.isPlural[i] = p.getSecond();
		}
		return r;
	}
	
	/**
	 * @see ParserInstance#get()
	 */
	private static ParserInstance getParser() {
		return ParserInstance.get();
	}

	/**
	 * @deprecated due to bad naming conventions,
	 * use {@link #LIST_SPLIT_PATTERN} instead.
	 */
	@Deprecated
	public final static Pattern listSplitPattern = LIST_SPLIT_PATTERN;
	
}
