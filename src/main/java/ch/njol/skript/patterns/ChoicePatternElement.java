package ch.njol.skript.patterns;

import ch.njol.skript.patterns.SkriptPattern.StringificationProperties;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A {@link PatternElement} that has multiple options, for example {@code hello|world}.
 */
public class ChoicePatternElement extends PatternElement {

	private final List<PatternElement> patternElements = new ArrayList<>();

	public void add(PatternElement patternElement) {
		patternElements.add(patternElement);
	}

	public PatternElement getLast() {
		return patternElements.getLast();
	}

	public void setLast(PatternElement patternElement) {
		patternElements.removeLast();
		patternElements.add(patternElement);
	}

	public List<PatternElement> getPatternElements() {
		return patternElements;
	}

	@Override
	void setNext(@Nullable PatternElement next) {
		super.setNext(next);
		for (PatternElement patternElement : patternElements)
			patternElement.setLastNext(next);
	}

	@Override
	public @Nullable MatchResult match(String expr, MatchResult matchResult) {
		for (PatternElement patternElement : patternElements) {
			MatchResult matchResultCopy = matchResult.copy();
			MatchResult newMatchResult = patternElement.match(expr, matchResultCopy);
			if (newMatchResult != null)
				return newMatchResult;
		}
		return null;
	}

	@Override
	public String toString() {
		return toString(StringificationProperties.DEFAULT);
	}

	@Override
	public String toString(StringificationProperties properties) {
		return patternElements.stream()
			.map(element -> element.toFullString(properties))
			.collect(Collectors.joining("|"));
	}

	@Override
	public Set<String> getCombinations(boolean clean) {
		Set<String> combinations = new HashSet<>();
		patternElements.forEach(patternElement -> combinations.addAll(patternElement.getAllCombinations(clean)));
		return combinations;
	}

}
