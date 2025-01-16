package org.skriptlang.skript.bukkit.registration;

import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptEvent.ListeningBehavior;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.util.Priority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

final class BukkitSyntaxInfosImpl {

	static final class EventImpl<E extends SkriptEvent> implements Event<E> {

		private final SyntaxInfo<E> defaultInfo;
		private final ListeningBehavior listeningBehavior;
		private final String name;
		private final String id;
		private final @Nullable String since;
		private final @Nullable String documentationId;
		private final Collection<String> description;
		private final Collection<String> examples;
		private final Collection<String> keywords;
		private final Collection<String> requiredPlugins;
		private final Collection<Class<? extends org.bukkit.event.Event>> events;

		EventImpl(
			SyntaxInfo<E> defaultInfo, ListeningBehavior listeningBehavior, String name,
			@Nullable String since, @Nullable String documentationId, Collection<String> description, Collection<String> examples,
			Collection<String> keywords, Collection<String> requiredPlugins, Collection<Class<? extends org.bukkit.event.Event>> events
		) {
			this.defaultInfo = defaultInfo;
			this.listeningBehavior = listeningBehavior;
			this.name = name.startsWith("*") ? name.substring(1) : "On " + name;
			this.id = name.toLowerCase(Locale.ENGLISH)
					.replaceAll("[#'\"<>/&]", "")
					.replaceAll("\\s+", "_");
			this.since = since;
			this.documentationId = documentationId;
			this.description = ImmutableList.copyOf(description);
			this.examples = ImmutableList.copyOf(examples);
			this.keywords = ImmutableList.copyOf(keywords);
			this.requiredPlugins = ImmutableList.copyOf(requiredPlugins);
			this.events = ImmutableList.copyOf(events);
		}

		@Override
		public Builder<? extends Builder<?, E>, E> toBuilder() {
			var builder = new BuilderImpl<>(type(), name);
			defaultInfo.toBuilder().applyTo(builder);
			builder.listeningBehavior(listeningBehavior);
			builder.documentationId(id);
			if (since != null) {
				builder.since(since);
			}
			if (documentationId != null) {
				builder.documentationId(documentationId);
			}
			builder.addDescription(description);
			builder.addExamples(examples);
			builder.addKeywords(keywords);
			builder.addRequiredPlugins(requiredPlugins);
			builder.addEvents(events);
			return builder;
		}

		@Override
		public ListeningBehavior listeningBehavior() {
			return listeningBehavior;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public String id() {
			return id;
		}

		@Override
		@Nullable
		public String since() {
			return since;
		}

		@Override
		@Nullable
		public String documentationId() {
			return documentationId;
		}

		@Override
		public Collection<String> description() {
			return description;
		}

		@Override
		public Collection<String> examples() {
			return examples;
		}

		@Override
		public Collection<String> keywords() {
			return keywords;
		}

		@Override
		public Collection<String> requiredPlugins() {
			return requiredPlugins;
		}

		@Override
		public Collection<Class<? extends org.bukkit.event.Event>> events() {
			return events;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			return (other instanceof Event<?> event) &&
					Objects.equals(defaultInfo, other) &&
					Objects.equals(name(), event.name()) &&
					Objects.equals(events(), event.events());
		}

		@Override
		public int hashCode() {
			return Objects.hash(defaultInfo, name(), events());
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.add("origin", origin())
					.add("type", type())
					.add("patterns", patterns())
					.add("priority", priority())
					.add("name", name())
					.add("events", events())
					.toString();
		}

		//
		// default methods
		//

		@Override
		public SyntaxOrigin origin() {
			return defaultInfo.origin();
		}

		@Override
		public Class<E> type() {
			return defaultInfo.type();
		}

		@Override
		public E instance() {
			return defaultInfo.instance();
		}

		@Override
		@Unmodifiable
		public Collection<String> patterns() {
			return defaultInfo.patterns();
		}

		@Override
		public Priority priority() {
			return defaultInfo.priority();
		}

		@SuppressWarnings("unchecked")
		static final class BuilderImpl<B extends Event.Builder<B, E>, E extends SkriptEvent> implements Event.Builder<B, E> {

			private final SyntaxInfo.Builder<?, E> defaultBuilder;
			private ListeningBehavior listeningBehavior = ListeningBehavior.UNCANCELLED;
			private final String name;
			private @Nullable String since;
			private @Nullable String documentationId;
			private final List<String> description = new ArrayList<>();
			private final List<String> examples = new ArrayList<>();
			private final List<String> keywords = new ArrayList<>();
			private final List<String> requiredPlugins = new ArrayList<>();
			private final List<Class<? extends org.bukkit.event.Event>> events = new ArrayList<>();

			BuilderImpl(Class<E> type, String name) {
				this.defaultBuilder = SyntaxInfo.builder(type);
				this.name = name;
			}

			@Override
			public B listeningBehavior(ListeningBehavior listeningBehavior) {
				this.listeningBehavior = listeningBehavior;
				return (B) this;
			}

			@Override
			public B since(String since) {
				this.since = since;
				return (B) this;
			}

			@Override
			public B documentationId(String documentationId) {
				this.documentationId = documentationId;
				return (B) this;
			}

			@Override
			public B addDescription(String description) {
				this.description.add(description);
				return (B) this;
			}

			@Override
			public B addDescription(String... description) {
				Collections.addAll(this.description, description);
				return (B) this;
			}

			@Override
			public B addDescription(Collection<String> description) {
				this.description.addAll(description);
				return (B) this;
			}

			@Override
			public B clearDescription() {
				this.description.clear();
				return (B) this;
			}

			@Override
			public B addExample(String example) {
				this.examples.add(example);
				return (B) this;
			}

			@Override
			public B addExamples(String... examples) {
				Collections.addAll(this.examples, examples);
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
			public B addKeyword(String keyword) {
				this.keywords.add(keyword);
				return (B) this;
			}

			@Override
			public B addKeywords(String... keywords) {
				Collections.addAll(this.keywords, keywords);
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
			public B addRequiredPlugin(String plugin) {
				this.requiredPlugins.add(plugin);
				return (B) this;
			}

			@Override
			public B addRequiredPlugins(String... plugins) {
				Collections.addAll(this.requiredPlugins, plugins);
				return (B) this;
			}

			@Override
			public B addRequiredPlugins(Collection<String> plugins) {
				this.requiredPlugins.addAll(plugins);
				return (B) this;
			}

			@Override
			public B clearRequiredPlugins() {
				this.requiredPlugins.clear();
				return (B) this;
			}

			@Override
			public B addEvent(Class<? extends org.bukkit.event.Event> event) {
				this.events.add(event);
				return (B) this;
			}

			@Override
			public B addEvents(Class<? extends org.bukkit.event.Event>... events) {
				Collections.addAll(this.events, events);
				return (B) this;
			}

			@Override
			public B addEvents(Collection<Class<? extends org.bukkit.event.Event>> events) {
				this.events.addAll(events);
				return (B) this;
			}

			@Override
			public B clearEvents() {
				this.events.clear();
				return (B) this;
			}

			@Override
			public B origin(SyntaxOrigin origin) {
				defaultBuilder.origin(origin);
				return (B) this;
			}

			@Override
			public B supplier(Supplier<E> supplier) {
				defaultBuilder.supplier(supplier);
				return (B) this;
			}

			@Override
			public B addPattern(String pattern) {
				defaultBuilder.addPattern(pattern);
				return (B) this;
			}

			@Override
			public B addPatterns(String... patterns) {
				defaultBuilder.addPatterns(patterns);
				return (B) this;
			}

			@Override
			public B addPatterns(Collection<String> patterns) {
				defaultBuilder.addPatterns(patterns);
				return (B) this;
			}

			@Override
			public B clearPatterns() {
				defaultBuilder.clearPatterns();
				return (B) this;
			}

			@Override
			public B priority(Priority priority) {
				defaultBuilder.priority(priority);
				return (B) this;
			}

			@Override
			public Event<E> build() {
				return new EventImpl<>(
					defaultBuilder.build(), listeningBehavior, name,
					since, documentationId, description, examples, keywords, requiredPlugins, events
				);
			}

			@Override
			public void applyTo(SyntaxInfo.Builder<?, ?> builder) {
				defaultBuilder.applyTo(builder);
				//noinspection rawtypes - Should be safe, generics will not influence this
				if (builder instanceof Event.Builder eventBuilder) {
					eventBuilder.listeningBehavior(listeningBehavior);
					if (since != null) {
						eventBuilder.since(since);
					}
					if (documentationId != null) {
						eventBuilder.documentationId(documentationId);
					}
					eventBuilder.addDescription(description);
					eventBuilder.addExamples(examples);
					eventBuilder.addKeywords(keywords);
					eventBuilder.addRequiredPlugins(requiredPlugins);
					eventBuilder.addEvents(events);
				}
			}

		}

	}

}
