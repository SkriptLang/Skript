package org.skriptlang.skript.bukkit.tags.sources;

import ch.njol.util.coll.iterator.EmptyIterable;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;
import org.skriptlang.skript.bukkit.tags.TagType;

public class SkriptTagSource<T extends Keyed> extends CustomTagSource<T> {

	public static final SkriptTagSource<Material> ITEMS = new SkriptTagSource<>(TagType.ITEMS);
	public static final SkriptTagSource<Material> BLOCKS = new SkriptTagSource<>(TagType.BLOCKS);
	public static final SkriptTagSource<EntityType> ENTITIES = new SkriptTagSource<>(TagType.ENTITIES);

	/**
	 * @param types  The tag types this source will represent.
	 */
	@SafeVarargs
	public SkriptTagSource(TagType<T>... types) {
		super(TagOrigin.SKRIPT, new EmptyIterable<>(), types);
	}

	public void addTag(Tag<T> tag) {
		tags.put(tag.getKey(), tag);
	}
}
