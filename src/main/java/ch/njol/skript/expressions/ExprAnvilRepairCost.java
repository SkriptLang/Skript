package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

@Name("Anvil Repair Cost")
@Description({
	"Returns the experience cost (in levels) to complete the current repair or the maximum experience cost (in levels) to be allowed by the current repair.",
	"The default value of max cost set by vanilla Minecraft is 40."
})
@Examples({
	"on inventory click:",
		"\tif {AnvilRepairSaleActive} = true:",
		"\t\twait a tick # recommended, to avoid client bugs",
		"\t\tset anvil repair cost to anvil repair cost * 50%",
		"\t\tsend \"Anvil repair sale is ON!\" to player",
	"",
	"on inventory click:",
		"\tplayer have permission \"anvil.repair.max.bypass\"",
		"\tset max repair cost of event-inventory to 99999"
})
@Since("2.8.0")
public class ExprAnvilRepairCost extends SimplePropertyExpression<Inventory, Integer> {

	static {
		registerDefault(ExprAnvilRepairCost.class, Integer.class, "[anvil] [item] [:max[imum]] repair cost", "inventories");
	}

	private boolean isMax;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isMax = parseResult.hasTag("max");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	@Nullable
	public Integer convert(Inventory inventory) {
		if (!(inventory instanceof AnvilInventory))
			return null;

		AnvilInventory anvilInventory = (AnvilInventory) inventory;
		return isMax ? anvilInventory.getMaximumRepairCost() : anvilInventory.getRepairCost();
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case ADD:
			case REMOVE:
			case SET:
				return CollectionUtils.array(Number.class);
			default:
				return null;
		}
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		int value = ((Number) delta[0]).intValue() * (mode == ChangeMode.REMOVE ? -1 : 1);
		for (Inventory inventory : getExpr().getArray(event)) {
			if (inventory instanceof AnvilInventory) {
				AnvilInventory anvilInventory = (AnvilInventory) inventory;
				int change = mode == ChangeMode.SET ? 0 : (isMax ? anvilInventory.getMaximumRepairCost() : anvilInventory.getRepairCost());
				int newValue = Math.max((change + value), 0);

				if (isMax)
					anvilInventory.setMaximumRepairCost(newValue);
				else
					anvilInventory.setRepairCost(newValue);
			}
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	public String getPropertyName() {
		return "anvil item" + (isMax ? " max" : "") + " repair cost";
	}
	
}
