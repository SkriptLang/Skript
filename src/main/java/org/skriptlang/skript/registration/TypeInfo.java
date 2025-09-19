package org.skriptlang.skript.registration;

import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.util.Priority;

import java.util.Collection;

public interface TypeInfo<T> {

	static <T> Builder<T> builder(Class<T> type, String name) {

	}

	@NotNull String id();

	@NotNull String name();

	@NotNull Class<T> type();

	@NotNull Priority priority();

	@Unmodifiable @NotNull Collection<String> patterns();

	/**
	 * Represents a builder for {@link TypeInfo TypeInfos}.
	 *
	 * @param <T> The class of the type.
	 */
	interface Builder<T> {

		/**
		 * Sets this type builder's patterns.
		 *
		 * @param patterns The patterns.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		Builder<T> patterns(@NotNull String @NotNull ... patterns);

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
		 * Completes this builder.
		 *
		 * @return The final type.
		 */
		@Contract("-> new")
		TypeInfo<T> build();

	}

}
