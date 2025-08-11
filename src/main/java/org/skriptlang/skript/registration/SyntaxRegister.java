package org.skriptlang.skript.registration;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * A syntax register is a collection of registered {@link SyntaxInfo}s of a common type.
 * @param <I> The type of syntax in this register.
 */
final class SyntaxRegister<I extends SyntaxInfo<?>> {

	private static int calculateComplexityScore(SyntaxInfo<?> info) {
		return info.patterns().stream()
				.mapToInt(SyntaxRegister::calculateComplexityScore)
				.max()
				.orElseThrow(); // a syntax info should have at least one pattern
	}

	private static int calculateComplexityScore(String pattern) {
		int score = 0;
		char[] chars = pattern.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '%') {
				// weigh "%thing% %thing%" or "%thing% [%thing%]" heavier
				if ((i - 2 >= 0 && chars[i - 2] == '%') || (i - 3 >= 0 && chars[i - 3] == '%')) {
					score += 3;
				} else {
					score++;
				}
			}
		}
		return score;
	}

	private static final Comparator<SyntaxInfo<?>> SET_COMPARATOR = (a,b) -> {
		if (a == b) { // only considered equal if registering the same infos
			return 0;
		}
		// priority is the primary factor in determining ordering
		int priorityResult = a.priority().compareTo(b.priority());
		if (priorityResult != 0) {
			return priorityResult;
		}
		// otherwise, consider the complexity of the syntax
		int scoreResult = Integer.compare(calculateComplexityScore(a), calculateComplexityScore(b));
		if (scoreResult != 0) {
			return scoreResult;
		}
		// otherwise, order by hashcode
		return Integer.compare(a.hashCode(), b.hashCode());
	};

	final Set<I> syntaxes = new ConcurrentSkipListSet<>(SET_COMPARATOR);
	private volatile @Nullable Set<I> cache = null;

	public Collection<I> syntaxes() {
		if (cache == null) {
			cache = ImmutableSet.copyOf(syntaxes);
		}
		return cache;
	}

	public void add(I info) {
		syntaxes.add(info);
		cache = null;
	}

	public void remove(I info) {
		syntaxes.remove(info);
		cache = null;
	}

}
