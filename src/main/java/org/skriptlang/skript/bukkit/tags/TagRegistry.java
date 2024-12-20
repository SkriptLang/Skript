package org.skriptlang.skript.bukkit.tags;

import ch.njol.util.coll.iterator.CheckedIterator;
import com.destroystokyo.paper.MaterialTags;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterators;
import io.papermc.paper.tag.EntityTags;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.tags.sources.BukkitTagSource;
import org.skriptlang.skript.bukkit.tags.sources.PaperTagSource;
import org.skriptlang.skript.bukkit.tags.sources.SkriptTagSource;
import org.skriptlang.skript.bukkit.tags.sources.TagOrigin;
import org.skriptlang.skript.bukkit.tags.sources.TagSource;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * A class in charge of storing and handling all the tags Skript can access.
 */
public class TagRegistry {

	private final TagSourceMap tagSourceMap = new TagSourceMap();

	/**
	 * Each new instance will create a new set of tag sources, in an effort to be reload safe.
	 */
	TagRegistry() {
		tagSourceMap.put(TagType.ITEMS, new BukkitTagSource<>("items", TagType.ITEMS));
		tagSourceMap.put(TagType.BLOCKS, new BukkitTagSource<>("blocks", TagType.BLOCKS));
		tagSourceMap.put(TagType.ENTITIES, new BukkitTagSource<>("entity_types", TagType.ENTITIES));

		if (TagModule.PAPER_TAGS_EXIST) {
			try {
				List<Tag<Material>> itemTags = new ArrayList<>();
				List<Tag<Material>> blockTags = new ArrayList<>();
				List<Tag<Material>> blockAndItemTag = new ArrayList<>();
				for (Field field : MaterialTags.class.getDeclaredFields()) {
					if (field.canAccess(null)) {
						//noinspection unchecked
						Tag<Material> tag = (Tag<Material>) field.get(null);
						boolean hasItem = false;
						boolean hasBlock = false;
						for (Material material : tag.getValues()) {
							if (!hasBlock && material.isBlock()) {
								blockTags.add(tag);
								hasBlock = true;
							}
							if (!hasItem && material.isItem()) {
								itemTags.add(tag);
								hasItem = true;
							}
							if (hasItem && hasBlock) {
								blockAndItemTag.add(tag);
								break;
							}
						}
					}
				}
				PaperTagSource<Material> paperMaterialTags = new PaperTagSource<>(blockAndItemTag, TagType.BLOCKS, TagType.ITEMS);
				PaperTagSource<Material> paperItemTags = new PaperTagSource<>(itemTags, TagType.ITEMS);
				PaperTagSource<Material> paperBlockTags = new PaperTagSource<>(blockTags, TagType.BLOCKS);
				tagSourceMap.put(TagType.BLOCKS, paperMaterialTags);
				tagSourceMap.put(TagType.ITEMS, paperMaterialTags);

				tagSourceMap.put(TagType.BLOCKS, paperBlockTags);
				tagSourceMap.put(TagType.ITEMS, paperItemTags);

				List<Tag<EntityType>> entityTags = new ArrayList<>();
				for (Field field : EntityTags.class.getDeclaredFields()) {
					if (field.canAccess(null))
						//noinspection unchecked
						entityTags.add((Tag<EntityType>) field.get(null));
				}
				PaperTagSource<EntityType> paperEntityTags = new PaperTagSource<>(entityTags, TagType.ENTITIES);
				tagSourceMap.put(TagType.ENTITIES, paperEntityTags);
			} catch (IllegalAccessException ignored) {}
		}

		SkriptTagSource.makeDefaultSources();
		tagSourceMap.put(TagType.ITEMS, SkriptTagSource.ITEMS());
		tagSourceMap.put(TagType.BLOCKS, SkriptTagSource.BLOCKS());
		tagSourceMap.put(TagType.ENTITIES, SkriptTagSource.ENTITIES());
	}

	/**
	 * Gets all the tags of a specific origin that are applicable to a given class.
	 * @param origin The origin to filter by.
	 * @param typeClass The class the tags should be applicable to.
	 * @param types Tag types to check with. Leaving this empty will check all tag types.
	 * @return TagRegistry from the given origin and types that apply to the given class.
	 * @param <T> see typeClass.
	 */
	public <T extends Keyed> Iterable<Tag<T>> getTags(TagOrigin origin, Class<T> typeClass, TagType<?>... types) {
		List<Iterator<Tag<T>>> tagIterators = new ArrayList<>();
		if (types == null)
			types = tagSourceMap.map.keys().toArray(new TagType[0]);
		for (TagType<?> type : types) {
			if (typeClass.isAssignableFrom(type.type())) {
				//noinspection unchecked
				Iterator<Tag<T>> iterator = getTags(origin, (TagType<T>) type).iterator();
				if (iterator.hasNext())
					tagIterators.add(iterator);
			}
		}
		return new Iterable<>() {
			@Override
			public @NotNull Iterator<Tag<T>> iterator() {
				return Iterators.concat(tagIterators.iterator());
			}
		};
	}

	/**
	 * Gets all the tags of a specific origin that are of a specific type.
	 * @param origin The origin to filter by.
	 * @param type The type of tags to get.
	 * @return TagRegistry from the given origin that are of the given type.
	 * @param <T> The class these tags apply to.
	 */
	public <T extends Keyed> Iterable<Tag<T>> getTags(TagOrigin origin, TagType<T> type) {
		if (!tagSourceMap.containsKey(type))
			return List.of();
		Iterator<TagSource<T>> tagSources = tagSourceMap.get(origin, type).iterator();
		if (!tagSources.hasNext())
			return List.of();
		return new Iterable<>() {
			@Override
			public @NotNull Iterator<Tag<T>> iterator() {
				return Iterators.concat(new Iterator<Iterator<Tag<T>>>() {
					@Override
					public boolean hasNext() {
						return tagSources.hasNext();
					}

					@Override
					public Iterator<Tag<T>> next() {
						return tagSources.next().getAllTags().iterator();
					}
				});
			}
		};
	}

	/**
	 * Gets all the tags of a specific origin that are of a specific type. Filters the resulting tags using the given
	 * predicate.
	 * @param origin The origin to filter by.
	 * @param type The type of tags to get.
	 * @param predicate A predicate to filter the tags with.
	 * @return TagRegistry from the given origin that are of the given type and that pass the filter.
	 * @param <T> The class these tags apply to.
	 */
	public <T extends Keyed> Iterable<Tag<T>> getMatchingTags(TagOrigin origin, TagType<T> type, Predicate<Tag<T>> predicate) {
		Iterator<Tag<T>> tagIterator = getTags(origin, type).iterator();
		return new Iterable<>() {
			@Override
			public @NotNull Iterator<Tag<T>> iterator() {
				return new CheckedIterator<>(tagIterator, predicate::test);
			}
		};
	}

	/**
	 * Gets a specific tag of a specific origin that is of a specific type.
	 * @param origin The origin to filter by.
	 * @param type The type of tags to get.
	 * @param key The key of the tag to get.
	 * @return The tag that matched the above values. Null if no tag is found.
	 * @param <T> The class these tags apply to.
	 */
	public <T extends Keyed> @Nullable Tag<T> getTag(TagOrigin origin, TagType<T> type, NamespacedKey key) {
		Tag<T> tag;
		for (TagSource<T> source : tagSourceMap.get(origin, type)) {
			tag = source.getTag(key);
			if (tag != null)
				return tag;
		}
		return null;
	}

	/**
	 * A MultiMap that maps TagTypes to multiple TagSources, matching generics.
	 */
	private static class TagSourceMap {

		private final ArrayListMultimap<TagType<?>, TagSource<?>> map = ArrayListMultimap.create();

		public <T extends Keyed> void put(TagType<T> key, TagSource<T> value) {
			map.put(key, value);
		}

		public <T extends Keyed> @NotNull List<TagSource<T>> get(TagOrigin origin, TagType<T> key) {
			List<TagSource<T>> sources = new ArrayList<>();
			for (TagSource<?> source : map.get(key)) {
				if (source.getOrigin().matches(origin))
					//noinspection unchecked
					sources.add((TagSource<T>) source);
			}
			return sources;
		}

		public <T extends Keyed> boolean containsKey(TagType<T> type) {
			return map.containsKey(type);
		}

	}

}
