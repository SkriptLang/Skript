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

import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

final class LegacyPotionEffectTypeInfo {

	private static Map<String, PotionEffectType> types;
	private static Map<String, String> names;

	private static void init() {
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
				@Nullable
				public PotionEffectType parse(String input, ParseContext context) {
					return types.get(input.toLowerCase(Locale.ENGLISH));
				}

				@Override
				public String toString(PotionEffectType potionEffectType, int flags) {
					return names.get(potionEffectType.getKey().getKey());
				}

				@Override
				public String toVariableNameString(PotionEffectType p) {
					return "potioneffecttype:" + p.getName();
				}
			})
			.serializer(new Serializer<>() {
				@Override
				public Fields serialize(final PotionEffectType o) {
					final Fields f = new Fields();
					f.putObject("name", o.getName());
					return f;
				}

				@Override
				public void deserialize(final PotionEffectType o, final Fields f) {
					assert false;
				}

				@Override
				protected PotionEffectType deserialize(final Fields fields) throws StreamCorruptedException {
					final String name = fields.getObject("name", String.class);
					assert name != null;
					final PotionEffectType t = PotionEffectType.getByName(name);
					if (t == null)
						throw new StreamCorruptedException("Invalid PotionEffectType " + name);
					return t;
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
