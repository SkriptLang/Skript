package org.skriptlang.skript.docs;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.SequencedCollection;

record DocumentationImpl(
	@Nullable String id,
	String name,
	String description,
	Collection<String> examples,
	SequencedCollection<String> since,
	Collection<String> requirements,
	Collection<String> keywords,
	boolean deprecated
) implements Documentation {

	DocumentationImpl(@Nullable String id,
					  String name,
					  String description,
					  Collection<String> examples,
					  Collection<String> since,
					  Collection<String> requirements,
					  Collection<String> keywords,
					  boolean deprecated) {
		this(id, name, description, ImmutableList.copyOf(examples), ImmutableList.copyOf(since),
			ImmutableList.copyOf(requirements), ImmutableList.copyOf(keywords), deprecated);
	}

	static class BuilderImpl implements Documentation.Builder {

		private String id;
		private String name = "";
		private String description = "";
		private final Collection<String> examples = new ArrayList<>();
		private final Collection<String> since = new ArrayList<>();
		private final Collection<String> requirements = new ArrayList<>();
		private final Collection<String> keywords = new ArrayList<>();
		private boolean deprecated;

		@Override
		public Builder id(@Nullable String id) {
			this.id = id;
			return this;
		}

		@Override
		public Builder name(String name) {
			this.name = name;
			return this;
		}

		@Override
		public Builder description(String description) {
			this.description = description;
			return this;
		}

		@Override
		public Builder addExamples(String... examples) {
			this.examples.addAll(Arrays.asList(examples));
			return this;
		}

		@Override
		public Builder addExamples(Collection<String> examples) {
			this.examples.addAll(examples);
			return this;
		}

		@Override
		public Builder clearExamples() {
			this.examples.clear();
			return this;
		}

		@Override
		public Builder addSince(String... since) {
			this.since.addAll(Arrays.asList(since));
			return this;
		}

		@Override
		public Builder addSince(Collection<String> since) {
			this.since.addAll(since);
			return this;
		}

		@Override
		public Builder clearSince() {
			this.since.clear();
			return this;
		}

		@Override
		public Builder addRequirements(String... requirements) {
			this.requirements.addAll(Arrays.asList(requirements));
			return this;
		}

		@Override
		public Builder addRequirements(Collection<String> requirements) {
			this.requirements.addAll(requirements);
			return this;
		}

		@Override
		public Builder clearRequirements() {
			this.requirements.clear();
			return this;
		}

		@Override
		public Builder addKeywords(String... keywords) {
			this.keywords.addAll(Arrays.asList(keywords));
			return this;
		}

		@Override
		public Builder addKeywords(Collection<String> keywords) {
			this.keywords.addAll(keywords);
			return this;
		}

		@Override
		public Builder clearKeywords() {
			this.keywords.clear();
			return this;
		}

		@Override
		public Builder deprecated() {
			this.deprecated = true;
			return this;
		}

		@Override
		public Documentation build() {
			return new DocumentationImpl(id, name, description, examples, since, requirements, keywords, deprecated);
		}

	}

}
