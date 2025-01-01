package org.skriptlang.skript.bukkit.tags.sources;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.tags.TagType;

/**
 * A set of tags provided by Bukkit.
 * @param <T> The class of the tags of this source.
 */
public class BukkitTagSource<T extends Keyed> extends TagSource<T> {

	private final String registry;

	/**
	 * @param registry The name of the registry to use. For example, {@link Tag#REGISTRY_ITEMS}.
	 * @param type The type of tag this represents. To continue the example, {@link TagType#ITEMS}.
	 */
	public BukkitTagSource(String registry, TagType<T> type) {
		super(TagOrigin.BUKKIT, type);
		this.registry = registry;
	}

	@Override
	public @NotNull Iterable<Tag<T>> getAllTags() {
		return Bukkit.getTags(registry, getTypes()[0].type());
	}

	@Override
	public @Nullable Tag<T> getTag(NamespacedKey key) {
		return Bukkit.getTag(registry, key, getTypes()[0].type());
	}

}
