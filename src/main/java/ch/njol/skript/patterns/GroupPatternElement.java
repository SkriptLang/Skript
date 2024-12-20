package ch.njol.skript.patterns;

import org.jetbrains.annotations.Nullable;

/**
 * A {@link PatternElement} that represents a group, for example {@code (test)}.
 */
public class GroupPatternElement extends PatternElement {

	private final PatternElement patternElement;

	public GroupPatternElement(PatternElement patternElement) {
		this.patternElement = patternElement;
	}

	public PatternElement getPatternElement() {
		return patternElement;
	}

	@Override
	void setNext(@Nullable PatternElement next) {
		super.setNext(next);
		patternElement.setLastNext(next);
	}

	@Override
	@Nullable
	public MatchResult match(String expr, MatchResult matchResult) {
		return patternElement.match(expr, matchResult);
	}

	@Override
	public String toString() {
		return "(" + patternElement + ")";
	}

}
