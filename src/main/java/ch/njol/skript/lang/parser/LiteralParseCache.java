package ch.njol.skript.lang.parser;

import ch.njol.skript.lang.ParseContext;

import java.util.HashSet;
import java.util.Set;

/**
 * A cache for literal data strings that failed {@code Classes.parse(data, Object.class, context)}.
 * <p>
 * Results of {@code Classes.parse} depend only on registered ClassInfo parsers and
 * {@link ParseContext}, neither of which change during script loading. This cache
 * is therefore safe to retain for an entire script load batch.
 */
public final class LiteralParseCache {

	/**
	 * A record representing a failed literal parse attempt.
	 *
	 * @param data The literal data string that failed to parse.
	 * @param context The parse context in which the parse was attempted.
	 */
	public record Failure(String data, ParseContext context) {}

	private final Set<Failure> failures = new HashSet<>();

	/**
	 * Returns true if the given literal data string is known to be unparsable in the given context.
	 */
	public boolean contains(Failure failure) {
		return failures.contains(failure);
	}

	/**
	 * Marks a literal parse attempt as failed.
	 */
	public void add(Failure failure) {
		failures.add(failure);
	}

	/**
	 * Clears all cached failures.
	 */
	public void clear() {
		failures.clear();
	}

}
