package org.skriptlang.skript.registration;

import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.classes.registry.RegistryParser;
import ch.njol.skript.classes.registry.RegistrySerializer;
import ch.njol.skript.lang.DefaultExpression;
import com.google.common.base.Preconditions;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.addon.SkriptAddon;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

final class TypeInfoImpl<T> implements TypeInfo<T> {

	private final SkriptAddon source;
	private final String name;
	private final Class<T> type;
	private final List<String> patterns;
	private final List<String> description;
	private final List<String> since;
	private final List<String> examples;
	private final List<String> keywords;
	private final List<String> requires;
	private final Parser<T> parser;
	private final Serializer<T> serializer;
	private final DefaultExpression<T> defaultExpression;
	private final Supplier<Iterator<T>> values;

	TypeInfoImpl(
			SkriptAddon source,
			String name,
			Class<T> type,
			String[] patterns,
			String[] description,
			String[] since,
			String[] examples,
			String[] keywords,
			String[] requires,
			Parser<T> parser,
			Serializer<T> serializer,
			DefaultExpression<T> defaultExpression,
			Supplier<Iterator<T>> values
	) {
		this.source = source;
		this.name = name;
		this.type = type;
		this.patterns = List.of(patterns);
		this.description = description != null ? List.of(description) : Collections.emptyList();
		this.since = since != null ? List.of(since) : Collections.emptyList();
		this.examples = examples != null ? List.of(examples) : Collections.emptyList();
		this.keywords = keywords != null ? List.of(keywords) : Collections.emptyList();
		this.requires = requires != null ? List.of(requires) : Collections.emptyList();
		this.parser = parser;
		this.serializer = serializer;
		this.defaultExpression = defaultExpression;
		this.values = values;
	}

	@Override
	public @NotNull SkriptAddon source() {
		return source;
	}

	@Override
	public @NotNull String name() {
		return name;
	}

	@Override
	public @NotNull Class<T> type() {
		return type;
	}

	@Override
	public @NotNull @Unmodifiable Collection<String> patterns() {
		return patterns;
	}

	@Override
	public @Unmodifiable @NotNull Collection<String> description() {
		return description;
	}

	@Override
	public @Unmodifiable @NotNull Collection<String> since() {
		return since;
	}

	@Override
	public @Unmodifiable @NotNull Collection<String> examples() {
		return examples;
	}

	@Override
	public @Unmodifiable @NotNull Collection<String> keywords() {
		return keywords;
	}

	@Override
	public @Unmodifiable @NotNull Collection<String> requires() {
		return requires;
	}

	@Override
	public Parser<T> parser() {
		return parser;
	}

	@Override
	public Serializer<T> serializer() {
		return serializer;
	}

	@Override
	public DefaultExpression<T> defaultExpression() {
		return defaultExpression;
	}

	@Override
	public Supplier<Iterator<T>> values() {
		return values;
	}

	static final class TypeInfoBuilderImpl<T> implements TypeInfo.Builder<T> {

		private final SkriptAddon source;
		private final Class<T> type;
		private final String name;
		private final String[] pattern;

		private String[] description;
		private String[] since;
		private String[] examples;
		private String[] keywords;
		private String[] requires;

		private Parser<T> parser;
		private Serializer<T> serializer;
		private DefaultExpression<T> defaultExpression;
		private Supplier<Iterator<T>> values;

		public TypeInfoBuilderImpl(SkriptAddon source, Class<T> type, String name, String... patterns) {
			Preconditions.checkNotNull(source, "source cannot be null");
			Preconditions.checkNotNull(type, "type cannot be null");
			Preconditions.checkNotNull(name, "name cannot be null");
			Preconditions.checkNotNull(patterns, "patterns cannot be null");
			checkNotNull(patterns, "patterns contents cannot be null");

			this.source = source;
			this.type = type;
			this.name = name;
			this.pattern = patterns;
		}

		@Override
		public Builder<T> description(@NotNull String @NotNull ... description) {
			Preconditions.checkNotNull(description, "description cannot be null");
			checkNotNull(description, "description contents cannot be null");

			this.description = description;
			return this;
		}

		@Override
		public Builder<T> since(@NotNull String @NotNull ... since) {
			Preconditions.checkNotNull(since, "since cannot be null");
			checkNotNull(since, "since contents cannot be null");

			this.since = since;
			return this;
		}

		@Override
		public Builder<T> examples(@NotNull String @NotNull ... examples) {
			Preconditions.checkNotNull(examples, "examples cannot be null");
			checkNotNull(examples, "examples contents cannot be null");

			this.examples = examples;
			return this;
		}

		@Override
		public Builder<T> keywords(@NotNull String @NotNull ... keywords) {
			Preconditions.checkNotNull(keywords, "keywords cannot be null");
			checkNotNull(keywords, "keywords contents cannot be null");

			this.keywords = keywords;
			return this;
		}


		@Override
		public Builder<T> requires(@NotNull String @NotNull ... requires) {
			Preconditions.checkNotNull(keywords, "requires cannot be null");
			checkNotNull(keywords, "requires contents cannot be null");

			this.requires = requires;
			return this;
		}

		@Override
		public Builder<T> parser(@NotNull Parser<T> parser) {
			Preconditions.checkNotNull(parser, "parser cannot be null");

			this.parser = parser;
			return this;
		}

		@Override
		public Builder<T> serializer(@NotNull Serializer<T> serializer) {
			Preconditions.checkNotNull(serializer, "serializer cannot be null");

			this.serializer = serializer;
			return this;
		}

		@Override
		public Builder<T> defaultExpression(@NotNull DefaultExpression<T> expr) {
			Preconditions.checkNotNull(expr, "expr cannot be null");

			this.defaultExpression = expr;
			return this;
		}

		@Override
		public Builder<T> values(@NotNull Supplier<Iterator<T>> values) {
			Preconditions.checkNotNull(values, "values cannot be null");

			this.values = values;
			return this;
		}

		@Override
		public TypeInfo<T> build() {
			return new TypeInfoImpl<>(
					source,
					name,
					type,
					pattern,
					description,
					since,
					examples,
					keywords,
					requires,
					parser,
					serializer,
					defaultExpression,
					values
			);
		}

	}

	static final class RegistryInfoBuilderImpl<T extends @NotNull Keyed> implements TypeInfo.RegistryBuilder<T> {

		private final SkriptAddon source;
		private final Class<T> type;
		private final String name;
		private final String[] patterns;

		private String[] description;
		private String[] since;
		private String[] examples;
		private String[] keywords;
		private String[] requires;

		private final Parser<T> parser;
		private final Serializer<T> serializer;
		private DefaultExpression<T> defaultExpression;
		private Supplier<Iterator<T>> values;

		public RegistryInfoBuilderImpl(
				SkriptAddon source, Class<T> type,
				String name,
				Registry<T> registry, String langNode,
				String... patterns
		) {
			Preconditions.checkNotNull(source, "source cannot be null");
			Preconditions.checkNotNull(type, "type cannot be null");
			Preconditions.checkNotNull(name, "name cannot be null");
			Preconditions.checkNotNull(patterns, "patterns cannot be null");
			Preconditions.checkNotNull(registry, "registry cannot be null");
			Preconditions.checkNotNull(langNode, "langNode cannot be null");

			this.source = source;
			this.type = type;
			this.name = name;
			this.patterns = patterns;

			this.values = registry::iterator;
			this.serializer = new RegistrySerializer<>(registry);
			this.parser = new RegistryParser<>(registry, langNode);
		}

		@Override
		public RegistryBuilder<T> description(@NotNull String @NotNull ... description) {
			Preconditions.checkNotNull(description, "description cannot be null");
			checkNotNull(description, "description contents cannot be null");

			this.description = description;
			return this;
		}

		@Override
		public RegistryBuilder<T> since(@NotNull String @NotNull ... since) {
			Preconditions.checkNotNull(since, "since cannot be null");
			checkNotNull(since, "since contents cannot be null");

			this.since = since;
			return this;
		}

		@Override
		public RegistryBuilder<T> examples(@NotNull String @NotNull ... examples) {
			Preconditions.checkNotNull(examples, "examples cannot be null");
			checkNotNull(examples, "examples contents cannot be null");

			this.examples = examples;
			return this;
		}

		@Override
		public RegistryBuilder<T> keywords(@NotNull String @NotNull ... keywords) {
			Preconditions.checkNotNull(keywords, "keywords cannot be null");
			checkNotNull(keywords, "keywords contents cannot be null");

			this.keywords = keywords;
			return this;
		}


		@Override
		public RegistryBuilder<T> requires(@NotNull String @NotNull ... requires) {
			Preconditions.checkNotNull(keywords, "requires cannot be null");
			checkNotNull(keywords, "requires contents cannot be null");

			this.requires = requires;
			return this;
		}

		@Override
		public RegistryBuilder<T> defaultExpression(@NotNull DefaultExpression<T> expr) {
			Preconditions.checkNotNull(expr, "expr cannot be null");

			this.defaultExpression = expr;
			return this;
		}

		@Override
		public RegistryBuilder<T> values(@NotNull Supplier<Iterator<T>> values) {
			Preconditions.checkNotNull(values, "values cannot be null");

			this.values = values;
			return this;
		}

		@Override
		public TypeInfo<T> build() {
			return new TypeInfoImpl<>(
					source,
					name,
					type,
					patterns,
					description,
					since,
					examples,
					keywords,
					requires,
					parser,
					serializer,
					defaultExpression,
					values
			);
		}

	}

	/**
	 * Checks whether the elements in a {@link String} array are null.
	 *
	 * @param strings The strings.
	 */
	private static void checkNotNull(@NotNull String[] strings, @NotNull String message) {
		for (String string : strings) {
			Preconditions.checkNotNull(string, message);
		}
	}

}

