package org.skriptlang.skript.bukkit.entity;

import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.localization.Noun;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;

import java.util.Collection;
import java.util.SequencedCollection;

interface EntityDataInfo<Data extends EntityData<E>, E extends Entity> extends SyntaxInfo<Data> {

	String dataName();

	SequencedCollection<String> codeNames();

	int defaultCodeName();

	Noun[] names();

	@Nullable EntityType entityType();

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

	interface Builder<B extends Builder<B, Data, E>, Data extends EntityData<E>, E extends Entity>
		extends SyntaxInfo.Builder<B, Data> {

		B addCodeName(String codeName);

		B addCodeNames(String... codeNames);

		B addCodeNames(Collection<String> codeNames);

		B defaultCodeName(int index);

		B entityType(@Nullable EntityType entityType);

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
