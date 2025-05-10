package org.skriptlang.skript.bukkit.tags;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The type of a tag. Represents a category or context that the tags apply in.
 * For example, {@link #ITEMS} tags apply to {@link Material}s, like {@link #BLOCKS}, but in item form rather than as
 * placed blocks.
 * <br>
 * This class also contains a static registry of all tag types.
 *
 * @param <T> see type.
 */
public class TagType<T extends Keyed> {

	private static final List<TagType<?>> REGISTERED_TAG_TYPES = Collections.synchronizedList(new ArrayList<>());

	public static final TagType<Material> ITEMS = new TagType<>("item", Material.class);
	public static final TagType<Material> BLOCKS = new TagType<>("block", Material.class);
	public static final TagType<EntityType> ENTITIES = new TagType<>("entity [type]", "entity type", EntityType.class);

	static {
		TagType.addType(ITEMS, BLOCKS, ENTITIES);
	}

	private final String pattern;
	private final String toString;
	private final Class<T> type;

	/**
	 * @param pattern The pattern to use when constructing the selection Skript pattern.
	 * @param type The class this tag type applies to.
	 */
	public TagType(String pattern, Class<T> type) {
		this(pattern, pattern, type);
	}

	/**
	 * @param pattern The pattern to use when constructing the selection Skript pattern.
	 * @param toString The string to use when printing a toString.
	 * @param type The class this tag type applies to.
	 */
	public TagType(String pattern, String toString, Class<T> type) {
		this.pattern = pattern;
		this.type = type;
		this.toString = toString;
	}

	public String pattern() {
		return pattern;
	}

	public Class<T> type() {
		return type;
	}

	@Override
	public String toString() {
		return toString;
	}

	/**
	 * Adds types to the registered tag types.
	 * @param type The types to add.
	 */
	public static void addType(TagType<?>... type) {
		REGISTERED_TAG_TYPES.addAll(List.of(type));
	}

	/**
	 * @return An unmodifiable list of all the registered types.
	 */
	@Contract(pure = true)
	public static @NotNull @UnmodifiableView List<TagType<?>> getTypes() {
		return Collections.unmodifiableList(REGISTERED_TAG_TYPES);
	}

	/**
	 * Gets tag types by index. If a negative value is used, gets all the tag types.
	 * @param i The index of the type to get.
	 * @return The type at that index, or all tags if index < 0.
	 */
	public static TagType<?> @NotNull [] getType(int i) {
		if (i < 0)
			return REGISTERED_TAG_TYPES.toArray(new TagType<?>[0]);
		return new TagType[]{REGISTERED_TAG_TYPES.get(i)};
	}

	/**
	 * Gets tag types by parser mark. Equivalent to {@code getType(i - 1)}.
	 * @param i The index of the type to get.
	 * @return The type at that index, or all tags if index < 0.
	 * @see #getType(int)
	 * @see #getFullPattern()
	 */
	public static TagType<?> @NotNull [] fromParseMark(int i) {
		return getType(i - 1);
	}

	/**
	 * @return Returns an optional choice pattern for use in Skript patterns. Contains parse marks.
	 *			Pass the parse mark to {@link #fromParseMark(int)} to get the
	 *			selected tag type in
	 *			{@link ch.njol.skript.lang.SyntaxElement#init(Expression[], int, Kleenean, SkriptParser.ParseResult)}.
	 */
	public static @NotNull String getFullPattern() {
		return getFullPattern(false);
	}

	/**
	 * @param required whether the choice should be optional or required.
	 * @return Returns a choice pattern for use in Skript patterns. Contains parse marks.
	 *			Pass the parse mark to {@link #fromParseMark(int)} to get the
	 *			selected tag type in
	 *			{@link ch.njol.skript.lang.SyntaxElement#init(Expression[], int, Kleenean, SkriptParser.ParseResult)}.
	 */
	public static @NotNull String getFullPattern(boolean required) {
		StringBuilder fullPattern = new StringBuilder(required ? "(" : "[");
		int numRegistries = REGISTERED_TAG_TYPES.size();
		for (int i = 0; i < numRegistries; i++) {
			fullPattern.append(i + 1).append(":").append(REGISTERED_TAG_TYPES.get(i).pattern());
			if (i + 1 != numRegistries)
				fullPattern.append("|");
		}
		fullPattern.append(required ? ")" : "]");
		return fullPattern.toString();
	}

}
