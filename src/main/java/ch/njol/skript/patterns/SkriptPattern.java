package ch.njol.skript.patterns;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SkriptPattern {

	private final PatternElement first;
	private final int expressionAmount;

	private final Keyword[] keywords;
	@Nullable
	private List<TypePatternElement> types;

	public SkriptPattern(PatternElement first, int expressionAmount) {
		this.first = first;
		this.expressionAmount = expressionAmount;
		keywords = Keyword.buildKeywords(first);
	}

	@Nullable
	public MatchResult match(String expr, int flags, ParseContext parseContext) {
		// Matching shortcut
		String lowerExpr = expr.toLowerCase(Locale.ENGLISH);
		for (Keyword keyword : keywords) {
			if (!keyword.isPresent(lowerExpr))
				return null;
		}

		expr = expr.trim();

		MatchResult matchResult = new MatchResult();
		matchResult.source = this;
		matchResult.expr = expr;
		matchResult.expressions = new Expression[expressionAmount];
		matchResult.parseContext = parseContext;
		matchResult.flags = flags;
		return first.match(expr, matchResult);
	}

	@Nullable
	public MatchResult match(String expr) {
		return match(expr, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT);
	}

	@Override
	public String toString() {
		return first.toFullString();
	}

	/**
	 * @return the size of the {@link MatchResult#expressions} array
	 * from a match.
	 */
	public int countTypes() {
		return expressionAmount;
	}

	/**
	 * Count the maximum amount of non-null types in this pattern,
	 * i.e. the maximum amount of non-null values in the {@link MatchResult#expressions}
	 * array from a match.
	 *
	 * @see #countTypes() for the amount of nullable values
	 * in the expressions array from a match.
	 */
	public int countNonNullTypes() {
		return countNonNullTypes(first);
	}

	/**
	 * Count the maximum amount of non-null types in the given pattern,
	 * i.e. the maximum amount of non-null values in the {@link MatchResult#expressions}
	 * array from a match.
	 */
	private static int countNonNullTypes(PatternElement patternElement) {
		int count = 0;

		// Iterate over all consequent pattern elements
		while (patternElement != null) {
			if (patternElement instanceof ChoicePatternElement) {
				// Keep track of the max type count of each component
				int max = 0;

				for (PatternElement component : ((ChoicePatternElement) patternElement).getPatternElements()) {
					int componentCount = countNonNullTypes(component);
					if (componentCount > max) {
						max = componentCount;
					}
				}

				// Only one of the components will be used, the rest will be non-null
				//  So we only need to add the max
				count += max;
			} else if (patternElement instanceof GroupPatternElement) {
				// For groups and optionals, simply recurse
				count += countNonNullTypes(((GroupPatternElement) patternElement).getPatternElement());
			} else if (patternElement instanceof OptionalPatternElement) {
				count += countNonNullTypes(((OptionalPatternElement) patternElement).getPatternElement());
			} else if (patternElement instanceof TypePatternElement) {
				// Increment when seeing a type
				count++;
			}

			// Move on to the next pattern element
			patternElement = patternElement.originalNext;
		}

		return count;
	}

	/**
	 * A method to obtain a list of all pattern elements of a specified type that are represented by this SkriptPattern.
	 * @param type The type of pattern elements to obtain.
	 * @return A list of all pattern elements of the specified type represented by this SkriptPattern.
	 * @param <T> The type of pattern element.
	 */
	public <T extends PatternElement> List<T> getElements(Class<T> type) {
		if (type == TypePatternElement.class) {
			if (types == null)
				types = ImmutableList.copyOf(getElements(TypePatternElement.class, first, new ArrayList<>()));
			//noinspection unchecked - checked with type == TypePatternElement
			return (List<T>) types;
		}
		return getElements(type, first, new ArrayList<>());
	}

	/**
	 * A method to obtain a list of all pattern elements of a specified type (from a starting element).
	 * @param type The type of pattern elements to obtain.
	 * @param element The element to start searching for other elements from (this will unwrap certain elements).
	 * @param elements A list to add matching elements to.
	 * @return A list of all pattern elements of a specified type (from a starting element).
	 * @param <T> The type of pattern element.
	 */
	private static <T extends PatternElement> List<T> getElements(Class<T> type, PatternElement element, List<T> elements) {
		while (element != null) {
			if (element instanceof ChoicePatternElement) {
				((ChoicePatternElement) element).getPatternElements().forEach(e -> getElements(type, e, elements));
			} else if (element instanceof GroupPatternElement) {
				getElements(type, ((GroupPatternElement) element).getPatternElement(), elements);
			} else if (element instanceof OptionalPatternElement) {
				getElements(type, ((OptionalPatternElement) element).getPatternElement(), elements);
			} else if (type.isInstance(element)) {
				//noinspection unchecked - it is checked with isInstance
				elements.add((T) element);
			}
			element = element.originalNext;
		}
		return elements;
	}

}
