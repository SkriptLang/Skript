/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package org.skriptlang.skript.bukkit.registration;

import ch.njol.skript.lang.SkriptEvent;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.bukkit.registration.BukkitInfos.Event;
import org.skriptlang.skript.lang.Priority;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

final class BukkitInfosImpl {

	static final class EventImpl<E extends SkriptEvent> implements Event<E> {

		private final SyntaxInfo<E> defaultInfo;
		private final String name;
		private final String id;
		@Nullable
		private final String since;
		@Nullable
		private final String documentationId;
		private final Collection<String> description;
		private final Collection<String> examples;
		private final Collection<String> keywords;
		private final Collection<String> requiredPlugins;
		private final Collection<Class<? extends org.bukkit.event.Event>> events;

		EventImpl(
			SyntaxInfo<E> defaultInfo, String name,
			@Nullable String since, @Nullable String documentationId, Collection<String> description, Collection<String> examples,
			Collection<String> keywords, Collection<String> requiredPlugins, Collection<Class<? extends org.bukkit.event.Event>> events
		) {
			this.defaultInfo = defaultInfo;
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
			if (!(other instanceof Event) || !super.equals(other)) {
				return false;
			}
			Event<?> event = (Event<?>) other;
			return Objects.equals(name(), event.name());
		}

		@Override
		public int hashCode() {
			return Objects.hash(origin(), type(), patterns(), name(), events());
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.add("origin", origin())
					.add("type", type())
					.add("patterns", patterns())
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

		@Override
		@Nullable
		public EntryValidator entryValidator() {
			return null;
		}

		@SuppressWarnings("unchecked")
		static final class BuilderImpl<B extends Event.Builder<B, E>, E extends SkriptEvent> implements Event.Builder<B, E> {

			private final SyntaxInfo.Builder<?, E> defaultBuilder;
			private final String name;
			@Nullable
			private String since;
			@Nullable
			private String documentationId;
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
			public B entryValidator(EntryValidator entryValidator) {
				throw new UnsupportedOperationException("entryValidator cannot be set for Event SyntaxInfos");
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
			public Event<E> build() {
				return new EventImpl<>(
					defaultBuilder.build(), name,
					since, documentationId, description, examples, keywords, requiredPlugins, events
				);
			}
		}

	}

}
