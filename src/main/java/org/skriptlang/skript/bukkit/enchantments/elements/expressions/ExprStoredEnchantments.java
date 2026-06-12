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
import ch.njol.skript.util.EnchantmentType;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Name("Stored Enchantments")
@Description("""
	The enchantments stored inside an enchanted book. This is different from enchanting the book, as for example a book
	of sharpness does not deal more damage when used as a weapon. Corresponds to the <code>minecraft:stored_enchantments</code> data component.
	Note that for example resetting the stored enchantments of an enchanted book of Sharpness III removes the sharpness enchantment, since
	it is impossible to know what the original enchantments of the item were. Note also that only one entry can exist per enchantment type;
	adding Sharpness III to a book with Sharpness V will convert it to a book of Sharpness III.
	""")
@Example("""
	command /godbook:
		trigger:
			set {_item} to minecraft:enchanted_book
			add mending to stored enchants of {_item} # adds mending 1
			add knockback 12 to stored enchants of {_item}
			add fire aspect 3 to stored enchants of {_item}
			give {_item} to player
	""")
@Since("INSERT VERSION")
public class ExprStoredEnchantments extends PropertyExpression<ItemType, EnchantmentType> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprStoredEnchantments.class, EnchantmentType.class, "stored enchant[ment]s", "itemtypes", false)
				.supplier(ExprStoredEnchantments::new)
				.build());
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<? extends ItemType>) expressions[0]);
		return true;
	}

	@Override
	protected EnchantmentType[] get(Event event, ItemType[] source) {
		List<EnchantmentType> types = new ArrayList<>();
		for (ItemType type : source) {
			ItemMeta meta = type.getItemMeta();
			if (!(meta instanceof EnchantmentStorageMeta storageMeta))
				continue;

			for (Map.Entry<Enchantment, Integer> entry : storageMeta.getStoredEnchants().entrySet()) {
				types.add(new EnchantmentType(entry.getKey(), entry.getValue()));
			}
		}

		return types.toArray(EnchantmentType[]::new);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, SET, DELETE, REMOVE, REMOVE_ALL -> CollectionUtils.array(EnchantmentType[].class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		ItemType[] targets = getExpr().getArray(event);
		for (ItemType target : targets) {
			ItemMeta rawMeta = target.getItemMeta();
			if (!(rawMeta instanceof EnchantmentStorageMeta meta))
				continue;
			Map<Enchantment, Integer> adjusted = new HashMap<>(meta.getStoredEnchants());
			if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
				adjusted.clear();

			switch (mode) {
				case DELETE -> {}
				case ADD, SET -> {
					assert delta != null;
					for (Object change : delta) {
						if (!(change instanceof EnchantmentType type))
							continue;
						adjusted.put(type.getType(), type.getLevel());
					}
				}
				case REMOVE, REMOVE_ALL -> {
					assert delta != null;
					for (Object change : delta) {
						if (!(change instanceof EnchantmentType type))
							continue;
						if (type.getInternalLevel() == -1) {
							adjusted.remove(type.getType());
						} else {
							adjusted.remove(type.getType(), type.getLevel());
						}
					}
				}
				default -> throw new IllegalArgumentException("Invalid change mode " + mode);
			}

			for (Enchantment existing : meta.getStoredEnchants().keySet())
				meta.removeStoredEnchant(existing);

			for (Map.Entry<Enchantment, Integer> adjustedEntry : adjusted.entrySet())
				meta.addStoredEnchant(adjustedEntry.getKey(), adjustedEntry.getValue(), true);

			target.setItemMeta(meta);
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends EnchantmentType> getReturnType() {
		return EnchantmentType.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "stored enchantments of " + getExpr().toString(event, debug);
	}

}
