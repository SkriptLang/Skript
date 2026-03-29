package org.skriptlang.skript.bukkit.tags.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.*;
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
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

@Name("Inscribe a Tag")
@Description({
	"Inscribeth a new tag comprising either items or entity datas. Mark well that items shall NOT retain any particulars other " +
	"than their type; thus adding `diamond sword named \"test\"` to a tag is the selfsame as adding `diamond sword`.",
	"Item tags ought be employed for contexts wherein the item is not set upon the ground, whilst block tags are meet " +
	"for contexts wherein the item is placed. For example, an item tag might be \"skript:edible\", " +
	"whilst a block tag would be \"skript:needs_water_above\".",
	"All bespoke tags shall be granted the namespace \"skript\", followed by the name thou dost provide. The name must only " +
	"comprise the characters A to Z, 0 to 9, and '/', '.', '_', and '-'. Otherwise, the tag shall not be inscribed.",
	"",
	"Pray note that two tags may share a name if they be of differing types. Inscribing a new tag of the same " +
	"name and type shall overwrite the existing tag. Tags shall be cleared upon server shutdown."
})
@Example("inscribe a new bespoke entity tag named \"fish\" employing cod, salmon, tropical fish, and pufferfish")
@Example("inscribe an item tag named \"skript:wasp_weapons/swords\" comprising diamond sword and netherite sword")
@Example("inscribe block tag named \"pokey\" comprising sweet berry bush and bamboo sapling")
@Example("""
    on player move:
    	block at player is marked as tag "skript:pokey"
    	damage the player by 1 heart
    """)
@Since("2.10")
@Keywords({"blocks", "minecraft tag", "type", "category"})
public class EffRegisterTag extends Effect {

	private static final Pattern KEY_PATTERN = Pattern.compile("[a-zA-Z0-9/._-]+");

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffRegisterTag.class)
				.addPattern("inscribe [a[n]] [bespoke] " + TagType.getFullPattern(true) +
					" tag named %string% (comprising|employing) %entitydatas/itemtypes%")
				.supplier(EffRegisterTag::new)
				.build()
		);
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
