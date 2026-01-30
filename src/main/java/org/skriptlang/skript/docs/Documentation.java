package org.skriptlang.skript.docs;

import ch.njol.skript.doc.*;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collection;
import java.util.SequencedCollection;

/**
 * Describes an object holding documentation.
 */
public interface Documentation {

	/**
	 * Documentation to use when intentionally representing a {@link Documentable} object as having no documentation.
	 */
	Documentation NONE = builder()
		.build();

	/**
	 * @return A builder for creating documentation.
	 */
	static Builder builder() {
		return new DocumentationImpl.BuilderImpl();
	}

	/**
	 * Creates documentation consisting of the four standard properties.
	 * @param name The name to use.
	 * @param description The description to use.
	 * @param since The since to use.
	 * @param examples The examples to use.
	 * @return Documentation built from the provided standard properties.
	 */
	static Documentation of(String name, String description, String since, String... examples) {
		return builder()
			.name(name)
			.addDescription(description)
			.addSince(since)
			.addExamples(examples)
			.build();
	}

	/**
	 * Creates documentation from the documentation annotations of a class.
	 * @param clazz The class to read annotations from.
	 * @return Documentation created from any documentation annotations present on {@code clazz}.
	 *  If {@code clazz} has no documentation annotations, the result of building an empty builder is returned.
	 */
	static Documentation of(Class<?> clazz) {
		Builder builder = builder();

		Name name = clazz.getAnnotation(Name.class);
		if (name != null) {
			builder.name(name.value());
		}

		Description description = clazz.getAnnotation(Description.class);
		if (description != null) {
			builder.addDescription(description.value());
		}

		if (clazz.isAnnotationPresent(Examples.class)) {
			Examples examples = clazz.getAnnotation(Examples.class);
			builder.addExamples(examples.value());
		} else if (clazz.isAnnotationPresent(Example.Examples.class)) {
			// If there are multiple examples, they get containerized
			Example.Examples examples = clazz.getAnnotation(Example.Examples.class);
			builder.addExamples(Arrays.stream(examples.value())
				.map(Example::value)
				.toList());
		} else if (clazz.isAnnotationPresent(Example.class)) {
			// If the user adds just one example, it isn't containerized
			Example example = clazz.getAnnotation(Example.class);
			builder.addExamples(example.value());
		}

		Since since = clazz.getAnnotation(Since.class);
		if (since != null) {
			builder.addSince(since.value());
		}

		return builder.build();
	}

	/**
	 * @return A name for the thing represented by this documentation.
	 */
	String name();

	/**
	 * @return Paragraphs of description for the thing represented by this documentation.
	 */
	@Unmodifiable SequencedCollection<String> description();

	/**
	 * @return Examples for using the thing represented by this documentation.
	 */
	@Unmodifiable Collection<String> examples();

	/**
	 * @return Versions when the thing represented by this documentation was added or changed.
	 */
	@Unmodifiable SequencedCollection<String> since();

	/**
	 * Describes a builder for creating a {@link Documentation} object.
	 */
	interface Builder {

		/**
		 * Sets the name to use for the documentation.
		 * @param name The name to use.
		 * @return This builder.
		 */
		Builder name(String name);

		/**
		 * Adds one or more lines of description to the documentation.
		 * @param description The lines of description to add.
		 * @return This builder.
		 */
		Builder addDescription(String... description);

		/**
		 * Adds one or more lines of description to the documentation.
		 * @param description The lines of description to add.
		 * @return This builder.
		 */
		Builder addDescription(Collection<String> description);

		/**
		 * Clears all added lines of description.
		 * @return This builder.
		 */
		Builder clearDescription();

		/**
		 * Adds one or more examples to the documentation.
		 * @param examples The examples to add.
		 * @return This builder.
		 */
		Builder addExamples(String... examples);

		/**
		 * Adds one or more examples to the documentation.
		 * @param examples The examples to add.
		 * @return This builder.
		 */
		Builder addExamples(Collection<String> examples);

		/**
		 * Clears all added examples.
		 * @return This builder.
		 */
		Builder clearExamples();

		/**
		 * Adds one or more entries describing a version when the thing represented
		 *  by the documentation was added or changed.
		 * @param since The entries to add.
		 * @return This builder.
		 */
		Builder addSince(String... since);

		/**
		 * Adds one or more entries describing a version when the thing represented
		 *  by the documentation was added or changed.
		 * @param since The entries to add.
		 * @return This builder.
		 */
		Builder addSince(Collection<String> since);

		/**
		 * Clears all added since entries.
		 * @return This builder.
		 */
		Builder clearSince();

		/**
		 * @return A {@link Documentation} object representing the values set on this builder.
		 */
		Documentation build();

	}

}
