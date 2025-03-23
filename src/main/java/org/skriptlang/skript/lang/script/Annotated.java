package org.skriptlang.skript.lang.script;

import ch.njol.skript.patterns.MatchResult;
import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.patterns.SkriptPattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @see Annotation
 */
public interface Annotated {

	/**
	 * This is not guaranteed to return the live set of annotations;
	 * it may return an unmodifiable view.
	 *
	 * @return A view into the actual annotations on this
	 */
	@NotNull Collection<Annotation> annotations();

	/**
	 * Checks whether an annotation instance is visible to the parser at this stage.
	 *
	 * @param annotation The annotation to test
	 * @return Whether this annotation is present
	 */
	default boolean hasAnnotation(Annotation annotation) {
		return this.annotations().contains(annotation);
	}

	/**
	 * Checks whether an annotation by exact text is visible to the parser at this stage.
	 *
	 * @param text The exact content of the annotation
	 * @return Whether an annotation with this content is present
	 */
	default boolean hasAnnotation(String text) {
		if (this.annotations().isEmpty())
			return false;
		for (Annotation annotation : this.annotations()) {
			if (annotation.valueEquals(text))
				return true;
		}
		return false;
	}

	/**
	 * A helper method for {@link #hasAnnotationMatching(SkriptPattern)} accepting a non-compiled pattern.
	 */
	default boolean hasAnnotationMatching(String pattern) {
		return this.hasAnnotationMatching(PatternCompiler.compile(pattern));
	}

	/**
	 * Whether there is an annotation present whose content matches the given pattern.
	 *
	 * @param pattern A pattern matching the content of an annotation
	 * @return Whether any annotation matching this pattern was present
	 */
	default boolean hasAnnotationMatching(SkriptPattern pattern) {
		return this.getAnnotationMatching(pattern) != null;
	}

	/**
	 * A helper method for {@link #getAnnotationsMatching(SkriptPattern)} accepting a non-compiled pattern.
	 */
	default Annotation.Match @NotNull [] getAnnotationsMatching(String pattern) {
		return this.getAnnotationsMatching(PatternCompiler.compile(pattern));
	}

	/**
	 * Finds all visible annotations whose content matches the provided pattern.
	 *
	 * @param pattern A pattern matching the content of an annotation
	 * @return A set of matches, including the parse results
	 */
	default Annotation.Match @NotNull [] getAnnotationsMatching(SkriptPattern pattern) {
		if (this.annotations().isEmpty())
			return new Annotation.Match[0];
		List<Annotation.Match> matches = new ArrayList<>(this.annotations().size());
		for (Annotation annotation : this.annotations()) {
			MatchResult result = pattern.match(annotation.value());
			if (result != null)
				matches.add(new Annotation.Match(annotation, result));
		}
		return matches.toArray(new Annotation.Match[0]);
	}

	/**
	 * A helper method for {@link #getAnnotationMatching(SkriptPattern)} accepting a non-compiled pattern.
	 */
	default @Nullable Annotation.Match getAnnotationMatching(String pattern) {
		return this.getAnnotationMatching(PatternCompiler.compile(pattern));
	}

	/**
	 * Finds the first annotation whose content matches the given pattern, or nothing if none match.
	 *
	 * @param pattern A pattern matching the content of an annotation
	 * @return A matched annotation and the parse result
	 */
	default @Nullable Annotation.Match getAnnotationMatching(SkriptPattern pattern) {
		if (this.annotations().isEmpty())
			return null;
		for (Annotation annotation : this.annotations()) {
			MatchResult result = pattern.match(annotation.value());
			if (result != null)
				return new Annotation.Match(annotation, result);
		}
		return null;
	}

	/**
	 * Returns a modifiable hash-based copy of the current annotation set.
	 *
	 * @return A copy of the current annotations set.
	 */
	default Set<Annotation> copyAnnotations() {
		return new HashSet<>(this.annotations());
	}

}
