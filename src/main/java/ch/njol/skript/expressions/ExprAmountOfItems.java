package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.simplification.Simplifiable;

@Name("Amount of Items")
@Description("Counts how many of a particular <a href='./classes.html#itemtype'>item type</a> are in a given inventory.")
@Examples("message \"You have %number of ores in the player's inventory% ores in your inventory.\"")
@Since("2.0")
public class ExprAmountOfItems extends SimpleExpression<Long> {
  
	static {
		Skript.registerExpression(ExprAmountOfItems.class, Long.class, ExpressionType.PROPERTY, "[the] (amount|number) of %itemtypes% (in|of) %inventories%");
	}
	
	private Expression<ItemType> items;
	private Expression<Inventory> inventories;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		items = (Expression<ItemType>) exprs[0];
		inventories = (Expression<Inventory>) exprs[1];
		return true;
	}
	
	@Override
	protected Long[] get(Event e) {
		ItemType[] itemTypes = items.getArray(e);
		long amount = 0;
		for (Inventory inventory : inventories.getArray(e)) {
			itemsLoop: for (ItemStack itemStack : inventory.getContents()) {
				if (itemStack != null) {
					for (ItemType itemType : itemTypes) {
						if (new ItemType(itemStack).isSimilar(itemType)) {
							amount += itemStack.getAmount();
							continue itemsLoop;
						}
					}
				}
			}
		}
		return new Long[]{amount};
	}
	
	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Expression<Long> simplify(Step step, @Nullable Simplifiable<?> source) {
		items = simplifyChild(items, step, source);
		inventories = simplifyChild(inventories, step, source);
		if (items instanceof Literal<ItemType> && inventories instanceof Literal<Inventory>)
			return getAsSimplifiedLiteral();
		return this;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the number of " + items.toString(e, debug) + " in " + inventories.toString(e, debug);
	}

}
