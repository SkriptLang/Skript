package org.skriptlang.skript.bukkit.tags.sources;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.bukkit.tags.TagType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A set of tags provided by Paper.
 * @param <T> The class of tag this source provides.
 */
public final class PaperTagSource<T extends Keyed> extends CustomTagSource<T> {

	/**
	 * Creates {@link PaperTag}s from the raw tags. Removes _settag.
	 * @param tags The raw tags provided by Paper.
	 * @return The modified tags with _settag removed from their keys.
	 * @param <T> The class of the tags.
	 */
	private static <T extends Keyed> @NotNull Iterable<Tag<T>> getPaperTags(@NotNull Iterable<Tag<T>> tags) {
		List<Tag<T>> modifiedTags = new ArrayList<>();
		for (Tag<T> tag : tags) {
			modifiedTags.add(new PaperTag<>(tag));
		}
		return modifiedTags;
	}

	/**
	 * @param tags The raw tags from Paper.
	 * @param types The tag types this source represents.
	 */
	@SafeVarargs
	public PaperTagSource(Iterable<Tag<T>> tags, TagType<T>... types) {
		super(TagOrigin.PAPER, getPaperTags(tags), types);
	}

	/**
	 * Wrapper for Paper tags to remove "_settag" from their key.
	 * @param <T1> The class of the tag.
	 */
	private static class PaperTag<T1 extends Keyed> implements Tag<T1> {

		private final Tag<T1> paperTag;
		private final NamespacedKey key;

		public PaperTag(@NotNull Tag<T1> paperTag) {
			this.paperTag = paperTag;
			this.key = NamespacedKey.fromString(paperTag.getKey().toString().replace("_settag", ""));
		}

		@Override
		public boolean isTagged(@NotNull T1 item) {
			return paperTag.isTagged(item);
		}

		@Override
		public @NotNull Set<T1> getValues() {
			return paperTag.getValues();
		}

		@Override
		public @NotNull NamespacedKey getKey() {
			return key;
		}

	}

}
