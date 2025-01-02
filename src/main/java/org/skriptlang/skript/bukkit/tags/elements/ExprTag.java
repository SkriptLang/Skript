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
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.tags.TagModule;
import org.skriptlang.skript.bukkit.tags.TagType;
import org.skriptlang.skript.bukkit.tags.sources.TagOrigin;

import java.util.ArrayList;
import java.util.List;

@Name("Tag")
@Description({
		"Represents a tag which can be used to classify items, blocks, or entities.",
		"Tags are composed of a value and an optional namespace: \"minecraft:oak_logs\".",
		"If you omit the namespace, one will be provided for you, depending on what kind of tag you're using. " +
		"For example, `tag \"doors\"` will be the tag \"minecraft:doors\", " +
		"while `paper tag \"doors\"` will be \"paper:doors\".",
		"`minecraft tag` will search through the vanilla tags, `datapack tag` will search for datapack-provided tags " +
		"(a namespace is required here!), `paper tag` will search for Paper's custom tags if you are running Paper, " +
		"and `custom tag` will look in the \"skript\" namespace for custom tags you've registered.",
		"You can also filter by tag types using \"item\", \"block\", or \"entity\"."
})
@Examples({
		"minecraft tag \"dirt\" # minecraft:dirt",
		"paper tag \"doors\" # paper:doors",
		"tag \"skript:custom_dirt\" # skript:custom_dirt",
		"custom tag \"dirt\" # skript:dirt",
		"datapack block tag \"dirt\" # minecraft:dirt",
		"datapack tag \"my_pack:custom_dirt\" # my_pack:custom_dirt",
		"tag \"minecraft:mineable/pickaxe\" # minecraft:mineable/pickaxe",
		"custom item tag \"blood_magic_sk/can_sacrifice_with\" # skript:blood_magic_sk/can_sacrifice_with"
})
@Since("2.10")
@RequiredPlugins("Paper (paper tags)")
@Keywords({"blocks", "minecraft tag", "type", "category"})
public class ExprTag extends SimpleExpression<Tag> {

	static {
		Skript.registerExpression(ExprTag.class, Tag.class, ExpressionType.COMBINED,
				TagOrigin.getFullPattern() + " " + TagType.getFullPattern() + " tag %strings%");
	}

	private Expression<String> names;
	TagType<?>[] types;
	private TagOrigin origin;
	private boolean datapackOnly;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		names = (Expression<String>) expressions[0];
		types = TagType.fromParseMark(parseResult.mark);
		origin = TagOrigin.fromParseTags(parseResult.tags);
		datapackOnly = origin == TagOrigin.BUKKIT && parseResult.hasTag("datapack");
		return true;
	}

	@Override
	protected Tag<?> @Nullable [] get(Event event) {
		String[] names = this.names.getArray(event);
		List<Tag<?>> tags = new ArrayList<>();

		String namespace = switch (origin) {
			case ANY, BUKKIT -> "minecraft";
			case PAPER -> "paper";
			case SKRIPT -> "skript";
		};

		nextName: for (String name : names) {
			// get key
			NamespacedKey key;
			if (name.contains(":")) {
				key = NamespacedKey.fromString(name);
			} else {
				// populate namespace if not provided
				key = new NamespacedKey(namespace, name);
			}
			if (key == null)
				continue;

			Tag<?> tag;
			for (TagType<?> type : types) {
				tag = TagModule.tagRegistry.getTag(origin, type, key);
				if (tag != null
					// ensures that only datapack/minecraft tags are sent when specifically requested
					&& (origin != TagOrigin.BUKKIT || (datapackOnly ^ tag.getKey().getNamespace().equals("minecraft")))
				) {
					tags.add(tag);
					continue nextName; // ensure 1:1
				}
			}
		}
		return tags.toArray(new Tag[0]);
	}

	@Override
	public boolean isSingle() {
		return names.isSingle();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Class<? extends Tag> getReturnType() {
		return Tag.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String registry = types.length > 1 ? "" : " " + types[0].toString();
		return origin.toString(datapackOnly) + registry + " tag " + names.toString(event, debug);
	}

	@Override
	public Expression<? extends Tag> simplify() {
		if (names instanceof Literal<String>)
			return new SimpleLiteral<>(getArray(null), Tag.class, true);
		return this;
	}

}
