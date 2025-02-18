package ch.njol.skript.patterns;

import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link PatternElement} that contains a regex {@link Pattern}, for example {@code <.+>}.
 */
public class RegexPatternElement extends PatternElement {

	private final Pattern pattern;

	public RegexPatternElement(Pattern pattern) {
		this.pattern = pattern;
	}

	@Override
	@Nullable
	public MatchResult match(String expr, MatchResult matchResult) {
		int exprIndex = matchResult.exprOffset;

		ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			Matcher matcher = pattern.matcher(expr);
			for (int nextExprOffset = SkriptParser.next(expr, exprIndex, matchResult.parseContext);
				 nextExprOffset != -1;
				 nextExprOffset = SkriptParser.next(expr, nextExprOffset, matchResult.parseContext)
			) {
				log.clear();
				matcher.region(exprIndex, nextExprOffset);
				if (matcher.matches()) {
					MatchResult matchResultCopy = matchResult.copy();
					matchResultCopy.exprOffset = nextExprOffset;

					MatchResult newMatchResult = matchNext(expr, matchResultCopy);
					if (newMatchResult != null) {
						// Append to end of list
						newMatchResult.regexResults.add(0, matcher.toMatchResult());
						log.printLog();
						return newMatchResult;
					}
				}
			}
			log.printError(null);
			return null;
		} finally {
			log.stop();
		}
	}

	@Override
	public String toString() {
		return "<" + pattern + ">";
	}

}
