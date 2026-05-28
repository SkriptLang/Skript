package org.skriptlang.skript.bukkit.enchantments;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.YggdrasilSerializer;
import ch.njol.skript.classes.registry.RegistryClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.skript.util.Experience;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.enchantments.elements.conditions.CondIsEnchanted;
import org.skriptlang.skript.bukkit.enchantments.elements.conditions.CondItemEnchantmentGlint;
import org.skriptlang.skript.bukkit.enchantments.elements.effects.EffEnchant;
import org.skriptlang.skript.bukkit.enchantments.elements.effects.EffForceEnchantmentGlint;
import org.skriptlang.skript.bukkit.enchantments.elements.expressions.*;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.lang.converter.Converters;

public class EnchantmentModule extends HierarchicalAddonModule {

	public EnchantmentModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void initSelf(SkriptAddon addon) {
		initClasses();
		initComparators();
		initConverters();
	}

	private void initClasses() {
		Classes.registerClass(new RegistryClassInfo<>(
				Enchantment.class, RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT),
				"enchantment", "enchantments")
			.user("enchantments?")
			.name("Enchantment")
			.description("An enchantment, e.g. 'sharpness' or 'fortune'. Unlike <a href='#enchantmenttype'>enchantment type</a> " +
					"this type has no level, but you usually don't need to use this type anyway.",
					"NOTE: Minecraft namespaces are supported, ex: 'minecraft:basalt_deltas'.",
					"This also supports custom enchantments using namespaces, ex: 'myenchants:explosive'.")
			.examples("")
			.since("1.4.6")
			.before("enchantmenttype"));

		Classes.registerClass(new ClassInfo<>(EnchantmentType.class, "enchantmenttype")
			.user("enchant(ing|ment) types?")
			.name("Enchantment Type")
			.description("An enchantment with an optional level, e.g. 'sharpness 2' or 'fortune'.")
			.usage("<enchantment> [<level>]")
			.examples("enchant the player's tool with sharpness 5",
					"helmet is enchanted with waterbreathing")
			.since("1.4.6")
			.parser(new Parser<>() {
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
			})
			.serializer(new YggdrasilSerializer<>()));

		Classes.registerClass(new ClassInfo<>(EnchantmentOffer.class, "enchantmentoffer")
			.user("enchant[ment][ ]offers?")
			.name("Enchantment Offer")
			.description("The enchantmentoffer in an enchant prepare event.")
			.examples("""
				on enchant prepare:
					set enchant offer 1 to sharpness 1
					set the cost of enchant offer 1 to 10 levels
				""")
			.since("2.5")
			.parser(new Parser<>() {
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
			}));
	}

	private void initComparators() {
		Comparators.registerComparator(EnchantmentOffer.class, EnchantmentType.class, (offer, enchantmentType) ->
			Relation.get(offer.getEnchantment() == enchantmentType.getType() && offer.getEnchantmentLevel() == enchantmentType.getLevel()));
		Comparators.registerComparator(EnchantmentOffer.class, Experience.class, (offer, experience) ->
			Relation.get(offer.getCost() == experience.getXP()));
		Comparators.registerComparator(EnchantmentType.class, Enchantment.class, ((enchantmentType, enchantment) ->
			Relation.get(enchantmentType.getType().equals(enchantment))));
	}

	private void initConverters() {
		Converters.registerConverter(Enchantment.class, EnchantmentType.class, e -> new EnchantmentType(e, -1));
		Converters.registerConverter(EnchantmentOffer.class, EnchantmentType.class, eo -> new EnchantmentType(eo.getEnchantment(), eo.getEnchantmentLevel()));
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon,
			CondIsEnchanted::register,
			CondItemEnchantmentGlint::register,
			EffEnchant::register,
			EffForceEnchantmentGlint::register,
			ExprAppliedEnchantments::register,
			ExprEnchantmentLevel::register,
			ExprEnchantmentOffer::register,
			ExprEnchantmentBonus::register,
			ExprEnchantmentOfferCost::register,
			ExprEnchantments::register,
			ExprEnchantingExpCost::register,
			ExprEnchantItem::register,
			ExprItemWithEnchantmentGlint::register,
			ExprMaximumEnchantmentLevel::register,
			ExprMinimumEnchantmentLevel::register,
			ExprStoredEnchantments::register
		);
	}

	@Override
	public String name() {
		return "enchantment";
	}
}
