package org.skriptlang.skript.bukkit.potion;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.localization.Language;
import ch.njol.util.StringUtils;
import ch.njol.yggdrasil.Fields;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.PotionUtils;

import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Utility for obtaining a {@link ClassInfo} for {@link PotionEffectType} using legacy methods.
 * This applies for versions older than 1.20.3, or more specifically,
 *  where {@link PotionUtils#getPotionEffectTypeRegistry()} is null.
 */
final class LegacyPotionEffectTypeInfo {

	private static Map<String, PotionEffectType> types;
	private static Map<String, String> names;

	private static void init() {
		if (types != null) {
			return;
		}
		types = new HashMap<>();
		names = new HashMap<>();
		Language.addListener(() -> {
			types.clear();
			names.clear();
			for (PotionEffectType potionEffectType : PotionEffectType.values()) {
				String key = potionEffectType.getKey().getKey();
				String[] entries = Language.getList("potion effect types." + key);
				names.put(key, entries[0]);
				for (String entry : entries) {
					types.put(entry.toLowerCase(Locale.ENGLISH), potionEffectType);
				}
			}
		});
	}

	private LegacyPotionEffectTypeInfo() { }

	/**
	 * Initializes and returns a ClassInfo for working with {@link PotionEffectType}s.
	 */
	public static ClassInfo<PotionEffectType> getInfo() {
		init();
		return new ClassInfo<>(PotionEffectType.class, "potioneffecttype")
			.user("potion ?effect ?types?")
			.name("Potion Effect Type")
			.description("A potion effect type, e.g. 'strength' or 'swiftness'.")
			.usage(StringUtils.join(names.values().toArray(new String[0]), ", "))
			.examples(
				"apply swiftness 5 to the player",
				"apply potion of speed 2 to the player for 60 seconds",
				"remove invisibility from the victim"
			)
			.since("")
			.supplier(PotionEffectType.values())
			.parser(new Parser<>() {
				@Override
				public @Nullable PotionEffectType parse(String input, ParseContext context) {
					return types.get(input.toLowerCase(Locale.ENGLISH));
				}

				@Override
				public String toString(PotionEffectType type, int flags) {
					return names.get(type.getKey().getKey());
				}

				@Override
				public String toVariableNameString(PotionEffectType type) {
					return "potioneffecttype:" + type.getName();
				}
			})
			.serializer(new Serializer<>() {
				@Override
				public Fields serialize(PotionEffectType type) {
					Fields fields = new Fields();
					fields.putObject("name", type.getName());
					return fields;
				}

				@Override
				public void deserialize(PotionEffectType type, Fields fields) {
					assert false;
				}

				@Override
				protected PotionEffectType deserialize(Fields fields) throws StreamCorruptedException {
					String name = fields.getObject("name", String.class);
					assert name != null;
					PotionEffectType type = PotionEffectType.getByName(name);
					if (type == null) {
						throw new StreamCorruptedException("Invalid PotionEffectType " + name);
					}
					return type;
				}

				@Override
				public boolean mustSyncDeserialization() {
					return false;
				}

				@Override
				public boolean canBeInstantiated() {
					return false;
				}
			});
	}

}
