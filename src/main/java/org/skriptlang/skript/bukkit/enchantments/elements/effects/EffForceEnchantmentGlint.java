package org.skriptlang.skript.bukkit.enchantments.elements.effects;

import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Force Enchantment Glint")
@Description("Forces the items to glint or not, or removes its existing enchantment glint enforcement.")
@Example("force {_items::*} to glint")
@Example("force the player's tool to stop glinting")
@Since("2.10")
public class EffForceEnchantmentGlint extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffForceEnchantmentGlint.class)
			.addPatterns(
				"(force|make) %itemtypes% [to] [start] glint[ing]",
				"(force|make) %itemtypes% [to] (not|stop) glint[ing]",
				"(clear|delete|reset) [the] enchantment glint override of %itemtypes%",
				"(clear|delete|reset) %itemtypes%'[s] enchantment glint override")
			.supplier(EffForceEnchantmentGlint::new)
			.build());
	}

	private Expression<ItemType> itemTypes;
	private int pattern;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		itemTypes = (Expression<ItemType>) expressions[0];
		pattern = matchedPattern;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (ItemType itemType : itemTypes.getArray(event)) {
			ItemMeta meta = itemType.getItemMeta();
			Boolean glint = switch (pattern) {
				case 0 -> true; // Pattern: forced to glint
				case 1 -> false; // Pattern: forced to not glint
				default -> null; // Pattern: Clear glint override
			};
			meta.setEnchantmentGlintOverride(glint);
			itemType.setItemMeta(meta);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		// Pattern: Clear glint override
		if (pattern > 1)
			return "clear the enchantment glint override of " + itemTypes.toString(event, debug);
		return "force " + itemTypes.toString(event, debug) + " to " + (pattern == 0 ? "start" : "stop") + " glinting";
	}

}
