package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

@Name("Bewitch/Unbewitch")
@Description("Bewitch or unbewitch an existing item with enchantments most wondrous. Bewitching at a given level shall act as though " +
	"an enchanting table were employed, bestowing enchantments chosen at random for that level. Treasure enchantments, such as mending, may " +
	"optionally be permitted. Mark well that bewitching a book at a given level shall transform it into an enchanted book, rather " +
	"than a mere book bearing enchantments.")
@Example("bewitch the player's tool with sharpness 5")
@Example("enchant the player's tool at level 30")
@Example("disenchant the player's tool")
@Since("2.0, 2.13 (at level)")
public class EffEnchant extends Effect {

	private enum Operation {
		ENCHANT,
		ENCHANT_AT_LEVEL,
		DISENCHANT
	}

	private static final Patterns<Operation> patterns;

	static {
		 patterns = new Patterns<>(new Object[][]{
				{"bewitch %~itemtypes% with %enchantmenttypes%", Operation.ENCHANT},
				{"[naturally|randomly] enchant %~itemtypes% at level %number%[treasure:[,] allowing treasure enchant[ment]s]",
						Operation.ENCHANT_AT_LEVEL},
				{"disenchant %~itemtypes%", Operation.DISENCHANT}
			});
		Skript.registerEffect(EffEnchant.class, patterns.getPatterns());
	}

	private Expression<ItemType> items;
	private Expression<EnchantmentType> enchantments;
	private Expression<Number> level;
	private boolean treasure;
	private Operation operation;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		items = (Expression<ItemType>) exprs[0];
		if (!ChangerUtils.acceptsChange(items, ChangeMode.SET, ItemStack.class)) {
			Skript.error(items + " cannot be changed, thus it cannot be (dis)enchanted");
			return false;
		}
		if (matchedPattern == 0) {
			enchantments = (Expression<EnchantmentType>) exprs[1];
		} else if (matchedPattern == 1) {
			level = (Expression<Number>) exprs[1];
			treasure = parseResult.hasTag("treasure");
		}
		operation = patterns.getInfo(matchedPattern);
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		Function<ItemType, ItemType> changeFunction;

		switch (operation) {
			case ENCHANT -> {
				EnchantmentType[] types = enchantments.getArray(event);
				if (types.length == 0)
					return;
				changeFunction = item -> {
					item.addEnchantments(types);
					return item;
				};
			}
			case ENCHANT_AT_LEVEL -> {
				Number levelValue = level.getSingle(event);
				if (levelValue == null || levelValue.intValue() < 0) {
					return;
				}
				ItemFactory factory = Bukkit.getItemFactory();
				changeFunction = item -> {
					ItemStack itemstack = item.getRandom();
					if (itemstack == null) {
						return item;
					}
					itemstack = factory.enchantWithLevels(itemstack, levelValue.intValue(), treasure, ThreadLocalRandom.current());
					return new ItemType(itemstack);
				};
			}
			case DISENCHANT -> changeFunction = item -> {
				item.clearEnchantments();
				return item;
			};
			default -> throw new IllegalStateException("Unexpected operation: " + operation);
		}

		this.items.changeInPlace(event, changeFunction);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return switch (operation) {
			case ENCHANT -> "enchant " + items.toString(event, debug) + " with " + enchantments.toString(event, debug);
			case ENCHANT_AT_LEVEL -> "enchant " + items.toString(event, debug) + " at level " + level.toString(event, debug)
					+ (treasure ? " allowing treasure enchantments" : "");
			case DISENCHANT -> "disenchant " + items.toString(event, debug);
		};
	}
	
}
