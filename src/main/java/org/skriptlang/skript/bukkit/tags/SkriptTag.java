package org.skriptlang.skript.bukkit.tags;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a custom tag created by the Skript user.
 * Implementation of {@link Tag} with a custom set of contents.
 * @param <T> The type of the contents.
 */
public class SkriptTag<T extends Keyed> implements Tag<T> {

	private final Set<T> contents;
	private final NamespacedKey key;

	public SkriptTag(NamespacedKey key, Collection<T> contents) {
		this.key = key;
		this.contents = new HashSet<>(contents);
	}

	@Override
	public boolean isTagged(@NotNull T item) {
		return contents.contains(item);
	}

	@Override
	public @NotNull @UnmodifiableView Set<T> getValues() {
		return Collections.unmodifiableSet(contents);
	}

	@Override
	public @NotNull NamespacedKey getKey() {
		return key;
	}

}
