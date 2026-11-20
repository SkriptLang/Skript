package org.skriptlang.skript.bukkit.enchantments.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.YggdrasilSerializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.util.EnchantmentType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public class EnchantmentTypeClassInfo extends ClassInfo<EnchantmentType> {

	public EnchantmentTypeClassInfo() {
		super(EnchantmentType.class, "enchantmenttype");
		user("enchant(ing|ment) ?types?")
			.name("Enchantment Type")
			.description("An enchantment with an optional level, e.g. 'sharpness 2' or 'fortune'.")
			.usage("<enchantment> [<level>]")
			.examples("enchant the player's tool with sharpness 5",
					"helmet is enchanted with waterbreathing")
			.since("1.4.6")
			.parser(new EnchantmentTypeParser())
			.serializer(new YggdrasilSerializer<>());
	}

	private static class EnchantmentTypeParser extends Parser<EnchantmentType> {
		//<editor-fold desc="enchantment type parser" defaultstate="collapsed">
		@Override
		public @Nullable EnchantmentType parse(String string, ParseContext context) {
			return EnchantmentType.parse(string);
		}

		@Override
		public String toString(EnchantmentType type, int flags) {
			return type.toString();
		}

		@Override
		public String toVariableNameString(EnchantmentType type) {
			return type.toString();
		}
		//</editor-fold>
	}

}
