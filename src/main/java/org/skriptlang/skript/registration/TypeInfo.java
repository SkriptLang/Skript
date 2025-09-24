package org.skriptlang.skript.registration;

import ch.njol.skript.classes.Cloner;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.doc.Documentable;
import ch.njol.skript.lang.DefaultExpression;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.TypeInfoImpl.EnumInfoBuilderImpl;
import org.skriptlang.skript.registration.TypeInfoImpl.RegistryInfoBuilderImpl;
import org.skriptlang.skript.registration.TypeInfoImpl.TypeInfoBuilderImpl;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Supplier;

/**
 * Represents the info Skript has related to a type.
 *
 * @param <T> The class that this {@link TypeInfo} represents
 */
@ApiStatus.Experimental
public interface TypeInfo<T> extends Documentable {

	/**
	 * Creates a new builder for a regular type.
	 *
	 * @param source The source addon.
	 * @param type The class this type belongs to.
	 * @param name The display name of this type.
	 * @param patterns The patterns that the user can enter to reference this type.
	 * @return A new builder.
	 * @param <T> The type.
	 */
	static <T> Builder<T> builder(
			@NotNull SkriptAddon source,
			@NotNull Class<T> type,
			@NotNull String name,
			@NotNull String... patterns
	) {
		return new TypeInfoBuilderImpl<>(source, type, name, patterns);
	}

	/**
	 * Creates a new builder for a type backed by a {@link Registry}.
	 *
	 * @param source The source addon.
	 * @param type The class this type belongs to.
	 * @param name The display name of this type.
	 * @param registry The registry to extract values from.
	 * @param langNode The node in the {@code default.lang} file used
	 *                 to associate the values from the registry to a value that
	 *                 can be used in scripts.
	 * @param patterns The patterns that the user can enter to reference this type.
	 * @return A new builder.
	 * @param <T> The type.
	 */
	static <T extends @NotNull Keyed> RestrictedBuilder<T> builder(
			@NotNull SkriptAddon source,
			@NotNull Class<T> type,
			@NotNull String name,
			@NotNull Registry<T> registry,
			@NotNull String langNode,
			@NotNull String @NotNull ... patterns
	) {
		return new RegistryInfoBuilderImpl<>(source, type, name, registry, langNode, patterns);
	}

	/**
	 * Creates a new builder for a type backed by an {@link Enum}.
	 *
	 * @param source The source addon.
	 * @param type The class this type belongs to.
	 * @param name The display name of this type.
	 * @param langNode The node in the {@code default.lang} file used
	 *                 to associate the values from the enum to a value that
	 *                 can be used in scripts.
	 * @param patterns The patterns that the user can enter to reference this type.
	 * @return A new builder.
	 * @param <T> The type.
	 */
	static <T extends Enum<T>> RestrictedBuilder<T> builder(
			@NotNull SkriptAddon source,
			@NotNull Class<T> type,
			@NotNull String name,
			@NotNull String langNode,
			@NotNull String @NotNull ... patterns
	) {
		return new EnumInfoBuilderImpl<>(source, type, name, langNode, patterns);
	}

	/**
	 * @return The source of this type.
	 */
	@NotNull SkriptAddon source();

	/**
	 * @return The {@link Class} representing the type.
	 */
	@NotNull Class<T> type();

	/**
	 * @return An unmodifiable collection of all possible ways to reference this type.
	 */
	@Unmodifiable @NotNull Collection<String> patterns();

	/**
	 * @return The {@link Parser} associated with this type.
	 */
	Parser<T> parser();

	/**
	 * @return The {@link Serializer} associated with this type.
	 */
	Serializer<T> serializer();

	/**
	 * @return The default expression associated with this type.
	 */
	DefaultExpression<T> defaultExpression();

	/**
	 * @return A supplier which provides an iterator
	 * for all possible values that this type can have.
	 */
	Supplier<Iterator<T>> values();

	/**
	 * @return The {@link Cloner} associated with this type.
	 */
	Cloner<T> cloner();

	/**
	 * Represents a builder for {@link TypeInfo TypeInfos}.
	 *
	 * @param <T> The class of the type.
	 */
	interface Builder<T> {

		/**
		 * Sets this type builder's description.
		 *
		 * @param description The description.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		Builder<T> description(@NotNull String @NotNull ... description);

		/**
		 * Sets this type builder's version history.
		 *
		 * @param since The version information.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		Builder<T> since(@NotNull String @NotNull ... since);

		/**
		 * Sets this type builder's examples.
		 *
		 * @param examples The examples.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		Builder<T> examples(@NotNull String @NotNull ... examples);

		/**
		 * Sets this type builder's keywords.
		 *
		 * @param keywords The keywords.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		Builder<T> keywords(@NotNull String @NotNull ... keywords);

		/**
		 * Sets this type builder's requirements.
		 *
		 * @param requires The requirements.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		Builder<T> requires(@NotNull String @NotNull ... requires);

		/**
		 * Sets this type builder's parser.
		 *
		 * @param parser The parser.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		Builder<T> parser(@NotNull Parser<T> parser);

		/**
		 * Sets this type builder's serializer.
		 *
		 * @param serializer The serializer.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		Builder<T> serializer(@NotNull Serializer<T> serializer);

		/**
		 * Sets this type builder's default expression.
		 *
		 * @param expr The default expression.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		Builder<T> defaultExpression(@NotNull DefaultExpression<T> expr);

		/**
		 * Sets this type builder's possible values.
		 *
		 * @param values All values.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		Builder<T> values(@NotNull Supplier<Iterator<T>> values);

		/**
		 * Sets this type builder's cloner.
		 *
		 * @param cloner The cloner.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		Builder<T> cloner(@NotNull Cloner<T> cloner);

		/**
		 * Completes this builder.
		 *
		 * @return The final type.
		 */
		@Contract("-> new")
		TypeInfo<T> build();

	}

	/**
	 * Represents a builder for {@link TypeInfo TypeInfos} based on
	 * {@link org.bukkit.Registry Bukkit's Registry} or an {@link Enum}.
	 *
	 * @param <T> The class of the type.
	 */
	interface RestrictedBuilder<T> {

		/**
		 * Sets this type builder's description.
		 *
		 * @param description The description.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		RestrictedBuilder<T> description(@NotNull String @NotNull ... description);

		/**
		 * Sets this type builder's version history.
		 *
		 * @param since The version information.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		RestrictedBuilder<T> since(@NotNull String @NotNull ... since);

		/**
		 * Sets this type builder's examples.
		 *
		 * @param examples The examples.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		RestrictedBuilder<T> examples(@NotNull String @NotNull ... examples);

		/**
		 * Sets this type builder's keywords.
		 *
		 * @param keywords The keywords.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		RestrictedBuilder<T> keywords(@NotNull String @NotNull ... keywords);

		/**
		 * Sets this type builder's requirements.
		 *
		 * @param requires The requirements.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		RestrictedBuilder<T> requires(@NotNull String @NotNull ... requires);

		/**
		 * Sets this type builder's default expression.
		 *
		 * @param expr The default expression.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		RestrictedBuilder<T> defaultExpression(@NotNull DefaultExpression<T> expr);

		/**
		 * Sets this type builder's possible values.
		 *
		 * @param values All values.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		RestrictedBuilder<T> values(@NotNull Supplier<Iterator<T>> values);

		/**
		 * Sets this type builder's cloner.
		 *
		 * @param cloner The cloner.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		RestrictedBuilder<T> cloner(@NotNull Cloner<T> cloner);

		/**
		 * Completes this builder.
		 *
		 * @return The final type.
		 */
		@Contract("-> new")
		TypeInfo<T> build();

	}

}
