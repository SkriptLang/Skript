package ch.njol.skript.patterns;

import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.patterns.SkriptPattern.StringificationProperties;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
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
	public @Nullable MatchResult match(String expr, MatchResult matchResult) {
		int exprIndex = matchResult.exprOffset;

		try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
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
						// at this point, all other regex results will have been added
						// thus, we append first to insert at the correct position
						newMatchResult.regexResults.addFirst(matcher.toMatchResult());
						log.printLog();
						return newMatchResult;
					}
				}
			}
			log.printError(null);
			return null;
		}
	}

	@Override
	public String toString() {
		return toString(StringificationProperties.DEFAULT);
	}

	@Override
	public String toString(StringificationProperties properties) {
		return "<" + pattern + ">";
	}

	@Override
	public Set<String> getCombinations(boolean clean) {
		return new HashSet<>(Set.of(toString()));
	}

}
