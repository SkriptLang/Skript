package org.skriptlang.skript.docs;

import ch.njol.skript.doc.*;
import org.jetbrains.annotations.Contract;
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
	 * @see #originOnly(Origin)
	 */
	Documentation NONE = originOnly(Origin.UNKNOWN);

	/**
	 * Constructs a documentation to use when intentionally representing a {@link Documentable} object as having no documentation
	 *  other than an origin.
	 * @param origin The origin to use.
	 * @return A documentation.
	 * @see #NONE
	 */
	static Documentation originOnly(Origin origin) {
		return new DocumentationImpl.OriginOnly(origin);
	}

	/**
	 * Used for determining whether a documentation
	 * @param documentation The documentation to check.
	 * @return Whether {@code documentation} represents an intentionally
	 * @see #NONE
	 * @see #originOnly(Origin)
	 */
	static boolean isNoDocs(Documentation documentation) {
		return documentation instanceof DocumentationImpl.OriginOnly;
	}

	/**
	 * @return A builder for creating documentation.
	 */
	@Contract("-> new")
	static Builder<?> builder() {
		return new DocumentationImpl.BuilderImpl<>();
	}

	/**
	 * Creates documentation consisting of the four standard properties.
	 * @param name The name to use.
	 * @param description The description to use.
	 * @param since The since entry to use.
	 * @param examples The examples to use.
	 * @return Documentation built from the provided standard properties.
	 */
	@Contract("_, _, _, _ -> new")
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
	@Contract("_ -> new")
	static Documentation of(Class<?> clazz) {
		NoDoc noDoc = clazz.getAnnotation(NoDoc.class);
		if (noDoc != null) {
			return NONE;
		}

		var builder = builder();

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

		// TODO @Events

		// TODO RelatedProperty

		return builder.build();
	}

	/**
	 * @return An origin identifying the provider of the thing represented by this documentation.
	 */
	Origin origin();

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
	 * Converts this documentation back into a builder.
	 * @return A builder capable of building this documentation.
	 */
	Builder<?> toBuilder();

	/**
	 * Describes a builder for creating a {@link Documentation} object.
	 */
	interface Builder<B extends Builder<B>> {

		/**
		 * Sets the origin to use for the documentation.
		 * @param origin The origin to use.
		 * @return This builder.
		 * @see Documentation#origin()
		 */
		@Contract(value = "_ -> this", mutates = "this")
		B origin(Origin origin);

		/**
		 * Sets the identifier to use for the documentation.
		 * @param id The identifier to use. Use {@code null} to unset it.
		 * @return This builder.
		 * @see Documentation#id()
		 */
		@Contract(value = "_ -> this", mutates = "this")
		B id(@Nullable String id);

		/**
		 * Sets the name to use for the documentation.
		 * @param name The name to use.
		 * @return This builder.
		 * @see Documentation#name()
		 */
		@Contract(value = "_ -> this", mutates = "this")
		B name(String name);

		/**
		 * Sets the description to use for the documentation.
		 * @param description The description to use.
		 * @return This builder.
		 * @see Documentation#description()
		 */
		@Contract(value = "_ -> this", mutates = "this")
		B description(String description);

		/**
		 * Adds an example to the documentation.
		 * @param example The example to add.
		 * @return This builder.
		 * @see Documentation#examples()
		 */
		@Contract(value = "_ -> this", mutates = "this")
		B addExample(String example);

		/**
		 * Adds one or more examples to the documentation.
		 * @param examples The examples to add.
		 * @return This builder.
		 * @see Documentation#examples()
		 */
		@Contract(value = "_ -> this", mutates = "this")
		B addExamples(String... examples);

		/**
		 * Adds one or more examples to the documentation.
		 * @param examples The examples to add.
		 * @return This builder.
		 * @see Documentation#examples()
		 */
		@Contract(value = "_ -> this", mutates = "this")
		B addExamples(Collection<String> examples);

		/**
		 * Clears all added examples.
		 * @return This builder.
		 * @see Documentation#examples()
		 */
		@Contract(value = "-> this", mutates = "this")
		B clearExamples();

		/**
		 * Adds an entry describing a version when the thing represented
		 *  by the documentation was added or changed.
		 * @param since The entry to add.
		 * @return This builder.
		 * @see Documentation#since()
		 */
		@Contract(value = "_ -> this", mutates = "this")
		B addSince(String since);

		/**
		 * Adds one or more entries describing a version when the thing represented
		 *  by the documentation was added or changed.
		 * @param since The entries to add.
		 * @return This builder.
		 * @see Documentation#since()
		 */
		@Contract(value = "_ -> this", mutates = "this")
		B addSince(String... since);

		/**
		 * Adds one or more entries describing a version when the thing represented
		 *  by the documentation was added or changed.
		 * @param since The entries to add.
		 * @return This builder.
		 * @see Documentation#since()
		 */
		@Contract(value = "_ -> this", mutates = "this")
		B addSince(Collection<String> since);

		/**
		 * Clears all added since entries.
		 * @return This builder.
		 * @see Documentation#since()
		 */
		@Contract(value = "-> this", mutates = "this")
		B clearSince();

		/**
		 * Adds a requirement to the documentation.
		 * @param requirement The requirement to add.
		 * @return This builder.
		 * @see Documentation#requirements()
		 */
		@Contract(value = "_ -> this", mutates = "this")
		B addRequirement(String requirement);

		/**
		 * Adds one or more requirements to the documentation.
		 * @param requirements The requirements to add.
		 * @return This builder.
		 * @see Documentation#requirements()
		 */
		@Contract(value = "_ -> this", mutates = "this")
		B addRequirements(String... requirements);

		/**
		 * Adds one or more requirements to the documentation.
		 * @param requirements The requirements to add.
		 * @return This builder.
		 * @see Documentation#requirements()
		 */
		@Contract(value = "_ -> this", mutates = "this")
		B addRequirements(Collection<String> requirements);

		/**
		 * Clears all added requirements.
		 * @return This builder.
		 * @see Documentation#requirements()
		 */
		@Contract(value = "-> this", mutates = "this")
		B clearRequirements();

		/**
		 * Adds a keyword to the documentation.
		 * @param keyword The keyword to add.
		 * @return This builder.
		 * @see Documentation#keywords()
		 */
		@Contract(value = "_ -> this", mutates = "this")
		B addKeyword(String keyword);

		/**
		 * Adds one or more keywords to the documentation.
		 * @param keywords The keywords to add.
		 * @return This builder.
		 * @see Documentation#keywords()
		 */
		@Contract(value = "_ -> this", mutates = "this")
		B addKeywords(String... keywords);

		/**
		 * Adds one or more keywords to the documentation.
		 * @param keywords The keywords to add.
		 * @return This builder.
		 * @see Documentation#keywords()
		 */
		@Contract(value = "_ -> this", mutates = "this")
		B addKeywords(Collection<String> keywords);

		/**
		 * Clears all added keywords
		 * @return This builder.
		 * @see Documentation#keywords()
		 */
		@Contract(value = "-> this", mutates = "this")
		B clearKeywords();

		/**
		 * Marks that the thing being represented by the documentation is considered deprecated.
		 * @return This builder.
		 * @see Documentation#deprecated()
		 */
		@Contract(value = "-> this", mutates = "this")
		B deprecated();

		/**
		 * @return A {@link Documentation} object representing the values set on this builder.
		 */
		@Contract("-> new")
		Documentation build();

		/**
		 * Applies the values of this builder onto <code>builder</code>.
		 * @param builder The builder to apply values onto.
		 */
		void applyTo(Builder<?> builder);

	}

}
