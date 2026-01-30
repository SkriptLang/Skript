package ch.njol.skript.lang.parser;

import ch.njol.skript.classes.ClassInfo;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * A scoped cache for failed expression parse attempts.
 * <p>
 * Each {@code parseExpression} call pushes a new scope via {@link #push()}.
 * Failures cached within that scope are isolated from parent scopes,
 * ensuring that recursive sub-expression parsing does not interfere
 * with the parent's cache. The scope is removed via {@link #pop()}
 * when the {@code parseExpression} call completes.
 */
public final class ExpressionParseCache {

	/**
	 * A record representing a failed expression parse attempt.
	 * Contains all inputs that affect whether {@code parseExpression}
	 * succeeds or fails for a given substring.
	 *
	 * @param substring The substring that was attempted to be parsed.
	 * @param effectiveFlags The effective parse flags (runtime flags masked by the type's flag mask).
	 * @param classes The ClassInfo types the expression was expected to match.
	 * @param isPlural Whether each type accepts plural expressions.
	 * @param isNullable Whether the type is nullable (optional).
	 * @param time The time state modifier for the type.
	 */
	public record Failure(
		String substring,
		int effectiveFlags,
		ClassInfo<?>[] classes,
		boolean[] isPlural,
		boolean isNullable,
		int time
	) {

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof Failure other))
				return false;
			return effectiveFlags == other.effectiveFlags
				&& isNullable == other.isNullable
				&& time == other.time
				&& substring.equals(other.substring)
				&& Arrays.equals(classes, other.classes)
				&& Arrays.equals(isPlural, other.isPlural);
		}

		@Override
		public int hashCode() {
			int hash = substring.hashCode() * 31 + effectiveFlags;
			hash = hash * 31 + Arrays.hashCode(classes);
			hash = hash * 31 + Arrays.hashCode(isPlural);
			hash = hash * 31 + Boolean.hashCode(isNullable);
			hash = hash * 31 + time;
			return hash;
		}

		@Override
		public String toString() {
			StringBuilder result = new StringBuilder("Failure{\"").append(substring).append("\" as ");
			for (int i = 0; i < classes.length; i++) {
				if (i > 0)
					result.append('/');
				result.append(classes[i].getCodeName());
				if (isPlural[i])
					result.append('s');
			}
			if (isNullable)
				result.append(" (nullable)");
			if (time != 0)
				result.append(" @").append(time);
			result.append(" flags=").append(effectiveFlags).append('}');
			return result.toString();
		}
	}

	private final Deque<Set<Failure>> stack = new ArrayDeque<>();

	/**
	 * Pushes a new cache scope. Call at the start of {@code parseExpression}.
	 */
	public void push() {
		stack.push(new HashSet<>());
	}

	/**
	 * Pops the current cache scope. Call at the end of {@code parseExpression}.
	 */
	public void pop() {
		stack.poll();
	}

	/**
	 * Checks whether the given failure is cached in the current scope.
	 */
	public boolean contains(Failure failure) {
		Set<Failure> current = stack.peek();
		return current != null && current.contains(failure);
	}

	/**
	 * Caches a failure in the current scope.
	 */
	public void add(Failure failure) {
		Set<Failure> current = stack.peek();
		if (current != null)
			current.add(failure);
	}

	/**
	 * Clears all scopes.
	 */
	public void clear() {
		stack.clear();
	}

}
