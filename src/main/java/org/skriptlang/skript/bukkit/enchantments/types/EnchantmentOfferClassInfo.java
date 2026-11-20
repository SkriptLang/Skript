package org.skriptlang.skript.bukkit.enchantments.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import org.bukkit.enchantments.EnchantmentOffer;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class EnchantmentOfferClassInfo extends ClassInfo<EnchantmentOffer> {

	public EnchantmentOfferClassInfo() {
		super(EnchantmentOffer.class, "enchantmentoffer");
		user("enchant[ment][ ]offers?")
			.name("Enchantment Offer")
			.description("The enchantmentoffer in an enchant prepare event.")
			.examples("""
				on enchant prepare:
					set enchant offer 1 to sharpness 1
					set the cost of enchant offer 1 to 10 levels
				""")
			.since("2.5")
			.parser(new EnchantmentOfferParser());
	}

	private static class EnchantmentOfferParser extends Parser<EnchantmentOffer> {
		//<editor-fold desc="enchantment offer parser" defaultstate="collapsed">
		@Override
		public boolean canParse(ParseContext context) {
			return false;
		}

		@Override
		public String toString(EnchantmentOffer eo, int flags) {
			return Classes.toString(eo.getEnchantment()) + " " + eo.getEnchantmentLevel();
		}

		@Override
		public String toVariableNameString(EnchantmentOffer eo) {
			return "offer:" + Classes.toString(eo.getEnchantment()) + "=" + eo.getEnchantmentLevel();
		}
		//</editor-fold>
	}

}
