package org.skriptlang.skript.bukkit.enchantments;

import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.skript.util.Experience;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.enchantments.elements.conditions.CondIsEnchanted;
import org.skriptlang.skript.bukkit.enchantments.elements.conditions.CondItemEnchantmentGlint;
import org.skriptlang.skript.bukkit.enchantments.elements.effects.EffEnchant;
import org.skriptlang.skript.bukkit.enchantments.elements.effects.EffForceEnchantmentGlint;
import org.skriptlang.skript.bukkit.enchantments.elements.expressions.*;
import org.skriptlang.skript.bukkit.enchantments.types.EnchantmentClassInfo;
import org.skriptlang.skript.bukkit.enchantments.types.EnchantmentOfferClassInfo;
import org.skriptlang.skript.bukkit.enchantments.types.EnchantmentTypeClassInfo;
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
		Classes.registerClass(new EnchantmentClassInfo());
		Classes.registerClass(new EnchantmentTypeClassInfo());
		Classes.registerClass(new EnchantmentOfferClassInfo());
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
			ExprEnchantingExpCost::register,
			ExprEnchantItem::register,
			ExprEnchantmentBonus::register,
			ExprEnchantmentHint::register,
			ExprEnchantmentLevel::register,
			ExprEnchantmentOffer::register,
			ExprEnchantmentOfferCost::register,
			ExprEnchantments::register,
			ExprItemWithEnchantmentGlint::register,
			ExprMinMaxEnchantmentLevel::register,
			ExprStoredEnchantments::register
		);
	}

	@Override
	public String name() {
		return "enchantment";
	}

}
