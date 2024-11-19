package org.skriptlang.skript.bukkit.tags.sources;

import ch.njol.util.coll.iterator.CheckedIterator;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.tags.TagType;

import java.util.Iterator;
import java.util.function.Predicate;

/**
 * A source for {@link org.bukkit.Tag}s, be it Bukkit's tag registries, Paper's handmade tags, or
 * custom tags made by the user.
 * @param <T> The type of tags this source will return.
 *           For example, the Bukkit "items" tag registry would return {@link org.bukkit.Material}s.
 */
public abstract class TagSource<T extends Keyed> {

	private final TagType<T>[] types;
	private final TagOrigin origin;

	/**
	 * @param origin The origin of this source.
	 * @param types The tag types this source represents.
	 */
	@SafeVarargs
	protected TagSource(TagOrigin origin, TagType<T>... types) {
		this.types = types;
		this.origin = origin;
	}

	/**
	 * @return All the tags associated with this source.
	 */
	public abstract Iterable<Tag<T>> getAllTags();

	/**
	 * For use in getting specific subsets of tags.
	 * @param predicate A Predicate used to filter tags.
	 * @return All the tags from this source, filtered based on the predicate.
	 */
	public Iterable<Tag<T>> getAllTagsMatching(Predicate<Tag<T>> predicate) {
		Iterator<Tag<T>> tagIterator = getAllTags().iterator();
 		return new Iterable<>() {
			@Override
			public @NotNull Iterator<Tag<T>> iterator() {
				return new CheckedIterator<>(tagIterator, predicate::test);
			}
		};
	}

	/**
	 * Gets a specific tag by the key.
	 * @param key The key to use to find the tag.
	 * @return The tag associated with the key. Null if no such tag exists.
	 */
	public abstract @Nullable Tag<T> getTag(NamespacedKey key);

	/**
	 * @return All the tag types that are represented by this source.
	 */
	public TagType<T>[] getTypes() {
		return types;
	}

	/**
	 * @return The origin of this source.
	 */
	public TagOrigin getOrigin() {
		return origin;
	}

}
