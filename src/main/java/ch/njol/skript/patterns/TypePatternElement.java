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
package ch.njol.skript.patterns;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ExprInfo;
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

	public static TypePatternElement fromString(String s, int expressionIndex) {
		boolean isNullable = s.startsWith("-");
		if (isNullable)
			s = s.substring(1);

		int flagMask = ~0;
		if (s.startsWith("*")) {
			s = s.substring(1);
			flagMask &= ~SkriptParser.PARSE_EXPRESSIONS;
		} else if (s.startsWith("~")) {
			s = s.substring(1);
			flagMask &= ~SkriptParser.PARSE_LITERALS;
		}

		if (!isNullable) {
			isNullable = s.startsWith("-");
			if (isNullable)
				s = s.substring(1);
		}

		int time = 0;
		int timeStart = s.indexOf("@");
		if (timeStart != -1) {
			time = Integer.parseInt(s.substring(timeStart + 1));
			s = s.substring(0, timeStart);
		}

		String[] classes = s.split("/");
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
		if (next == null) {
			newExprOffset = expr.length();
		} else {
			newExprOffset = SkriptParser.next(expr, matchResult.exprOffset, matchResult.parseContext);
			if (newExprOffset == -1)
				return null;
		}

		ExprInfo exprInfo = getExprInfo();

		ParseLogHandler loopLogHandler = SkriptLogger.startParseLogHandler();
		try {
			for (; newExprOffset != -1; newExprOffset = SkriptParser.next(expr, newExprOffset, matchResult.parseContext)) {
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

							expressionLogHandler.printLog();
							loopLogHandler.printLog();

							newMatchResult.expressions[expressionIndex] = expression;
							return newMatchResult;
						}
					} finally {
						expressionLogHandler.printError();
					}
				}
			}
		} finally {
			if (!loopLogHandler.isStopped())
				loopLogHandler.printError();
		}

		return null;
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

	private ExprInfo getExprInfo() {
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
