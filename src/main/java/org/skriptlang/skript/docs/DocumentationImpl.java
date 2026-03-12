package org.skriptlang.skript.docs;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
	boolean deprecated,
	Collection<Documentable> additionalData
) implements Documentation {

	/**
	 * An {@link Documentation#additionalData()} to be added to {@link Documentation}s that should not be written.
	 */
	static final Documentable SKIP_WRITE = (adapter) -> { };

	DocumentationImpl(Origin origin,
					  @Nullable String id,
					  String name,
					  String description,
					  Collection<String> examples,
					  Collection<String> since,
					  Collection<String> requirements,
					  Collection<String> keywords,
					  boolean deprecated,
					  Collection<Documentable> additionalData) {
		this(origin, id, name, description, ImmutableList.copyOf(examples), ImmutableList.copyOf(since),
			ImmutableList.copyOf(requirements), ImmutableList.copyOf(keywords), deprecated,
			ImmutableList.copyOf(additionalData));
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
		additionalData.forEach(builder::addData);
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
		private final Collection<Documentable> additionalData = new ArrayList<>();

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
		public B addData(Documentable documentable) {
			this.additionalData.add(documentable);
			return (B) this;
		}

		@Override
		public B clearData() {
			this.additionalData.clear();
			return (B) this;
		}

		@Override
		public Documentation build() {
			return new DocumentationImpl(origin, id, name, description, examples, since, requirements, keywords,
				deprecated, additionalData);
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

}
