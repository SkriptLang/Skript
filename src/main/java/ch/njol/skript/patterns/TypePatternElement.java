package ch.njol.skript.patterns;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ExprInfo;
import ch.njol.skript.lang.parser.ExpressionParseCache;
import ch.njol.skript.lang.parser.LiteralParseCache;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * A {@link PatternElement} that contains a type to be matched with an expressions, for example {@code %number%}.
 */
public class TypePatternElement extends PatternElement {

	private final ClassInfo<?>[] classes;
	private final boolean[] isPlural;
	private final boolean isNullable;
	private final int flagMask;
	private final int time;

	private final int expressionIndex;

	public TypePatternElement(ClassInfo<?>[] classes, boolean[] isPlural, boolean isNullable, int flagMask, int time, int expressionIndex) {
		this.classes = classes;
		this.isPlural = isPlural;
		this.isNullable = isNullable;
		this.flagMask = flagMask;
		this.time = time;
		this.expressionIndex = expressionIndex;
	}

	public static TypePatternElement fromString(String string, int expressionIndex) {
		int caret = 0, flagMask = ~0;
		boolean isNullable = false;
		flags:
		do {
			switch (string.charAt(caret)) {
				case '-':
					isNullable = true;
					break;
				case '*':
					flagMask &= ~SkriptParser.PARSE_EXPRESSIONS;
					break;
				case '~':
					flagMask &= ~SkriptParser.PARSE_LITERALS;
					break;
				default:
					break flags;
			}
			++caret;
		} while (true);

		int time = 0;
		int timeStart = string.indexOf('@', caret);
		if (timeStart != -1) {
			time = Integer.parseInt(string.substring(timeStart + 1));
			string = string.substring(0, timeStart);
		} else {
			string = string.substring(caret);
		}

		String[] classes = string.split("/");
		ClassInfo<?>[] classInfos = new ClassInfo[classes.length];
		boolean[] isPlural = new boolean[classes.length];

		for (int i = 0; i < classes.length; i++) {
			Utils.PluralResult p = Utils.isPlural(classes[i]);
			classInfos[i] = Classes.getClassInfo(p.updated());
			isPlural[i] = p.plural();
		}

		return new TypePatternElement(classInfos, isPlural, isNullable, flagMask, time, expressionIndex);
	}

	@Override
	public @Nullable MatchResult match(String expr, MatchResult matchResult) {
		int exprOffset = initOffset(expr, matchResult);
		if (exprOffset == -1)
			return null;

		ExprInfo exprInfo = getExprInfo();
		ExpressionParseCache parseCache = ParserInstance.get().getExpressionParseCache();

		MatchResult matchBackup = null;
		ParseLogHandler loopLogBackup = null;
		ParseLogHandler exprLogBackup = null;

		ParseLogHandler loopLog = SkriptLogger.startParseLogHandler();
		try {
			while (exprOffset != -1) {
				loopLog.clear();

				// match rest of pattern to determine our range to work in
				MatchResult copy = matchResult.copy();
				copy.exprOffset = exprOffset;
				MatchResult tailMatch = matchNext(expr, copy);
				if (tailMatch == null) {
					exprOffset = advanceOffset(expr, exprOffset, matchResult.parseContext);
					continue;
				}

				// Check if this substring has already failed.
				String substring = expr.substring(matchResult.exprOffset, exprOffset);
				int effectiveFlags = matchResult.flags & flagMask;
				var cacheKey = new ExpressionParseCache.Failure(substring, effectiveFlags, classes, isPlural, isNullable, time);
				if (parseCache.contains(cacheKey)) {
					exprOffset = advanceOffset(expr, exprOffset, matchResult.parseContext);
					continue;
				}

				// actually attempt to parse the substring, adding to cache if failed.
				ParseLogHandler exprLog = SkriptLogger.startParseLogHandler();
				try {
					Expression<?> expression = new SkriptParser(substring, effectiveFlags, matchResult.parseContext)
																.parseExpression(exprInfo);

					if (expression == null) {
						parseCache.add(cacheKey);
						exprOffset = advanceOffset(expr, exprOffset, matchResult.parseContext);
						continue;
					}
					// time states need to match and be valid
					if (!applyTimeState(expression)) {
						loopLog.printError();
						return null;
					}

					tailMatch.expressions[expressionIndex] = expression;
					if (!hasUnparsedLiterals(tailMatch)) {
						exprLog.printLog();
						loopLog.printLog();
						return tailMatch;
					}
					if (matchBackup == null) {
						matchBackup = tailMatch;
						loopLogBackup = loopLog.backup();
						exprLogBackup = exprLog.backup();
					}
				} finally {
					if (!exprLog.isStopped())
						exprLog.printError();
				}

				exprOffset = advanceOffset(expr, exprOffset, matchResult.parseContext);
			}
		} finally {
			if (loopLogBackup != null) {
				loopLog.restore(loopLogBackup);
				assert exprLogBackup != null;
				exprLogBackup.printLog();
			}
			if (!loopLog.isStopped())
				loopLog.printError();
		}

		return matchBackup;
	}

	/**
	 * Applies time state to the expression. Returns false if the time state
	 * cannot be applied, meaning matching should fail entirely.
	 */
	private boolean applyTimeState(Expression<?> expression) {
		if (time == 0)
			return true;
		if (expression instanceof Literal)
			return false;
		if (ParserInstance.get().getHasDelayBefore() == Kleenean.TRUE) {
			Skript.error("Cannot use time states after the event has already passed", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		if (!expression.setTime(time)) {
			Skript.error(expression + " does not have a " + (time == -1 ? "past" : "future") + " state", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}

	/**
	 * Checks whether any expressions after this one are unparsed literals
	 * that cannot be parsed as Object. If so, the loop should continue
	 * to try to find a match without unparsed literals.
	 */
	private boolean hasUnparsedLiterals(@NotNull MatchResult matchResult) {
		LiteralParseCache literalCache = ParserInstance.get().getLiteralParseCache();
		for (int i = expressionIndex + 1; i < matchResult.expressions.length; i++) {
			if (!(matchResult.expressions[i] instanceof UnparsedLiteral unparsed))
				continue;
			var key = new LiteralParseCache.Failure(unparsed.getData(), matchResult.parseContext);
			if (literalCache.contains(key))
				return true;
			if (Classes.parse(unparsed.getData(), Object.class, matchResult.parseContext) == null) {
				literalCache.add(key);
				return true;
			}
		}
		return false;
	}

	// -- Offset computation --
	// nextLiteral and nextLiteralIsWhitespace are set by initOffset() and
	// mutated by advanceOffset() when a whitespace literal search falls through.

	private @Nullable String nextLiteral;
	private boolean nextLiteralIsWhitespace;

	/**
	 * Computes the initial expression offset based on the next pattern element.
	 */
	private int initOffset(String expr, MatchResult matchResult) {
		if (next == null)
			return expr.length();

		if (!(next instanceof LiteralPatternElement)) {
			nextLiteral = null;
			return SkriptParser.next(expr, matchResult.exprOffset, matchResult.parseContext);
		}

		nextLiteral = next.toString();
		nextLiteralIsWhitespace = nextLiteral.trim().isEmpty();

		if (!nextLiteralIsWhitespace) {
			// trim trailing whitespace — can cause issues with optional patterns following the literal
			int len = nextLiteral.length();
			for (int i = len; i > 0; i--) {
				if (nextLiteral.charAt(i - 1) != ' ') {
					if (i != len)
						nextLiteral = nextLiteral.substring(0, i);
					break;
				}
			}
		}

		int offset = SkriptParser.nextOccurrence(expr, nextLiteral, matchResult.exprOffset, matchResult.parseContext, false);
		if (offset == -1 && nextLiteralIsWhitespace) {
			nextLiteral = null;
			offset = SkriptParser.next(expr, matchResult.exprOffset, matchResult.parseContext);
		}
		return offset;
	}

	/**
	 * Advances the expression offset to the next candidate split point.
	 */
	private int advanceOffset(String expr, int currentOffset, ParseContext parseContext) {
		if (nextLiteral == null)
			return SkriptParser.next(expr, currentOffset, parseContext);

		int newOffset = SkriptParser.nextOccurrence(expr, nextLiteral, currentOffset + 1, parseContext, false);
		if (newOffset == -1 && nextLiteralIsWhitespace) {
			nextLiteral = null;
			return SkriptParser.next(expr, currentOffset, parseContext);
		}
		return newOffset;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder().append("%");
		if (isNullable)
			stringBuilder.append("-");
		if (flagMask != ~0) {
			if ((flagMask & SkriptParser.PARSE_LITERALS) == 0)
				stringBuilder.append("~");
			else if ((flagMask & SkriptParser.PARSE_EXPRESSIONS) == 0)
				stringBuilder.append("*");
		}
		for (int i = 0; i < classes.length; i++) {
			String codeName = classes[i].getCodeName();
			if (isPlural[i])
				stringBuilder.append(Utils.toEnglishPlural(codeName));
			else
				stringBuilder.append(codeName);
			if (i != classes.length - 1)
				stringBuilder.append("/");
		}
		if (time != 0)
			stringBuilder.append("@").append(time);
		return stringBuilder.append("%").toString();
	}

	public ExprInfo getExprInfo() {
		ExprInfo exprInfo = new ExprInfo(classes.length);
		for (int i = 0; i < classes.length; i++) {
			exprInfo.classes[i] = classes[i];
			exprInfo.isPlural[i] = isPlural[i];
		}
		exprInfo.isOptional = isNullable;
		exprInfo.flagMask = flagMask;
		exprInfo.time = time;
		return exprInfo;
	}

	/**
	 * {@inheritDoc}
	 * @param clean Whether this type should be replaced with {@code %*%} if it's not literal.
	 */
	@Override
	public Set<String> getCombinations(boolean clean) {
		Set<String> combinations = new HashSet<>();
		if (!clean || flagMask == 2) {
			combinations.add(toString());
		} else {
			combinations.add("%*%");
		}
		return combinations;
	}

}
