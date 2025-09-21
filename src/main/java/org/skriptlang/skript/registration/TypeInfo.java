package org.skriptlang.skript.registration;

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
import org.skriptlang.skript.registration.TypeInfoImpl.RegistryInfoBuilderImpl;
import org.skriptlang.skript.registration.TypeInfoImpl.TypeInfoBuilderImpl;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Supplier;

@ApiStatus.Experimental
public interface TypeInfo<T> extends Documentable {

	static <T> Builder<T> builder(
			@NotNull SkriptAddon source,
			@NotNull Class<T> type,
			@NotNull String name,
			@NotNull String... patterns
	) {
		return new TypeInfoBuilderImpl<>(source, type, name, patterns);
	}

	static <T extends @NotNull Keyed> RegistryBuilder<T> builder(
			@NotNull SkriptAddon source,
			@NotNull Class<T> type,
			@NotNull String name,
			@NotNull Registry<T> registry,
			@NotNull String langNode,
			@NotNull String @NotNull ... patterns
	) {
		return new RegistryInfoBuilderImpl<>(source, type, name, registry, langNode, patterns);
	}

	@NotNull SkriptAddon source();

	@NotNull Class<T> type();

	@NotNull @Unmodifiable Collection<String> patterns();

	Parser<T> parser();

	Serializer<T> serializer();

	DefaultExpression<T> defaultExpression();

	Supplier<Iterator<T>> values();

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
		 * Completes this builder.
		 *
		 * @return The final type.
		 */
		@Contract("-> new")
		TypeInfo<T> build();

	}

	/**
	 * Represents a builder for {@link TypeInfo TypeInfos} based on {@link org.bukkit.Registry Bukkit's Registry}.
	 *
	 * @param <T> The class of the type.
	 */
	interface RegistryBuilder<T> {

		/**
		 * Sets this type builder's description.
		 *
		 * @param description The description.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		RegistryBuilder<T> description(@NotNull String @NotNull ... description);

		/**
		 * Sets this type builder's version history.
		 *
		 * @param since The version information.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		RegistryBuilder<T> since(@NotNull String @NotNull ... since);

		/**
		 * Sets this type builder's examples.
		 *
		 * @param examples The examples.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		RegistryBuilder<T> examples(@NotNull String @NotNull ... examples);

		/**
		 * Sets this type builder's keywords.
		 *
		 * @param keywords The keywords.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		RegistryBuilder<T> keywords(@NotNull String @NotNull ... keywords);

		/**
		 * Sets this type builder's requirements.
		 *
		 * @param requires The requirements.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		RegistryBuilder<T> requires(@NotNull String @NotNull ... requires);

		/**
		 * Sets this type builder's default expression.
		 *
		 * @param expr The default expression.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		RegistryBuilder<T> defaultExpression(@NotNull DefaultExpression<T> expr);

		/**
		 * Sets this type builder's possible values.
		 *
		 * @param values All values.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		RegistryBuilder<T> values(@NotNull Supplier<Iterator<T>> values);

		/**
		 * Completes this builder.
		 *
		 * @return The final type.
		 */
		@Contract("-> new")
		TypeInfo<T> build();

	}

}
