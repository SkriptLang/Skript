package ch.njol.skript.patterns;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * A {@link PatternElement} that contains an optional part, for example {@code [hello world]}.
 */
public class OptionalPatternElement extends PatternElement {

	private final PatternElement patternElement;

	public OptionalPatternElement(PatternElement patternElement) {
		this.patternElement = patternElement;
	}

	@Override
	void setNext(@Nullable PatternElement next) {
		super.setNext(next);
		patternElement.setLastNext(next);
	}

	@Override
	@Nullable
	public MatchResult match(String expr, MatchResult matchResult) {
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
		return "[" + patternElement.toFullString() + "]";
	}

	@Override
	public Set<String> getCombinations(boolean clean) {
		Set<String> combinations = patternElement.getAllCombinations(clean);
		combinations.add("");
		return combinations;
	}

}
