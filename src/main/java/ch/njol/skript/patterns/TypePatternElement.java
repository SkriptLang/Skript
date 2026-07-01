package ch.njol.skript.patterns;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ExprInfo;
import ch.njol.skript.lang.parser.ExpressionParseCache;
import ch.njol.skript.lang.parser.LiteralParseCache;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.patterns.SkriptPattern.StringificationProperties;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;

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
				case '-' -> isNullable = true;
				case '*' -> flagMask &= ~SkriptParser.PARSE_EXPRESSIONS;
				case '~' -> flagMask &= ~SkriptParser.PARSE_LITERALS;
				default -> {
					break flags;
				}
			}
			caret++;
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
		OffsetState state = new OffsetState();
		int exprOffset = initOffset(expr, matchResult, state);
		if (exprOffset == -1)
			return null;

		ExprInfo exprInfo = getExprInfo();
		ExpressionParseCache parseCache = ParserInstance.get().getExpressionParseCache();

		MatchResult matchBackup = null;
		ParseLogHandler loopLogBackup = null;
		ParseLogHandler exprLogBackup = null;

		//noinspection resource - managed manually due to other usages
		ParseLogHandler loopLog = SkriptLogger.startParseLogHandler();
		try {
			while (exprOffset != -1) {
				loopLog.clear();

				// Check if this substring has already failed.
				String substring = expr.substring(matchResult.exprOffset, exprOffset);
				int effectiveFlags = matchResult.flags & flagMask;
				var cacheKey = new ExpressionParseCache.Failure(substring, effectiveFlags, classes, isPlural, isNullable, time);
				if (parseCache.contains(cacheKey)) {
					exprOffset = advanceOffset(expr, exprOffset, matchResult.parseContext, state);
					continue;
				}

				// match rest of pattern to determine our range to work in
				MatchResult copy = matchResult.copy();
				copy.exprOffset = exprOffset;
				MatchResult tailMatch = matchNext(expr, copy);
				if (tailMatch == null) {
					exprOffset = advanceOffset(expr, exprOffset, matchResult.parseContext, state);
					continue;
				}

				// actually attempt to parse the substring, adding to cache if failed.
				//noinspection resource - managed manually due to other usages
				ParseLogHandler exprLog = SkriptLogger.startParseLogHandler();
				try {
					Expression<?> expression = new SkriptParser(substring, effectiveFlags, matchResult.parseContext)
																.parseExpression(exprInfo);

					if (expression == null) {
						parseCache.add(cacheKey);
						exprOffset = advanceOffset(expr, exprOffset, matchResult.parseContext, state);
						continue;
					}
					// time states need to match and be valid
					if (!applyTimeState(expression)) {
						return null;
					}

					tailMatch.expressions[expressionIndex] = expression;
					/*
					 * the parser will return unparsed literals in cases where it cannot interpret an input and object is the desired return type.
					 * in those cases, it is up to the expression to interpret the input.
					 * however, this presents a problem for input that is not intended as being one of these object-accepting expressions.
					 * these object-accepting expressions will be matched instead but their parsing will fail as they cannot interpret the unparsed literals.
					 * even though it can't interpret them, this loop will have returned a match and thus parsing has ended (and the correct interpretation never attempted).
					 * to avoid this issue, while also permitting unparsed literals in cases where they are justified,
					 *  the code below forces the loop to continue in hopes of finding a match without unparsed literals.
					 * if it is unsuccessful, a backup of the first successful match (with unparsed literals) is saved to be returned.
					 */
					if (!hasUnparsedLiterals(tailMatch)) {
						exprLog.printLog();
						loopLog.printLog();
						return tailMatch;
					}
					if (matchBackup == null) { // only backup the first occurrence of unparsed literals
						matchBackup = tailMatch;
						loopLogBackup = loopLog.backup();
						exprLogBackup = exprLog.backup();
					}
				} finally {
					if (!exprLog.isStopped())
						exprLog.printError();
				}

				exprOffset = advanceOffset(expr, exprOffset, matchResult.parseContext, state);
			}
		} finally {
			if (loopLogBackup != null) { // print backup logs if applicable
				loopLog.restore(loopLogBackup);
				assert exprLogBackup != null;
				exprLogBackup.printLog();
			}
			if (!loopLog.isStopped())
				loopLog.printError();
		}
		// if there were unparsed literals, we will return the backup now
		// if there were not, this returns null
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
			Skript.error("Cannot use time states after the event has already passed");
			return false;
		}
		if (!expression.setTime(time)) {
			Skript.error(expression + " does not have a " + (time == EventValue.Time.PAST.value() ? "past" : "future") + " state");
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

	/**
	 * Mutable state for a single match() invocation's offset iteration.
	 * Kept separate from the element so the element is thread-safe.
	 */
	private static final class OffsetState {
		@Nullable String nextLiteral; // string value of next literal element (for finding right boundary)
		boolean nextLiteralIsWhitespace; // whether that next literal is just whitespace
	}

	/**
	 * Computes the initial expression offset based on the next pattern element.
	 */
	private int initOffset(String expr, MatchResult matchResult, OffsetState state) {
		if (next == null)
			return expr.length();

		if (!(next instanceof LiteralPatternElement)) {
			state.nextLiteral = null;
			return SkriptParser.next(expr, matchResult.exprOffset, matchResult.parseContext);
		}

		state.nextLiteral = next.toString();
		state.nextLiteralIsWhitespace = state.nextLiteral.trim().isEmpty();

		if (!state.nextLiteralIsWhitespace) { // Don't do this for literal patterns that are *only* whitespace - they have their own special handling
			// trim trailing whitespace - it can cause issues with optional patterns following the literal
			state.nextLiteral = state.nextLiteral.stripTrailing();
		}

		int offset = SkriptParser.nextOccurrence(expr, state.nextLiteral, matchResult.exprOffset, matchResult.parseContext, false);
		if (offset == -1 && state.nextLiteralIsWhitespace) { // We need to tread more carefully here
			// This may be because the next PatternElement is optional or an empty choice (there may be other cases too)
			state.nextLiteral = null;
			offset = SkriptParser.next(expr, matchResult.exprOffset, matchResult.parseContext);
		}
		return offset;
	}

	/**
	 * Advances the expression offset to the next candidate split point.
	 */
	private int advanceOffset(String expr, int currentOffset, ParseContext parseContext, OffsetState state) {
		if (state.nextLiteral == null)
			return SkriptParser.next(expr, currentOffset, parseContext);

		int newOffset = SkriptParser.nextOccurrence(expr, state.nextLiteral, currentOffset + 1, parseContext, false);
		if (newOffset == -1 && state.nextLiteralIsWhitespace) {
			// This may be because the next PatternElement is optional or an empty choice (there may be other cases too)
			// So, from this point on, we're going to go character by character
			state.nextLiteral = null;
			return SkriptParser.next(expr, currentOffset, parseContext);
		}
		return newOffset;
	}

	@Override
	public String toString() {
		return toString(StringificationProperties.DEFAULT);
	}

	@Override
	public String toString(StringificationProperties properties) {
		StringBuilder stringBuilder = new StringBuilder().append("%");
		if (!properties.excludeTypeFlags()) {
			if (isNullable) {
				stringBuilder.append("-");
			}
			if (flagMask != ~0) {
				if ((flagMask & SkriptParser.PARSE_LITERALS) == 0) {
					stringBuilder.append("~");
				} else if ((flagMask & SkriptParser.PARSE_EXPRESSIONS) == 0) {
					stringBuilder.append("*");
				}
			}
		}
		for (int i = 0; i < classes.length; i++) {
			String codeName = classes[i].getCodeName();
			if (isPlural[i]) {
				stringBuilder.append(Utils.toEnglishPlural(codeName));
			} else {
				stringBuilder.append(codeName);
			}
			if (i != classes.length - 1) {
				stringBuilder.append("/");
			}
		}
		if (!properties.excludeTypeFlags() && time != 0) {
			stringBuilder.append("@").append(time);
		}
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
