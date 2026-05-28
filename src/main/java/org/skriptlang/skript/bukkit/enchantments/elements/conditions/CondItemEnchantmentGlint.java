package org.skriptlang.skript.bukkit.enchantments.elements.conditions;

import org.bukkit.inventory.meta.ItemMeta;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Item Has Enchantment Glint Override")
@Description("Checks whether an item has the enchantment glint overridden, or is forced to glint or not.")
@Example("""
	if the player's tool has the enchantment glint override
		send "Your tool has the enchantment glint override." to player
	""")
@Example("""
	if {_item} is forced to glint:
		send "This item is forced to glint." to player
	else if {_item} is forced to not glint:
		send "This item is forced to not glint." to player
	else:
		send "This item does not have any glint override." to player
	""")
@Since("2.10")
public class CondItemEnchantmentGlint extends PropertyCondition<ItemType> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.CONDITION, PropertyCondition
			.infoBuilder(CondItemEnchantmentGlint.class, PropertyType.HAVE, "enchantment glint overrid(den|e)", "itemtypes")
			.addPatterns(getPatterns(PropertyType.BE, "forced to [:not] glint", "itemtypes")).build());
	}

	private int matchedPattern;
	private boolean glint;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.matchedPattern = matchedPattern;
		glint = !parseResult.hasTag("not");
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(ItemType itemType) {
		ItemMeta meta = itemType.getItemMeta();
		// enchantment glint override
		if (matchedPattern == 0)
			return meta.hasEnchantmentGlintOverride();
		// forced to glint
		if (!meta.hasEnchantmentGlintOverride())
			return false;
		return meta.getEnchantmentGlintOverride();
	}

	@Override
	protected String getPropertyName() {
		if (matchedPattern == 0)
			return "enchantment glint overridden";
		return "forced to " + (glint ? "" : "not ") + "glint";
	}

}
