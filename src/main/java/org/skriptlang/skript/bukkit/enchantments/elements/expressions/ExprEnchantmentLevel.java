package org.skriptlang.skript.bukkit.enchantments.elements.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.stream.Stream;

import static org.skriptlang.skript.registration.DefaultSyntaxInfos.Expression.builder;

@Name("Enchantment Level")
@Description("The level of a particular <a href='#enchantment'>enchantment</a> on an item.")
@Example("""
	player's tool is a sword of sharpness:
		message "You have a sword of sharpness %level of sharpness of the player's tool% equipped"
	""")
@Since("2.0")
public class ExprEnchantmentLevel extends SimpleExpression<Long> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, builder(ExprEnchantmentLevel.class, Long.class)
			.addPatterns(
				"[the] [enchant[ment]] level[s] of %enchantments% (on|of) %itemtypes%",
				"[the] %enchantments% [enchant[ment]] level[s] (on|of) %itemtypes%",
				"%itemtypes%'[s] %enchantments% [enchant[ment]] level[s]",
				"%itemtypes%'[s] [enchant[ment]] level[s] of %enchantments%")
			.supplier(ExprEnchantmentLevel::new)
			.priority(PropertyExpression.DEFAULT_PRIORITY)
			.build());
	}

	private Expression<ItemType> items;

	private Expression<Enchantment> enchants;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		int i = matchedPattern < 2 ? 1 : 0;
		items = (Expression<ItemType>) exprs[i];
		enchants = (Expression<Enchantment>) exprs[i ^ 1];
		return true;
	}

	@Override
	protected Long[] get(Event event) {
		Enchantment[] enchantments = enchants.getArray(event);
		return Stream.of(items.getArray(event))
			.map(ItemType::getEnchantmentTypes)
			.flatMap(Stream::of)
			.filter(enchantment -> CollectionUtils.contains(enchantments, enchantment.getType()))
			.map(EnchantmentType::getLevel)
			.map(i -> (long) i)
			.toArray(Long[]::new);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, REMOVE, ADD -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		ItemType[] itemTypes = items.getArray(event);
		Enchantment[] enchantments = enchants.getArray(event);
		assert delta != null;
		long changeValue = ((Number) delta[0]).longValue();

		for (ItemType itemType : itemTypes) {
			for (Enchantment enchantment : enchantments) {
				EnchantmentType enchantmentType = itemType.getEnchantmentType(enchantment);
				long oldLevel = enchantmentType == null ? 0 : enchantmentType.getLevel();

				long newItemLevel;
				switch (mode) {
					case ADD -> newItemLevel = Math2.addSaturated(oldLevel, changeValue);
					case REMOVE -> newItemLevel = Math2.addSaturated(oldLevel, -changeValue);
					case SET -> newItemLevel = changeValue;
					default -> {
						assert false;
						return;
					}
				}

				if (newItemLevel <= 0) {
					itemType.removeEnchantments(new EnchantmentType(enchantment));
				} else {
					itemType.addEnchantments(new EnchantmentType(enchantment, Math2.clampToInt(newItemLevel)));
				}
			}
		}
	}

	@Override
	public boolean isSingle() {
		return items.isSingle() && enchants.isSingle();
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the level of " + enchants.toString(event, debug) + " of " + items.toString(event, debug);
	}

}
