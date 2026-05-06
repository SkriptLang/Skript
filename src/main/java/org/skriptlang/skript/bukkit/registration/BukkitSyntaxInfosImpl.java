package org.skriptlang.skript.bukkit.registration;

import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptEvent.ListeningBehavior;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event;
import org.skriptlang.skript.docs.Documentation;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.util.Priority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class BukkitSyntaxInfosImpl {

	static final class EventImpl<E extends SkriptEvent> implements Event<E> {

		private final SyntaxInfo<E> defaultInfo;
		private final ListeningBehavior listeningBehavior;
		private final Collection<Class<? extends org.bukkit.event.Event>> events;

		EventImpl(
			SyntaxInfo<E> defaultInfo, ListeningBehavior listeningBehavior,
			Collection<Class<? extends org.bukkit.event.Event>> events
		) {
			String name = defaultInfo.documentation().name();
			name = name.startsWith("*") ? name.substring(1) : "On " + name;
			this.defaultInfo = defaultInfo.toBuilder()
				.documentation(defaultInfo.documentation().toBuilder()
					.name(name)
					.build())
				.build();
			this.listeningBehavior = listeningBehavior;
			this.events = ImmutableList.copyOf(events);
		}

		@Override
		public Builder<? extends Builder<?, E>, E> toBuilder() {
			var builder = new BuilderImpl<>(type());
			builder.oldName = this.documentation().name();
			defaultInfo.toBuilder().applyTo(builder);
			builder.listeningBehavior(listeningBehavior);
			builder.addEvents(events);
			return builder;
		}

		@Override
		public ListeningBehavior listeningBehavior() {
			return listeningBehavior;
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
			if (!(other instanceof Event<?> info)) {
				return false;
			}
			// if 'other' is a custom implementation, have it compare against this to ensure symmetry
			if (other.getClass() != EventImpl.class && !other.equals(this)) {
				return false;
			}
			// compare known data
			return type() == info.type() &&
					Objects.equals(patterns(), info.patterns()) &&
					Objects.equals(priority(), info.priority()) &&
					Objects.equals(events(), info.events());
		}

		@Override
		public int hashCode() {
			return Objects.hash(defaultInfo, events());
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.add("type", type())
					.add("patterns", patterns())
					.add("priority", priority())
					.add("documentation", documentation())
					.add("events", events())
					.toString();
		}

		//
		// default methods
		//

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
		public SequencedCollection<String> patterns() {
			return defaultInfo.patterns();
		}

		@Override
		public Priority priority() {
			return defaultInfo.priority();
		}

		@Override
		public Documentation documentation() {
			return defaultInfo.documentation();
		}

		@SuppressWarnings("unchecked")
		static final class BuilderImpl<B extends Event.Builder<B, E>, E extends SkriptEvent> implements Event.Builder<B, E> {

			private final SyntaxInfo.Builder<?, E> defaultBuilder;
			private ListeningBehavior listeningBehavior = ListeningBehavior.UNCANCELLED;
			private final List<Class<? extends org.bukkit.event.Event>> events = new ArrayList<>();

			private @Nullable String oldName = null;

			BuilderImpl(Class<E> type) {
				this.defaultBuilder = SyntaxInfo.builder(type);
			}

			BuilderImpl(Class<E> type, String name) {
				this.defaultBuilder = SyntaxInfo.builder(type);
				editDocumentation(builder -> builder.name(name));
			}

			private void editDocumentation(Consumer<Documentation.Builder<?>> consumer) {
				var tempDefault = defaultBuilder.addPattern("").build();
				var builder = tempDefault.documentation().toBuilder();
				consumer.accept(builder);
				defaultBuilder.clearPatterns()
					.addPatterns(tempDefault.patterns().stream().filter(pattern -> !pattern.isEmpty()).toList())
					.documentation(builder.build());
			}

			@Override
			public B listeningBehavior(ListeningBehavior listeningBehavior) {
				this.listeningBehavior = listeningBehavior;
				return (B) this;
			}

			@Override
			public B documentationId(String documentationId) {
				editDocumentation(builder -> builder.id(documentationId));
				return (B) this;
			}

			@Override
			public B addSince(String since) {
				editDocumentation(builder -> builder.addSince(since));
				return (B) this;
			}

			@Override
			public B addSince(String... since) {
				editDocumentation(builder -> builder.addSince(since));
				return (B) this;
			}

			@Override
			public B addSince(Collection<String> since) {
				editDocumentation(builder -> builder.addSince(since));
				return (B) this;
			}

			@Override
			public B clearSince() {
				editDocumentation(Documentation.Builder::clearSince);
				return (B) this;
			}

			@Override
			public B addDescription(String description) {
				addDescription(List.of(description));
				return (B) this;
			}

			@Override
			public B addDescription(String... description) {
				addDescription(List.of(description));
				return (B) this;
			}

			@Override
			public B addDescription(Collection<String> description) {
				editDocumentation(builder -> {
					String current = builder.build().description();
					if (!current.isEmpty()) {
						current += "\n";
					}
					current += String.join("\n", description);
					builder.description(current);
				});
				return (B) this;
			}

			@Override
			public B clearDescription() {
				editDocumentation(builder -> builder.description(""));
				return (B) this;
			}

			@Override
			public B addExample(String example) {
				editDocumentation(builder -> builder.addExample(example));
				return (B) this;
			}

			@Override
			public B addExamples(String... examples) {
				editDocumentation(builder -> builder.addExamples(examples));
				return (B) this;
			}

			@Override
			public B addExamples(Collection<String> examples) {
				editDocumentation(builder -> builder.addExamples(examples));
				return (B) this;
			}

			@Override
			public B clearExamples() {
				editDocumentation(Documentation.Builder::clearExamples);
				return (B) this;
			}

			@Override
			public B addKeyword(String keyword) {
				editDocumentation(builder -> builder.addKeyword(keyword));
				return (B) this;
			}

			@Override
			public B addKeywords(String... keywords) {
				editDocumentation(builder -> builder.addKeywords(keywords));
				return (B) this;
			}

			@Override
			public B addKeywords(Collection<String> keywords) {
				editDocumentation(builder -> builder.addKeywords(keywords));
				return (B) this;
			}

			@Override
			public B clearKeywords() {
				editDocumentation(Documentation.Builder::clearKeywords);
				return (B) this;
			}

			@Override
			public B addRequiredPlugin(String plugin) {
				editDocumentation(builder -> builder.addRequirement(plugin));
				return (B) this;
			}

			@Override
			public B addRequiredPlugins(String... plugins) {
				editDocumentation(builder -> builder.addRequirements(plugins));
				return (B) this;
			}

			@Override
			public B addRequiredPlugins(Collection<String> plugins) {
				editDocumentation(builder -> builder.addRequirements(plugins));
				return (B) this;
			}

			@Override
			public B clearRequiredPlugins() {
				editDocumentation(Documentation.Builder::clearRequirements);
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
			public B origin(Origin origin) {
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
			public B documentation(Documentation documentation) {
				defaultBuilder.documentation(documentation);
				if (!documentation.name().equals(oldName)) {
					oldName = null;
				}
				return (B) this;
			}

			@Override
			public Event<E> build() {
				if (this.oldName != null) { // bruh
					editDocumentation(builder -> builder.name("*" + this.oldName));
				}
				return new EventImpl<>(defaultBuilder.build(), listeningBehavior, events);
			}

			@Override
			public void applyTo(SyntaxInfo.Builder<?, ?> builder) {
				defaultBuilder.applyTo(builder);
				//noinspection rawtypes - Should be safe, generics will not influence this
				if (builder instanceof Event.Builder eventBuilder) {
					eventBuilder.listeningBehavior(listeningBehavior);
					eventBuilder.addEvents(events);
				}
			}

		}

	}

}
