package org.skriptlang.skript.bukkit.enchantments.elements.expressions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Item Enchantments")
@Description("All the enchantments an <a href='#itemtype'>item type</a> has.")
@Example("clear enchantments of event-item")
@Since("2.2-dev36")
public class ExprEnchantments extends PropertyExpression<ItemType, EnchantmentType> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprEnchantments.class, EnchantmentType.class, "enchantments", "itemtypes", false)
				.supplier(ExprEnchantments::new)
				.build());
	}

	@Override
	@SuppressWarnings({"null","unchecked"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<ItemType>) exprs[0]);
		return true;
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	protected EnchantmentType[] get(Event event, ItemType[] source) {
		List<EnchantmentType> enchantments = new ArrayList<>();
		for (ItemType item : source) {
			EnchantmentType[] enchants = item.getEnchantmentTypes();
			
			if (enchants == null)
				continue;
			
			Collections.addAll(enchantments, enchants);
		}
		return enchantments.toArray(new EnchantmentType[0]);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, SET, REMOVE, REMOVE_ALL, DELETE, RESET -> CollectionUtils.array(EnchantmentType[].class);
			default -> null;
		};
	}

	
	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		ItemType[] source = getExpr().getArray(event);

		EnchantmentType[] enchants = new EnchantmentType[delta != null ? delta.length : 0];

		if (delta != null) {
			for (int i = 0; i < delta.length; i++) {
				enchants[i] = (EnchantmentType) delta[i];
			}
		}
		
		switch (mode) {
			case ADD:
				for (ItemType item : source)
					item.addEnchantments(enchants);
				break;
			case REMOVE:
			case REMOVE_ALL:
				for (ItemType item : source) {
					ItemMeta meta = item.getItemMeta();
					assert meta != null;
					for (EnchantmentType enchant : enchants) {
						Enchantment ench = enchant.getType();
						assert ench != null;
						if (enchant.getInternalLevel() == -1
								|| meta.getEnchantLevel(ench) == enchant.getLevel()) {
							// Remove directly from meta since it's more efficient in this case
							meta.removeEnchant(ench);
						}
					}
					item.setItemMeta(meta);
				}
				break;
			case SET:
				for (ItemType item : source) {
					item.clearEnchantments();
					item.addEnchantments(enchants);
				}
				break;
			case DELETE:
			case RESET:
				for (ItemType item : source)
					item.clearEnchantments();
				break;
		}
	}

	@Override
	public Class<? extends EnchantmentType> getReturnType() {
		return EnchantmentType.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the enchantments of " + getExpr().toString(event, debug);
	}
	
}
