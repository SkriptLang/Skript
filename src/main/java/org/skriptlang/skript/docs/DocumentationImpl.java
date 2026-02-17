package org.skriptlang.skript.docs;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.SequencedCollection;

record DocumentationImpl(
	Origin origin,
	@Nullable String id,
	String name,
	String description,
	Collection<String> examples,
	SequencedCollection<String> since,
	Collection<String> requirements,
	Collection<String> keywords,
	boolean deprecated
) implements Documentation {

	DocumentationImpl(Origin origin,
					  @Nullable String id,
					  String name,
					  String description,
					  Collection<String> examples,
					  Collection<String> since,
					  Collection<String> requirements,
					  Collection<String> keywords,
					  boolean deprecated) {
		this(origin, id, name, description, ImmutableList.copyOf(examples), ImmutableList.copyOf(since),
			ImmutableList.copyOf(requirements), ImmutableList.copyOf(keywords), deprecated);
	}

	@Override
	public Builder<?> toBuilder() {
		var builder = new BuilderImpl<>()
			.origin(origin)
			.id(id)
			.name(name)
			.description(description)
			.addExamples(examples)
			.addSince(since)
			.addRequirements(requirements)
			.addKeywords(keywords);
		if (deprecated) {
			builder.deprecated();
		}
		return builder;
	}

	@SuppressWarnings("unchecked")
	static class BuilderImpl<B extends BuilderImpl<B>> implements Documentation.Builder<B> {

		private Origin origin = Origin.UNKNOWN;
		private String id;
		private String name = "";
		private String description = "";
		private final Collection<String> examples = new ArrayList<>();
		private final Collection<String> since = new ArrayList<>();
		private final Collection<String> requirements = new ArrayList<>();
		private final Collection<String> keywords = new ArrayList<>();
		private boolean deprecated;

		@Override
		public B origin(Origin origin) {
			this.origin = origin;
			return (B) this;
		}

		@Override
		public B id(@Nullable String id) {
			this.id = id;
			return (B) this;
		}

		@Override
		public B name(String name) {
			this.name = name;
			return (B) this;
		}

		@Override
		public B description(String description) {
			this.description = description;
			return (B) this;
		}

		@Override
		public B addExample(String example) {
			this.examples.add(example);
			return (B) this;
		}

		@Override
		public B addExamples(String... examples) {
			this.examples.addAll(Arrays.asList(examples));
			return (B) this;
		}

		@Override
		public B addExamples(Collection<String> examples) {
			this.examples.addAll(examples);
			return (B) this;
		}

		@Override
		public B clearExamples() {
			this.examples.clear();
			return (B) this;
		}

		@Override
		public B addSince(String since) {
			this.since.add(since);
			return (B) this;
		}

		@Override
		public B addSince(String... since) {
			this.since.addAll(Arrays.asList(since));
			return (B) this;
		}

		@Override
		public B addSince(Collection<String> since) {
			this.since.addAll(since);
			return (B) this;
		}

		@Override
		public B clearSince() {
			this.since.clear();
			return (B) this;
		}

		@Override
		public B addRequirement(String requirement) {
			this.requirements.add(requirement);
			return (B) this;
		}

		@Override
		public B addRequirements(String... requirements) {
			this.requirements.addAll(Arrays.asList(requirements));
			return (B) this;
		}

		@Override
		public B addRequirements(Collection<String> requirements) {
			this.requirements.addAll(requirements);
			return (B) this;
		}

		@Override
		public B clearRequirements() {
			this.requirements.clear();
			return (B) this;
		}

		@Override
		public B addKeyword(String keyword) {
			this.keywords.add(keyword);
			return (B) this;
		}

		@Override
		public B addKeywords(String... keywords) {
			this.keywords.addAll(Arrays.asList(keywords));
			return (B) this;
		}

		@Override
		public B addKeywords(Collection<String> keywords) {
			this.keywords.addAll(keywords);
			return (B) this;
		}

		@Override
		public B clearKeywords() {
			this.keywords.clear();
			return (B) this;
		}

		@Override
		public B deprecated() {
			this.deprecated = true;
			return (B) this;
		}

		@Override
		public Documentation build() {
			return new DocumentationImpl(origin, id, name, description, examples, since, requirements, keywords, deprecated);
		}

		@Override
		public void applyTo(Builder<?> builder) {
			builder.origin(origin)
				.id(id)
				.name(name)
				.description(description)
				.addExamples(examples)
				.addSince(since)
				.addRequirements(requirements)
				.addKeywords(keywords);
			if (deprecated) {
				builder.deprecated();
			}
		}

	}

	record OriginOnly(Origin origin) implements Documentation {

		@Override
		public @Nullable String id() {
			return null;
		}

		@Override
		public String name() {
			return "";
		}

		@Override
		public @Unmodifiable String description() {
			return "";
		}

		@Override
		public @Unmodifiable Collection<String> examples() {
			return List.of();
		}

		@Override
		public @Unmodifiable SequencedCollection<String> since() {
			return List.of();
		}

		@Override
		public @Unmodifiable Collection<String> requirements() {
			return List.of();
		}

		@Override
		public @Unmodifiable Collection<String> keywords() {
			return List.of();
		}

		@Override
		public boolean deprecated() {
			return false;
		}

		@Override
		public Builder<?> toBuilder() {
			return new OriginOnlyBuilder()
				.origin(origin);
		}

		/**
		 * A builder that will return an OriginOnly documentation if only the origin is modified.
		 */
		private static class OriginOnlyBuilder implements Documentation.Builder<OriginOnlyBuilder> {

			private Origin origin;
			private @Nullable Builder<?> builder;

			private Builder<?> builder() {
				if (builder == null) {
					builder = new BuilderImpl<>();
				}
				return builder;
			}

			@Override
			public OriginOnlyBuilder origin(Origin origin) {
				this.origin = origin;
				return this;
			}

			@Override
			public OriginOnlyBuilder id(@Nullable String id) {
				builder().id(id);
				return this;
			}

			@Override
			public OriginOnlyBuilder name(String name) {
				builder().name(name);
				return this;
			}

			@Override
			public OriginOnlyBuilder description(String description) {
				builder().description(description);
				return this;
			}

			@Override
			public OriginOnlyBuilder addExample(String example) {
				builder().addExample(example);
				return this;
			}

			@Override
			public OriginOnlyBuilder addExamples(String... examples) {
				builder().addExamples(examples);
				return this;
			}

			@Override
			public OriginOnlyBuilder addExamples(Collection<String> examples) {
				builder().addExamples(examples);
				return this;
			}

			@Override
			public OriginOnlyBuilder clearExamples() {
				builder().clearExamples();
				return this;
			}

			@Override
			public OriginOnlyBuilder addSince(String since) {
				builder().addSince(since);
				return this;
			}

			@Override
			public OriginOnlyBuilder addSince(String... since) {
				builder().addSince(since);
				return this;
			}

			@Override
			public OriginOnlyBuilder addSince(Collection<String> since) {
				builder().addSince(since);
				return this;
			}

			@Override
			public OriginOnlyBuilder clearSince() {
				builder().clearSince();
				return this;
			}

			@Override
			public OriginOnlyBuilder addRequirement(String requirement) {
				builder().addRequirement(requirement);
				return this;
			}

			@Override
			public OriginOnlyBuilder addRequirements(String... requirements) {
				builder().addRequirements(requirements);
				return this;
			}

			@Override
			public OriginOnlyBuilder addRequirements(Collection<String> requirements) {
				builder().addRequirements(requirements);
				return this;
			}

			@Override
			public OriginOnlyBuilder clearRequirements() {
				builder().clearRequirements();
				return this;
			}

			@Override
			public OriginOnlyBuilder addKeyword(String keyword) {
				builder().addKeyword(keyword);
				return this;
			}

			@Override
			public OriginOnlyBuilder addKeywords(String... keywords) {
				builder().addKeywords(keywords);
				return this;
			}

			@Override
			public OriginOnlyBuilder addKeywords(Collection<String> keywords) {
				builder().addKeywords(keywords);
				return this;
			}

			@Override
			public OriginOnlyBuilder clearKeywords() {
				builder().clearKeywords();
				return this;
			}

			@Override
			public OriginOnlyBuilder deprecated() {
				builder().deprecated();
				return this;
			}

			@Override
			public Documentation build() {
				if (builder != null) {
					return builder
						.origin(origin)
						.build();
				}
				return new OriginOnly(origin);
			}

			@Override
			public void applyTo(Builder<?> builder) {
				builder.origin(origin);
				if (this.builder != null) {
					this.builder.applyTo(builder);
				}
			}

		}

	}

}
