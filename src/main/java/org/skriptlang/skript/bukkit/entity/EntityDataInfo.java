package org.skriptlang.skript.bukkit.entity;

import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.localization.Noun;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData.EntityDataPatterns;
import org.skriptlang.skript.bukkit.entity.EntityData.PatternGroup;
import org.skriptlang.skript.registration.SyntaxInfo;

import java.util.Collection;
import java.util.SequencedCollection;

/**
 * Syntax info for entity data classes.
 * @param <Data> The entity data class.
 * @param <E> The entity class that {@code data} uses.
 */
public interface EntityDataInfo<Data extends EntityData<E>, E extends Entity> extends SyntaxInfo<Data> {

	/**
	 * @return The name this info is identified as.
	 */
	String dataName();

	/**
	 * @return The data patterns for this info.
	 */
	EntityDataPatterns<?> dataPatterns();

	/**
	 * @return The default {@link PatternGroup}.
	 */
	PatternGroup<?> defaultGroup();

	/**
	 * @return The index of the default {@link PatternGroup}.
	 */
	int defaultGroupIndex();

	/**
	 * @return The {@link Noun} format of the 'name' node for each code name in the lang file.
	 */
	SequencedCollection<Noun> names();

	/**
	 * @return The entity type this info correlates to.
	 */
	@Nullable EntityType entityType();

	/**
	 * @return The entity class this info correlates to.
	 */
	Class<? extends E> entityClass();

	/**
	 * Gets the {@link PatternGroup} corresponding to the {@code matchedPattern} in {@link EntityData#init(ch.njol.skript.lang.Expression[], int, Kleenean, ParseResult)}.
	 * @param matchedPattern The index of the pattern matched.
	 * @return The corresponding {@link PatternGroup}.
	 */
	PatternGroup<?> groupFromMatchedPattern(int matchedPattern);

	/**
	 * Gets the index of the pattern matched in the corresponding {@link PatternGroup}.
	 * @param matchedPattern The placement of the pattern used.
	 * @return Index of the pattern in {@link PatternGroup}.
	 */
	int matchedGroupPattern(int matchedPattern);

	/**
	 * Builder used for constructing a new {@link EntityDataInfo}.
	 * @param <B> The type of builder being used.
	 * @param <Data> The {@link EntityData} class being used.
	 * @param <E> The {@link Entity} class {@code Data} is assigned to.
	 */
	interface Builder<B extends Builder<B, Data, E>, Data extends EntityData<E>, E extends Entity>
		extends SyntaxInfo.Builder<B, Data> {

		/**
		 * Sets the data patterns containing the patterns to be used.
		 * @param dataPatterns The patterns to use.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		B dataPatterns(EntityDataPatterns<?> dataPatterns);

		/**
		 * Sets the default group index. Correlates to the {@link PatternGroup} contained in {@link EntityDataPatterns}.
		 * @param index The index of the group to default to.
		 * @return This builder.
		 */
		@Contract("_ -> this")
		B defaultGroupIndex(int index);

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
		@Contract("_ -> fail")
		default B addPattern(String pattern) {
			throw new UnsupportedOperationException();
		};

		@Internal
		@Override
		@Contract("_ -> fail")
		default B addPatterns(String... patterns) {
			throw new UnsupportedOperationException();
		}

		@Internal
		@Override
		@Contract("_ -> fail")
		default B addPatterns(Collection<String> patterns) {
			throw new UnsupportedOperationException();
		}

		@Internal
		@Override
		@Contract("-> fail")
		default B clearPatterns() {
			throw new UnsupportedOperationException();
		}

	}

}
