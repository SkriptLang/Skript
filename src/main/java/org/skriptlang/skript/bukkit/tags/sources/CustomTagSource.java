package org.skriptlang.skript.bukkit.tags.sources;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.tags.TagType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A custom source of tags that stores its own tags.
 * @param <T> The class of the tags provided by this source.
 */
public class CustomTagSource<T extends Keyed> extends TagSource<T> {

	final Map<NamespacedKey, Tag<T>> tags;

	/**
	 * @param origin The origin of this source.
	 * @param tags The tags this source will own.
	 * @param types The tag types this source will represent.
	 */
	@SafeVarargs
	public CustomTagSource(TagOrigin origin, @NotNull Iterable<Tag<T>> tags, TagType<T>... types) {
		super(origin, types);
		this.tags = new HashMap<>();
		for (Tag<T> tag : tags) {
			this.tags.put(tag.getKey(), tag);
		}
	}

	@Override
	public Iterable<Tag<T>> getAllTags() {
		return Collections.unmodifiableCollection(tags.values());
	}

	@Override
	public @Nullable Tag<T> getTag(NamespacedKey key) {
		return tags.get(key);
	}

}
