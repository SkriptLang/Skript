package ch.njol.skript.patterns;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ExprInfo;
import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.NonNullPair;
import org.jetbrains.annotations.Nullable;

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
			NonNullPair<String, Boolean> p = Utils.getEnglishPlural(classes[i]);
			classInfos[i] = Classes.getClassInfo(p.getFirst());
			isPlural[i] = p.getSecond();
		}

		return new TypePatternElement(classInfos, isPlural, isNullable, flagMask, time, expressionIndex);
	}

	@Override
	@Nullable
	public MatchResult match(String expr, MatchResult matchResult) {
		int newExprOffset;

		String nextLiteral = null;
		boolean nextLiteralIsWhitespace = false;

		if (next == null) {
			newExprOffset = expr.length();
		} else if (next instanceof LiteralPatternElement) {
			nextLiteral = next.toString();

			nextLiteralIsWhitespace = nextLiteral.trim().isEmpty();

			if (!nextLiteralIsWhitespace) { // Don't do this for literal patterns that are *only* whitespace - they have their own special handling
				// trim trailing whitespace - it can cause issues with optional patterns following the literal
				int nextLength = nextLiteral.length();
				for (int i = nextLength; i > 0; i--) {
					if (nextLiteral.charAt(i - 1) != ' ') {
						if (i != nextLength)
							nextLiteral = nextLiteral.substring(0, i);
						break;
					}
				}
			}

			newExprOffset = SkriptParser.nextOccurrence(expr, nextLiteral, matchResult.exprOffset, matchResult.parseContext, false);
			if (newExprOffset == -1 && nextLiteralIsWhitespace) { // We need to tread more carefully here
				// This may be because the next PatternElement is optional or an empty choice (there may be other cases too)
				nextLiteral = null;
				newExprOffset = SkriptParser.next(expr, matchResult.exprOffset, matchResult.parseContext);
			}
		} else {
			newExprOffset = SkriptParser.next(expr, matchResult.exprOffset, matchResult.parseContext);
		}

		if (newExprOffset == -1)
			return null;

		ExprInfo exprInfo = getExprInfo();

		MatchResult matchBackup = null;
		ParseLogHandler loopLogHandlerBackup = null;
		ParseLogHandler expressionLogHandlerBackup = null;

		ParseLogHandler loopLogHandler = SkriptLogger.startParseLogHandler();
		try {
			while (newExprOffset != -1) {
				loopLogHandler.clear();

				MatchResult matchResultCopy = matchResult.copy();
				matchResultCopy.exprOffset = newExprOffset;

				MatchResult newMatchResult = matchNext(expr, matchResultCopy);

				if (newMatchResult != null) {
					ParseLogHandler expressionLogHandler = SkriptLogger.startParseLogHandler();
					try {
						Expression<?> expression = new SkriptParser(expr.substring(matchResult.exprOffset, newExprOffset), matchResult.flags & flagMask, matchResult.parseContext).parseExpression(exprInfo);
						if (expression != null) {
							if (time != 0) {
								if (expression instanceof Literal)
									return null;

								if (ParserInstance.get().getHasDelayBefore() == Kleenean.TRUE) {
									Skript.error("Cannot use time states after the event has already passed", ErrorQuality.SEMANTIC_ERROR);
									return null;
								}

								if (!expression.setTime(time)) {
									Skript.error(expression + " does not have a " + (time == -1 ? "past" : "future") + " state", ErrorQuality.SEMANTIC_ERROR);
									return null;
								}
							}

							newMatchResult.expressions[expressionIndex] = expression;

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
							boolean hasUnparsedLiteral = false;
							for (int i = expressionIndex + 1; i < newMatchResult.expressions.length; i++) {
								if (newMatchResult.expressions[i] instanceof UnparsedLiteral) {
									hasUnparsedLiteral = Classes.parse(((UnparsedLiteral) newMatchResult.expressions[i]).getData(), Object.class, newMatchResult.parseContext) == null;
									if (hasUnparsedLiteral) {
										break;
									}
								}
							}

							if (!hasUnparsedLiteral) {
								expressionLogHandler.printLog();
								loopLogHandler.printLog();
								return newMatchResult;
							} else if (matchBackup == null) { // only backup the first occurrence of unparsed literals
								matchBackup = newMatchResult;
								loopLogHandlerBackup = loopLogHandler.backup();
								expressionLogHandlerBackup = expressionLogHandler.backup();
							}
						}
					} finally {
						expressionLogHandler.printError();
					}
				}

				if (nextLiteral != null) {
					int oldNewExprOffset = newExprOffset;
					newExprOffset = SkriptParser.nextOccurrence(expr, nextLiteral, newExprOffset + 1, matchResult.parseContext, false);
					if (newExprOffset == -1 && nextLiteralIsWhitespace) {
						// This may be because the next PatternElement is optional or an empty choice (there may be other cases too)
						// So, from this point on, we're going to go character by character
						nextLiteral = null;
						newExprOffset = SkriptParser.next(expr, oldNewExprOffset, matchResult.parseContext);
					}
				} else {
					newExprOffset = SkriptParser.next(expr, newExprOffset, matchResult.parseContext);
				}
			}
		} finally {
			if (loopLogHandlerBackup != null) { // print backup logs if applicable
				loopLogHandler.restore(loopLogHandlerBackup);
				assert expressionLogHandlerBackup != null;
				expressionLogHandlerBackup.printLog();
			}
			if (!loopLogHandler.isStopped()) {
				loopLogHandler.printError();
			}
		}

		// if there were unparsed literals, we will return the backup now
		// if there were not, this returns null
		return matchBackup;
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

}
