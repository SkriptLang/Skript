package org.skriptlang.skript.lang.script;

import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.patterns.MatchResult;
import ch.njol.skript.patterns.SkriptPattern;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * An @annotation is a piece of metadata attached to code.
 * By itself, it does nothing. There are no errors or penalties for writing a bogus
 * or unknown annotation (in this case it acts like a comment does).
 * An annotation is visible to (and only to) the line of code it is placed before:
 *
 * <pre>{@code
 * 	on event:
 *      @my cool annotation
 * 		my annotated effect
 * }</pre>
 * <p>
 * If multiple annotations are placed before a line of code then all will be visible
 * to that line.
 *
 * <pre>{@code
 * 	on event:
 *      @my cool annotation
 *      @another annotation
 * 		my annotated effect
 * }</pre>
 * <p>
 * Annotations are visible to every element within the line, and are discarded after the line has been
 * parsed.
 *
 * <pre>{@code
 * 	on event:
 *      @my cool annotation
 * 		my annotated effect
 * 		my not-so-annotated effect
 * }</pre>
 * <p>
 * It is up to elements within a line how to (or whether to) interact with annotations.
 * Some may have a global effect on any syntax (e.g. suppressing a warning, changing parsing order)
 * whereas others might be relevant ONLY to one syntax.
 * <p>
 * This interface represents a post-parsing annotation.
 */
public interface Annotation extends CharSequence {

	@Contract("_ -> new")
	static @NotNull Annotation create(String text) {
		return new SimpleAnnotation(text);
	}

	static boolean isAnnotationPresent(String text) {
		return ParserInstance.get().hasAnnotation(text);
	}

	static boolean isAnnotationPresent(SkriptPattern pattern) {
		return ParserInstance.get().hasAnnotationMatching(pattern);
	}

	/**
	 * @return The annotation's content after the initial '@'
	 */
	@NotNull String value();

	/**
	 * Whether the content of this annotation equals the given value.
	 */
	default boolean valueEquals(@Nullable String value) {
		return this.value().equals(value);
	}

	record Match(Annotation annotation, MatchResult result) implements Annotation {

		@Override
		public @NotNull String value() {
			return annotation.value();
		}

		@Override
		public int length() {
			return annotation.length();
		}

		@Override
		public char charAt(int index) {
			return annotation.charAt(index);
		}

		@Override
		public @NotNull CharSequence subSequence(int start, int end) {
			return annotation.subSequence(start, end);
		}

		@Override
		public @NotNull String toString() {
			return annotation.toString();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Annotation other)
				return Objects.equals(this.value(), other.value())
					|| annotation.equals(obj);
			return false;
		}

	}

}

record SimpleAnnotation(String value) implements Annotation {

	SimpleAnnotation(@NotNull String value) {
		this.value = value;
	}

	@Override
	public @NotNull String value() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Annotation annotation)
			return Objects.equals(value, annotation.value());
		return false;
	}

	@Override
	public @NotNull String toString() {
		return '@' + value;
	}

	@Override
	public int length() {
		return 1 + value.length();
	}

	@Override
	public char charAt(int index) {
		if (index == 0)
			return '@';
		return value.charAt(index - 1);
	}

	@Override
	public @NotNull String subSequence(int start, int end) {
		if (start == 0)
			return '@' + value.substring(0, end - 1);
		return value.substring(start - 1, end - 1);
	}

}
