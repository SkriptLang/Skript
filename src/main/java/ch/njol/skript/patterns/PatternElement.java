package ch.njol.skript.patterns;

import org.jetbrains.annotations.Nullable;

public abstract class PatternElement {

	@Nullable
	PatternElement next;

	@Nullable
	PatternElement originalNext;

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

	@Nullable
	public abstract MatchResult match(String expr, MatchResult matchResult);

	@Nullable
	protected MatchResult matchNext(String expr, MatchResult matchResult) {
		if (next == null) {
			return matchResult.exprOffset == expr.length() ? matchResult : null;
		}
		return next.match(expr, matchResult);
	}

	@Override
	public abstract String toString();

	public String toFullString() {
		StringBuilder stringBuilder = new StringBuilder(toString());
		PatternElement next = this;
		while ((next = next.originalNext) != null) {
			stringBuilder.append(next);
		}
		return stringBuilder.toString();
	}

}
