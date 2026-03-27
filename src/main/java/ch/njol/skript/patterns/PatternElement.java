package ch.njol.skript.patterns;

import ch.njol.skript.patterns.SkriptPattern.StringificationProperties;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * A pattern element represents a part of a {@link SkriptPattern}.
 */
public abstract class PatternElement {

	@Nullable PatternElement originalNext;
	@Nullable PatternElement next;

	void setNext(@Nullable PatternElement next) {
		this.next = next;
	}

	void setLastNext(@Nullable PatternElement newNext) {
		PatternElement next = this;
		while (true) {
			if (next.next == null) {
				next.setNext(newNext);
				return;
			}
			next = next.next;
		}
	}

	public abstract @Nullable MatchResult match(String expr, MatchResult matchResult);

	protected @Nullable MatchResult matchNext(String expr, MatchResult matchResult) {
		if (next == null) {
			return matchResult.exprOffset == expr.length() ? matchResult : null;
		}
		return next.match(expr, matchResult);
	}

	@Override
	public abstract String toString();

	/**
	 * Constructs a string representation of this pattern element.
	 * @param properties Properties to consider during stringification.
	 * @return A string representation of this pattern element.
	 */
	public abstract String toString(StringificationProperties properties);

	/**
	 * @deprecated Use {@link #toFullString(StringificationProperties)}.
	 */
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
	public String toFullString() {
		return toFullString(StringificationProperties.DEFAULT);
	}

	/**
	 * Constructs a string representation of this pattern element and those that follow it.
	 * @param properties Properties to consider during stringification.
	 * @return A string representation of this pattern element and those that follow it.
	 */
	public String toFullString(StringificationProperties properties) {
		StringBuilder stringBuilder = new StringBuilder(toString(properties));
		PatternElement next = this;
		while ((next = next.originalNext) != null) {
			stringBuilder.append(next.toString(properties));
		}
		return stringBuilder.toString();
	}

	/**
	 * Gets the combinations available to this {@link PatternElement}.
	 * @param clean Whether unnecessary data, determined by each implementation, should be excluded from the combinations.
	 * @return The combinations.
	 */
	public abstract Set<String> getCombinations(boolean clean);

	/**
	 * Gets all combinations available to this {@link PatternElement} and linked {@link PatternElement}s.
	 * @param clean Whether unnecessary data, determined by each implementation, should be excluded from the combinations.
	 * @return The combinations.
	 */
	public final Set<String> getAllCombinations(boolean clean) {
		Set<String> combinations = getCombinations(clean);
		if (combinations.isEmpty())
			combinations.add("");
		PatternElement next = this;
		while ((next = next.originalNext) != null) {
			Set<String> newCombinations = new HashSet<>();
			Set<String> nextCombinations = next.getCombinations(clean);
			if (nextCombinations.isEmpty())
				continue;
			for (String base : combinations) {
				for (String add : nextCombinations) {
					newCombinations.add(combineCombination(base, add));
				}
			}
			combinations = newCombinations;
		}
		return combinations;
	}

	/**
	 * Helper method for appropriately combining two strings together.
	 * @return The resulting string.
	 */
	private static String combineCombination(String first, String second) {
		if (first.isBlank()) {
			return second.stripLeading();
		} else if (second.isEmpty()) {
			return first.stripTrailing();
		} else if (first.endsWith(" ") && second.startsWith(" ")) {
			return first + second.stripLeading();
		}
		return first + second;
	}

}
