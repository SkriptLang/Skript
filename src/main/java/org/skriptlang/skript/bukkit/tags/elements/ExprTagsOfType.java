package org.skriptlang.skript.bukkit.tags.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Keyed;
import org.bukkit.Tag;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.tags.TagModule;
import org.skriptlang.skript.bukkit.tags.TagType;
import org.skriptlang.skript.bukkit.tags.sources.TagOrigin;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

@Name("All Tags of a Type")
@Description({
	"Returns all the tags.",
	"`minecraft tag` will return only the vanilla tags, `datapack tag` will return only datapack-provided tags, " +
		"`paper tag` will return only Paper's custom tags (if you are running Paper), " +
		"and `custom tag` will look in the \"skript\" namespace for custom tags you've registered.",
	"You can also filter by tag types using \"item\", \"block\", or \"entity\"."
})
@Examples({
	"broadcast minecraft tags",
	"send paper entity tags",
	"broadcast all block tags"
})
@Since("INSERT VERSION")
@RequiredPlugins("Paper (paper tags)")
@Keywords({"blocks", "minecraft tag", "type", "category"})
public class ExprTagsOfType extends SimpleExpression<Tag> {

	static {
		Skript.registerExpression(ExprTagsOfType.class, Tag.class, ExpressionType.SIMPLE,
				"[all [[of] the]] " + TagOrigin.getFullPattern() + " " + TagType.getFullPattern() + " tags");
	}

	int type;
	TagOrigin origin;
	boolean datapackOnly;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		type = parseResult.mark - 1;
		origin = TagOrigin.fromParseTags(parseResult.tags);
		datapackOnly = origin == TagOrigin.BUKKIT && parseResult.hasTag("datapack");
		return true;
	}

	@Override
	protected Tag<?> @Nullable [] get(Event event) {
		TagType<?>[] types = TagType.getType(type);
		Set<Tag<?>> tags = new TreeSet<>(Comparator.comparing(Keyed::key));
		for (TagType<?> type : types) {
			for (Tag<?> tag : TagModule.TAGS.getTagsMatching(origin, type,
				tag -> (origin != TagOrigin.BUKKIT || (datapackOnly ^ tag.getKey().getNamespace().equals("minecraft"))))
			) {
				tags.add(tag);
			}
		}
		return tags.toArray(new Tag[0]);
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
		String registry = type == -1 ? "" : TagType.getType(type)[0].pattern();
		return "all of the " + origin.toString(datapackOnly) + registry + "tags";
	}
}
