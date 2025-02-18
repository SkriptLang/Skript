package org.skriptlang.skript.bukkit.tags.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.tags.SkriptTag;
import org.skriptlang.skript.bukkit.tags.TagModule;
import org.skriptlang.skript.bukkit.tags.TagType;
import org.skriptlang.skript.bukkit.tags.sources.SkriptTagSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

@Name("Register Tag")
@Description({
	"Registers a new tag containing either items or entity datas. Note that items will NOT keep any information other " +
	"than their type, so adding `diamond sword named \"test\"` to a tag is the same as adding `diamond sword`",
	"Item tags should be used for contexts where the item is not placed down, while block tags should be used " +
	"for contexts where the item is placed. For example, and item tag could be \"skript:edible\", " +
	"while a block tag would be \"skript:needs_water_above\".",
	"All custom tags will be given the namespace \"skript\", followed by the name you give it. The name must only " +
	"include the characters A to Z, 0 to 9, and '/', '.', '_', and '-'. Otherwise, the tag will not register.",
	"",
	"Please note that two tags can share a name if they are of different types. Registering a new tag of the same " +
	"name and type will overwrite the existing tag. Tags will reset on server shutdown."
})
@Examples({
	"register a new custom entity tag named \"fish\" using cod, salmon, tropical fish, and pufferfish",
	"register an item tag named \"skript:wasp_weapons/swords\" containing diamond sword and netherite sword",
	"register block tag named \"pokey\" containing sweet berry bush and bamboo sapling",
	"",
	"on player move:",
		"\tblock at player is tagged as tag \"skript:pokey\"",
		"\tdamage the player by 1 heart"
})
@Since("2.10")
@Keywords({"blocks", "minecraft tag", "type", "category"})
public class EffRegisterTag extends Effect {

	private static final Pattern KEY_PATTERN = Pattern.compile("[a-zA-Z0-9/._-]+");

	static {
		Skript.registerEffect(EffRegisterTag.class,
				"register [a[n]] [custom] " + TagType.getFullPattern(true) +
					" tag named %string% (containing|using) %entitydatas/itemtypes%");
	}

	private Expression<String> name;
	private Expression<?> contents;
	private TagType<?> type;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		name = (Expression<String>) expressions[0];
		if (name instanceof Literal<String> literal) {
			String key = removeSkriptNamespace(literal.getSingle());
			if (!KEY_PATTERN.matcher(key).matches()) {
				Skript.error("Tag names can only contain the following characters: letters, numbers, and some symbols: " +
						"'/', '.', '_', and '-'");
				return false;
			}
		}

		contents = expressions[1];
		type = TagType.getType(parseResult.mark - 1)[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		String name = this.name.getSingle(event);
		if (name == null)
			return;

		name = removeSkriptNamespace(name);

		if (!KEY_PATTERN.matcher(name).matches())
			return;

		NamespacedKey key = new NamespacedKey(Skript.getInstance(), name);

		Object[] contents = this.contents.getArray(event);
		if (contents.length == 0)
			return;


		if (this.type.type() == Material.class) {
			Tag<Material> tag = getMaterialTag(key, contents);
			if (this.type == TagType.ITEMS) {
				SkriptTagSource.ITEMS().addTag(tag);
			} else if (this.type == TagType.BLOCKS) {
				SkriptTagSource.BLOCKS().addTag(tag);
			}

		} else if (this.type.type() == EntityType.class) {
			Tag<EntityType> tag = getEntityTag(key, contents);
			SkriptTagSource.ENTITIES().addTag(tag);
		}
	}

	private static @NotNull String removeSkriptNamespace(@NotNull String key) {
		if (key.startsWith("skript:"))
			key = key.substring(7);
		return key;
	}

	@Contract("_, _ -> new")
	private @NotNull Tag<Material> getMaterialTag(NamespacedKey key, Object @NotNull [] contents) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		List<Material> tagContents = new ArrayList<>();
		for (Object object : contents) {
			Keyed[] values = TagModule.getKeyed(object);
			if (object instanceof ItemType itemType && !itemType.isAll()) {
				// add random
				tagContents.add((Material) values[random.nextInt(0, values.length)]);
			} else {
				for (Keyed value : values) {
					if (value instanceof Material material)
						tagContents.add(material);
				}
			}
		}
		return new SkriptTag<>(key, tagContents);
	}

	@Contract("_, _ -> new")
	private @NotNull Tag<EntityType> getEntityTag(NamespacedKey key, Object @NotNull [] contents) {
		List<EntityType> tagContents = new ArrayList<>();
		for (Object object : contents) {
			for (Keyed value : TagModule.getKeyed(object)) {
				if (value instanceof EntityType entityType)
					tagContents.add(entityType);
			}
		}
		return new SkriptTag<>(key, tagContents);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("register a new", type.toString(), "tag named", name, "containing", contents)
			.toString();
	}

}
