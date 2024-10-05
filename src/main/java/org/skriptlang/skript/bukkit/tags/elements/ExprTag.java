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

@Name("Tag")
@Description({
		"Represents a tag which can be used to classify items, blocks, or entities.",
		"Tags are composed of a value and an optional namespace: \"minecraft:oak_logs\".",
		"If you omit the namespace, one will be provided for you, depending on what kind of tag you're using. " +
		"For example, `tag \"doors\"` will be the tag \"minecraft:doors\", " +
		"while `paper tag \"doors\"` will be \"paper:doors\".",
		"`minecraft tag` will search through the vanilla tags, `datapack tag` will search for datapack-provided tags " +
		"(a  namespace is required here!), `paper tag` will search for Paper's custom tags if you are running Paper, " +
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
		"custom item tag \"blood_magic/sacrificial\" # skript:blood_magic/sacrificial"
})
@Since("INSERT VERSION")
@RequiredPlugins("Paper (paper tags)")
@Keywords({"blocks", "minecraft tag", "type", "category"})
public class ExprTag extends SimpleExpression<Tag> {

	static {
		Skript.registerExpression(ExprTag.class, Tag.class, ExpressionType.COMBINED,
				TagOrigin.getFullPattern() + " " + TagType.getFullPattern() + " tag %string%");
	}

	Expression<String> name;
	int type;
	TagOrigin origin;
	boolean datapackOnly;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		name = (Expression<String>) expressions[0];
		type = parseResult.mark - 1;
		origin = TagOrigin.fromParseTags(parseResult.tags);
		datapackOnly = origin == TagOrigin.BUKKIT && parseResult.hasTag("datapack");
		return true;
	}

	@Override
	protected Tag<?> @Nullable [] get(Event event) {
		String name = this.name.getSingle(event);
		if (name == null)
			return null;

		// get key
		NamespacedKey key;
		if (name.contains(":")) {
			key = NamespacedKey.fromString(name);
		} else {
			// populate namespace if not provided
			String namespace = switch (origin) {
				case ANY, BUKKIT -> "minecraft";
				case PAPER -> "paper";
				case SKRIPT -> "skript";
			};
			key = new NamespacedKey(namespace, name);
		}
		if (key == null)
			return null;

		Tag<?> tag;
		TagType<?>[] types = TagType.getType(type);
		for (TagType<?> type : types) {
			tag = TagModule.TAGS.getTag(origin, type, key);
			if (tag != null
				// ensures that only datapack/minecraft tags are sent when specifically requested
				&& (origin != TagOrigin.BUKKIT || (datapackOnly ^ tag.getKey().getNamespace().equals("minecraft")))
			) {
				return new Tag[]{tag};
			}
		}

		return null;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Class<? extends Tag> getReturnType() {
		return Tag.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String registry = type == -1 ? "" : TagType.getType(type)[0].toString();
		return origin.toString(datapackOnly) + registry + "tag " + name.toString(event, debug);
	}

	@Override
	public Expression<? extends Tag> simplify() {
		if (name instanceof Literal<String>)
			return new SimpleLiteral<>(getArray(null), Tag.class, true);
		return this;
	}

}
