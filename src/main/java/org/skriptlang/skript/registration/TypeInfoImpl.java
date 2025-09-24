package org.skriptlang.skript.registration;

import ch.njol.skript.classes.*;
import ch.njol.skript.classes.registry.RegistryParser;
import ch.njol.skript.classes.registry.RegistrySerializer;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.util.coll.iterator.ArrayIterator;
import com.google.common.base.Preconditions;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.addon.SkriptAddon;

import java.util.*;
import java.util.function.Supplier;

final class TypeInfoImpl<T> implements TypeInfo<T> {

	private final SkriptAddon source;
	private final String name;
	private final Class<T> type;
	private final Set<String> patterns;
	private final List<String> description;
	private final List<String> since;
	private final List<String> examples;
	private final List<String> keywords;
	private final List<String> requires;
	private final Parser<T> parser;
	private final Serializer<T> serializer;
	private final DefaultExpression<T> defaultExpression;
	private final Supplier<Iterator<T>> values;
	private final Cloner<T> cloner;

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
			Supplier<Iterator<T>> values,
			Cloner<T> cloner
	) {
		this.source = source;
		this.name = name;
		this.type = type;
		this.description = description != null ? List.of(description) : Collections.emptyList();
		this.since = since != null ? List.of(since) : Collections.emptyList();
		this.examples = examples != null ? List.of(examples) : Collections.emptyList();
		this.keywords = keywords != null ? List.of(keywords) : Collections.emptyList();
		this.requires = requires != null ? List.of(requires) : Collections.emptyList();
		this.parser = parser;
		this.serializer = serializer;
		this.defaultExpression = defaultExpression;
		this.values = values;
		this.cloner = cloner;

		Set<String> generated = new HashSet<>();
		for (String pattern : patterns) {
			generated.addAll(ClassInfo.PatternGenerator.generate(pattern));
		}
		this.patterns = Set.copyOf(generated);
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
	public @Unmodifiable @NotNull List<String> description() {
		return description;
	}

	@Override
	public @Unmodifiable @NotNull List<String> since() {
		return since;
	}

	@Override
	public @Unmodifiable @NotNull List<String> examples() {
		return examples;
	}

	@Override
	public @Unmodifiable @NotNull List<String> keywords() {
		return keywords;
	}

	@Override
	public @Unmodifiable @NotNull List<String> requires() {
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

	@Override
	public Cloner<T> cloner() {
		return cloner;
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
		private Cloner<T> cloner;

		TypeInfoBuilderImpl(SkriptAddon source, Class<T> type, String name, String... patterns) {
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
			Preconditions.checkNotNull(requires, "requires cannot be null");
			checkNotNull(requires, "requires contents cannot be null");

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
		public Builder<T> cloner(@NotNull Cloner<T> cloner) {
			Preconditions.checkNotNull(cloner, "cloner cannot be null");

			this.cloner = cloner;
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
					values,
					cloner
			);
		}

	}

	static final class RegistryInfoBuilderImpl<T extends @NotNull Keyed> implements RestrictedBuilder<T> {

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
		private Cloner<T> cloner;

		RegistryInfoBuilderImpl(
				SkriptAddon source,
				Class<T> type,
				String name,
				Registry<T> registry,
				String langNode,
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
		public RestrictedBuilder<T> description(@NotNull String @NotNull ... description) {
			Preconditions.checkNotNull(description, "description cannot be null");
			checkNotNull(description, "description contents cannot be null");

			this.description = description;
			return this;
		}

		@Override
		public RestrictedBuilder<T> since(@NotNull String @NotNull ... since) {
			Preconditions.checkNotNull(since, "since cannot be null");
			checkNotNull(since, "since contents cannot be null");

			this.since = since;
			return this;
		}

		@Override
		public RestrictedBuilder<T> examples(@NotNull String @NotNull ... examples) {
			Preconditions.checkNotNull(examples, "examples cannot be null");
			checkNotNull(examples, "examples contents cannot be null");

			this.examples = examples;
			return this;
		}

		@Override
		public RestrictedBuilder<T> keywords(@NotNull String @NotNull ... keywords) {
			Preconditions.checkNotNull(keywords, "keywords cannot be null");
			checkNotNull(keywords, "keywords contents cannot be null");

			this.keywords = keywords;
			return this;
		}


		@Override
		public RestrictedBuilder<T> requires(@NotNull String @NotNull ... requires) {
			Preconditions.checkNotNull(requires, "requires cannot be null");
			checkNotNull(requires, "requires contents cannot be null");

			this.requires = requires;
			return this;
		}

		@Override
		public RestrictedBuilder<T> defaultExpression(@NotNull DefaultExpression<T> expr) {
			Preconditions.checkNotNull(expr, "expr cannot be null");

			this.defaultExpression = expr;
			return this;
		}

		@Override
		public RestrictedBuilder<T> values(@NotNull Supplier<Iterator<T>> values) {
			Preconditions.checkNotNull(values, "values cannot be null");

			this.values = values;
			return this;
		}

		@Override
		public RestrictedBuilder<T> cloner(@NotNull Cloner<T> cloner) {
			Preconditions.checkNotNull(cloner, "cloner cannot be null");

			this.cloner = cloner;
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
					values,
					cloner
			);
		}

	}

	static final class EnumInfoBuilderImpl<T extends Enum<T>> implements RestrictedBuilder<T> {

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
		private Cloner<T> cloner;

		EnumInfoBuilderImpl(
				SkriptAddon source,
				Class<T> type,
				String name,
				String langNode,
				String... patterns
		) {
			Preconditions.checkNotNull(source, "source cannot be null");
			Preconditions.checkNotNull(type, "type cannot be null");
			Preconditions.checkNotNull(name, "name cannot be null");
			Preconditions.checkNotNull(patterns, "patterns cannot be null");
			Preconditions.checkNotNull(langNode, "langNode cannot be null");

			this.source = source;
			this.type = type;
			this.name = name;
			this.patterns = patterns;

			this.values = () -> new ArrayIterator<>(type.getEnumConstants());
			this.serializer = new EnumSerializer<>(type);
			this.parser = new EnumParser<>(type, langNode);
		}

		@Override
		public RestrictedBuilder<T> description(@NotNull String @NotNull ... description) {
			Preconditions.checkNotNull(description, "description cannot be null");
			checkNotNull(description, "description contents cannot be null");

			this.description = description;
			return this;
		}

		@Override
		public RestrictedBuilder<T> since(@NotNull String @NotNull ... since) {
			Preconditions.checkNotNull(since, "since cannot be null");
			checkNotNull(since, "since contents cannot be null");

			this.since = since;
			return this;
		}

		@Override
		public RestrictedBuilder<T> examples(@NotNull String @NotNull ... examples) {
			Preconditions.checkNotNull(examples, "examples cannot be null");
			checkNotNull(examples, "examples contents cannot be null");

			this.examples = examples;
			return this;
		}

		@Override
		public RestrictedBuilder<T> keywords(@NotNull String @NotNull ... keywords) {
			Preconditions.checkNotNull(keywords, "keywords cannot be null");
			checkNotNull(keywords, "keywords contents cannot be null");

			this.keywords = keywords;
			return this;
		}


		@Override
		public RestrictedBuilder<T> requires(@NotNull String @NotNull ... requires) {
			Preconditions.checkNotNull(requires, "requires cannot be null");
			checkNotNull(requires, "requires contents cannot be null");

			this.requires = requires;
			return this;
		}

		@Override
		public RestrictedBuilder<T> defaultExpression(@NotNull DefaultExpression<T> expr) {
			Preconditions.checkNotNull(expr, "expr cannot be null");

			this.defaultExpression = expr;
			return this;
		}

		@Override
		public RestrictedBuilder<T> values(@NotNull Supplier<Iterator<T>> values) {
			Preconditions.checkNotNull(values, "values cannot be null");

			this.values = values;
			return this;
		}

		@Override
		public RestrictedBuilder<T> cloner(@NotNull Cloner<T> cloner) {
			Preconditions.checkNotNull(cloner, "cloner cannot be null");

			this.cloner = cloner;
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
					values,
					cloner
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

