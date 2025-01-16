package org.skriptlang.skript.bukkit.tags.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Keyed;
import org.bukkit.Tag;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.tags.TagModule;
import org.skriptlang.skript.bukkit.tags.TagType;
import org.skriptlang.skript.bukkit.tags.sources.TagOrigin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

@Name("Tags of X")
@Description({
	"Returns all the tags of an item, block, or entity.",
	"`minecraft tag` will return only the vanilla tags, `datapack tag` will return only datapack-provided tags, " +
	"`paper tag` will return only Paper's custom tags (if you are running Paper), " +
	"and `custom tag` will look in the \"skript\" namespace for custom tags you've registered.",
	"You can also filter by tag types using \"item\", \"block\", or \"entity\"."
})
@Examples({
	"broadcast minecraft tags of dirt",
	"send true if paper item tags of target block contains paper tag \"doors\"",
	"broadcast the block tags of player's tool"
})
@Since("2.10")
@RequiredPlugins("Paper (paper tags)")
@Keywords({"blocks", "minecraft tag", "type", "category"})
public class ExprTagsOf extends PropertyExpression<Object, Tag> {

	static {
		Skript.registerExpression(ExprTagsOf.class, Tag.class, ExpressionType.PROPERTY,
				"[all [[of] the]|the] " + TagOrigin.getFullPattern() + " " + TagType.getFullPattern() + " tags of %itemtype/entity/entitydata%",
				"%itemtype/entity/entitydata%'[s] " + TagOrigin.getFullPattern() + " " + TagType.getFullPattern() + " tags");
	}

	TagType<?>[] types;
	TagOrigin origin;
	boolean datapackOnly;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.setExpr(expressions[0]);
		types = TagType.fromParseMark(parseResult.mark);
		origin = TagOrigin.fromParseTags(parseResult.tags);
		datapackOnly = origin == TagOrigin.BUKKIT && parseResult.hasTag("datapack");
		return true;
	}

	@Override
	protected Tag<?> @Nullable [] get(Event event, Object @NotNull [] source) {
		if (source.length == 0)
			return null;
		boolean isAny = (source[0] instanceof ItemType itemType && !itemType.isAll());
		Keyed[] values = TagModule.getKeyed(source[0]);
		if (values == null)
			return null;
		// choose single material if it's something like `any log`
		if (isAny) {
			ThreadLocalRandom random = ThreadLocalRandom.current();
			values = new Keyed[]{values[random.nextInt(0, values.length)]};
		}

		Set<Tag<?>> tags = new TreeSet<>(Comparator.comparing(Keyed::key));
		for (Keyed value : values) {
			tags.addAll(getTags(value));
		}

		return tags.stream()
				.filter(tag ->
					// ensures that only datapack/minecraft tags are sent when specifically requested
					(origin != TagOrigin.BUKKIT || (datapackOnly ^ tag.getKey().getNamespace().equals("minecraft"))))
				.toArray(Tag[]::new);
	}

	/**
	 * Helper method for getting the tags of a value.
	 * @param value The value to get the tags of.
	 * @return The tags the value is a part of.
	 * @param <T> The type of the value.
	 */
	public <T extends Keyed> Collection<Tag<T>> getTags(@NotNull T value) {
		List<Tag<T>> tags = new ArrayList<>();
		//noinspection unchecked
		Class<T> clazz = (Class<T>) value.getClass();
		for (Tag<T> tag : TagModule.tagRegistry.getTags(origin, clazz, types)) {
			if (tag.isTagged(value))
				tags.add(tag);
		}
		return tags;
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<Tag> getReturnType() {
		return Tag.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String registry = types.length > 1 ? "" : " " + types[0].toString();
		return  origin.toString(datapackOnly) + registry + " tags of " + getExpr().toString(event, debug);
	}

}
