package org.skriptlang.skript.docs;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.SequencedCollection;

record DocumentationImpl(
	String name,
	SequencedCollection<String> description,
	SequencedCollection<String> examples,
	SequencedCollection<String> since
) implements Documentation {

	DocumentationImpl(String name,
					  Collection<String> description,
					  Collection<String> examples,
					  Collection<String> since) {
		this(name, ImmutableList.copyOf(description), ImmutableList.copyOf(examples), ImmutableList.copyOf(since));
	}

	static class BuilderImpl implements Documentation.Builder {

		private String name;
		private final Collection<String> description = new ArrayList<>();
		private final Collection<String> examples = new ArrayList<>();
		private final Collection<String> since = new ArrayList<>();

		@Override
		public Builder name(String name) {
			this.name = name;
			return this;
		}

		@Override
		public Builder addDescription(String... description) {
			this.description.addAll(Arrays.asList(description));
			return this;
		}

		@Override
		public Builder addDescription(Collection<String> description) {
			this.description.addAll(description);
			return this;
		}

		@Override
		public Builder clearDescription() {
			this.description.clear();
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
		public Documentation build() {
			return new DocumentationImpl(name, description, examples, since);
		}

	}

}
