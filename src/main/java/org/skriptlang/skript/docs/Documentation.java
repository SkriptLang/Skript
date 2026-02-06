package org.skriptlang.skript.docs;

import ch.njol.skript.doc.*;
import org.jetbrains.annotations.Nullable;
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
	 * @param since The since entry to use.
	 * @param examples The examples to use.
	 * @return Documentation built from the provided standard properties.
	 */
	static Documentation of(String name, String description, String since, String... examples) {
		return builder()
			.name(name)
			.description(description)
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

		DocumentationId id = clazz.getAnnotation(DocumentationId.class);
		if (id != null) {
			builder.id(id.value());
		}

		Name name = clazz.getAnnotation(Name.class);
		if (name != null) {
			builder.name(name.value());
		}

		Description description = clazz.getAnnotation(Description.class);
		if (description != null) {
			builder.description(String.join("\n", description.value()));
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

		RequiredPlugins requiredPlugins = clazz.getAnnotation(RequiredPlugins.class);
		if (requiredPlugins != null) {
			builder.addRequirements(requiredPlugins.value());
		}

		Keywords keywords = clazz.getAnnotation(Keywords.class);
		if (keywords != null) {
			builder.addKeywords(keywords.value());
		}

		Deprecated deprecated = clazz.getAnnotation(Deprecated.class);
		if (deprecated != null) {
			builder.deprecated();
		}

		return builder.build();
	}

	/**
	 * @return An identifier for referencing the thing represented by this documentation.
	 */
	@Nullable String id();

	/**
	 * @return A name for the thing represented by this documentation.
	 */
	String name();

	/**
	 * @return A description for the thing represented by this documentation.
	 */
	@Unmodifiable String description();

	/**
	 * @return Examples for using the thing represented by this documentation.
	 */
	@Unmodifiable Collection<String> examples();

	/**
	 * @return Versions when the thing represented by this documentation was added or changed.
	 */
	@Unmodifiable SequencedCollection<String> since();

	/**
	 * @return Requirements for using the thing represented by this documentation.
	 */
	@Unmodifiable Collection<String> requirements();

	/**
	 * @return Keywords for referencing the thing represented by this documentation.
	 */
	@Unmodifiable Collection<String> keywords();

	/**
	 * @return Whether the thing represented by this documentation is considered deprecated.
	 */
	boolean deprecated();

	/**
	 * Describes a builder for creating a {@link Documentation} object.
	 */
	interface Builder {

		/**
		 * Sets the identifier to use for the documentation.
		 * @param id The identifier to use. Use {@code null} to unset it.
		 * @return This builder.
		 * @see Documentation#id()
		 */
		Builder id(@Nullable String id);

		/**
		 * Sets the name to use for the documentation.
		 * @param name The name to use.
		 * @return This builder.
		 * @see Documentation#name()
		 */
		Builder name(String name);

		/**
		 * Sets the description to use for the documentation.
		 * @param description The description to use.
		 * @return This builder.
		 * @see Documentation#description()
		 */
		Builder description(String description);

		/**
		 * Adds an example to the documentation.
		 * @param example The example to add.
		 * @return This builder.
		 * @see Documentation#examples()
		 */
		Builder addExample(String example);

		/**
		 * Adds one or more examples to the documentation.
		 * @param examples The examples to add.
		 * @return This builder.
		 * @see Documentation#examples()
		 */
		Builder addExamples(String... examples);

		/**
		 * Adds one or more examples to the documentation.
		 * @param examples The examples to add.
		 * @return This builder.
		 * @see Documentation#examples()
		 */
		Builder addExamples(Collection<String> examples);

		/**
		 * Clears all added examples.
		 * @return This builder.
		 * @see Documentation#examples()
		 */
		Builder clearExamples();

		/**
		 * Adds an entry describing a version when the thing represented
		 *  by the documentation was added or changed.
		 * @param since The entry to add.
		 * @return This builder.
		 * @see Documentation#since()
		 */
		Builder addSince(String since);

		/**
		 * Adds one or more entries describing a version when the thing represented
		 *  by the documentation was added or changed.
		 * @param since The entries to add.
		 * @return This builder.
		 * @see Documentation#since()
		 */
		Builder addSince(String... since);

		/**
		 * Adds one or more entries describing a version when the thing represented
		 *  by the documentation was added or changed.
		 * @param since The entries to add.
		 * @return This builder.
		 * @see Documentation#since()
		 */
		Builder addSince(Collection<String> since);

		/**
		 * Clears all added since entries.
		 * @return This builder.
		 * @see Documentation#since()
		 */
		Builder clearSince();

		/**
		 * Adds a requirement to the documentation.
		 * @param requirement The requirement to add.
		 * @return This builder.
		 * @see Documentation#requirements()
		 */
		Builder addRequirement(String requirement);

		/**
		 * Adds one or more requirements to the documentation.
		 * @param requirements The requirements to add.
		 * @return This builder.
		 * @see Documentation#requirements()
		 */
		Builder addRequirements(String... requirements);

		/**
		 * Adds one or more requirements to the documentation.
		 * @param requirements The requirements to add.
		 * @return This builder.
		 * @see Documentation#requirements()
		 */
		Builder addRequirements(Collection<String> requirements);

		/**
		 * Clears all added requirements.
		 * @return This builder.
		 * @see Documentation#requirements()
		 */
		Builder clearRequirements();

		/**
		 * Adds a keyword to the documentation.
		 * @param keyword The keyword to add.
		 * @return This builder.
		 * @see Documentation#keywords()
		 */
		Builder addKeyword(String keyword);

		/**
		 * Adds one or more keywords to the documentation.
		 * @param keywords The keywords to add.
		 * @return This builder.
		 * @see Documentation#keywords()
		 */
		Builder addKeywords(String... keywords);

		/**
		 * Adds one or more keywords to the documentation.
		 * @param keywords The keywords to add.
		 * @return This builder.
		 * @see Documentation#keywords()
		 */
		Builder addKeywords(Collection<String> keywords);

		/**
		 * Clears all added keywords
		 * @return This builder.
		 * @see Documentation#keywords()
		 */
		Builder clearKeywords();

		/**
		 * Marks that the thing being represented by the documentation is considered deprecated.
		 * @return This builder.
		 * @see Documentation#deprecated()
		 */
		Builder deprecated();

		/**
		 * @return A {@link Documentation} object representing the values set on this builder.
		 */
		Documentation build();

	}

}
