package org.skriptlang.skript.bukkit.entity;

import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.localization.Noun;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;

import java.util.Collection;
import java.util.SequencedCollection;

/**
 * Syntax info for entity data classes.
 * @param <Data> The entity data class.
 * @param <E> The entity class that {@code data} uses.
 */
interface EntityDataInfo<Data extends EntityData<E>, E extends Entity> extends SyntaxInfo<Data> {

	/**
	 * @return The name this info is identified as.
	 */
	String dataName();

	/**
	 * @return The code names this info looks for.
	 */
	SequencedCollection<String> codeNames();

	/**
	 * @return The default code name.
	 */
	String defaultCodeName();

	/**
	 * @return The index of the default code name.
	 */
	int defaultCodeNameIndex();

	/**
	 * @return The {@link Noun} format of the 'name' node for each code name in the lang file.
	 */
	Noun[] names();

	/**
	 * @return The entity type this info correlates to.
	 */
	@Nullable EntityType entityType();

	/**
	 * @return The entity class this info correlates to.
	 */
	Class<? extends E> entityClass();

	/**
	 * Gets the corresponding placement of {@code codeName}.
	 * @param codeName The code name.
	 * @return The placement.
	 */
	int codeNamePlacement(String codeName);

	/**
	 * Gets the {@code codeName} corresponding to the {@code matchedPattern} in {@link EntityData#init(ch.njol.skript.lang.Expression[], int, Kleenean, ParseResult)}.
	 * @param matchedPattern The placement of the pattern used.
	 * @return The corresponding {@code codeName}.
	 */
	String codeNameFromMatchedPattern(int matchedPattern);

	/**
	 * Gets the actual matched pattern from {@code matchedPattern} in {@link EntityData#init(ch.njol.skript.lang.Expression[], int, Kleenean, ParseResult)}.
	 * @param matchedPattern The placement of the pattern used
	 * @return The actual placement.
	 */
	int matchedCodeNamePattern(int matchedPattern);

	/**
	 * Builder used for constructing a new {@link EntityDataInfo}.
	 * @param <B> The type of builder being used.
	 * @param <Data> The {@link EntityData} class being used.
	 * @param <E> The {@link Entity} class {@code Data} is assigned to.
	 */
	interface Builder<B extends Builder<B, Data, E>, Data extends EntityData<E>, E extends Entity>
		extends SyntaxInfo.Builder<B, Data> {

		/**
		 * Adds a codename used to grab the 'name' and 'patterns' from in the lang file.
		 * @param codeName The codename to use.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		B addCodeName(String codeName);

		/**
		 * Adds codenames used to grab the 'name' and 'patterns' from in the lang file.
		 * @param codeNames The codenames to use.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		B addCodeNames(String... codeNames);

		/**
		 * Adds codenames used to grab the 'name' and 'patterns' from in the lang file.
		 * @param codeNames The codenames to use.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		B addCodeNames(Collection<String> codeNames);

		/**
		 * Sets the default codename index. Correlates to the codenames added.
		 * @param index The index of the codename to default to.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		B defaultCodeName(int index);

		/**
		 * Sets the entity type the info correlates to.
		 * @param entityType The entity type.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		B entityType(@Nullable EntityType entityType);

		/**
		 * Sets the entity class the info correlates to.
		 * @param entityClass The entity class.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		B entityClass(Class<? extends E> entityClass);

		@Override
		EntityDataInfo<Data, E> build();

		@Internal
		@Override
		default B addPattern(String pattern) {
			throw new UnsupportedOperationException();
		};

		@Internal
		@Override
		default B addPatterns(String... patterns) {
			throw new UnsupportedOperationException();
		}

		@Internal
		@Override
		default B addPatterns(Collection<String> patterns) {
			throw new UnsupportedOperationException();
		}

		@Internal
		@Override
		default B clearPatterns() {
			throw new UnsupportedOperationException();
		}

	}

}
