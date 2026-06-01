package ch.njol.skript.patterns;

import ch.njol.skript.patterns.SkriptPattern.StringificationProperties;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * A {@link PatternElement} that contains an optional part, for example {@code [hello world]}.
 */
public class OptionalPatternElement extends PatternElement {

	private final PatternElement patternElement;

	public OptionalPatternElement(PatternElement patternElement) {
		if (patternElement instanceof GroupPatternElement groupPatternElement && groupPatternElement.next == null) {
			// convert [(...)] to [...], for example [(a|b)] to [a|b]
			patternElement = groupPatternElement.getPatternElement();
		}
		this.patternElement = patternElement;
	}

	@Override
	void setNext(@Nullable PatternElement next) {
		super.setNext(next);
		patternElement.setLastNext(next);
	}

	@Override
	public @Nullable MatchResult match(String expr, MatchResult matchResult) {
		MatchResult newMatchResult = patternElement.match(expr, matchResult.copy());
		if (newMatchResult != null)
			return newMatchResult;
		return matchNext(expr, matchResult);
	}

	public PatternElement getPatternElement() {
		return patternElement;
	}

	@Override
	public String toString() {
		return toString(StringificationProperties.DEFAULT);
	}

	@Override
	public String toString(StringificationProperties properties) {
		return "[" + patternElement.toFullString(properties) + "]";
	}

	@Override
	public Set<String> getCombinations(boolean clean) {
		Set<String> combinations = patternElement.getAllCombinations(clean);
		combinations.add("");
		return combinations;
	}

}
