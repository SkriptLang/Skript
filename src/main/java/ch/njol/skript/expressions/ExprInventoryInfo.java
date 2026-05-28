package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@Name("Inventory Holder/Viewers/Rows/Slots")
@Description({"Gets the amount of rows/slots, viewers and holder of an inventory.",
	"",
	"NOTE: 'Viewers' expression returns a list of players viewing the inventory. Note that a player is considered to be viewing their own inventory and internal crafting screen even when said inventory is not open."})
@Example("event-inventory's amount of rows")
@Example("holder of player's top inventory")
@Example("{_inventory}'s viewers")
@Since("2.2-dev34, 2.5 (slots)")
public class ExprInventoryInfo extends SimpleExpression<Object> {
	
	private final static int HOLDER = 1, VIEWERS = 2, ROWS = 3, SLOTS = 4;
	
	static {
		Skript.registerExpression(ExprInventoryInfo.class, Object.class, ExpressionType.PROPERTY,
				"(" + HOLDER + "¦holder[s]|" + VIEWERS + "¦viewers|" + ROWS + "¦[amount of] rows|" + SLOTS + "¦[amount of] slots)" + " of %inventories%",
				"%inventories%'[s] (" + HOLDER + "¦holder[s]|" + VIEWERS + "¦viewers|" + ROWS + "¦[amount of] rows|" + SLOTS + "¦[amount of] slots)");
	}
	
	@SuppressWarnings("null")
	private Expression<Inventory> inventories;
	private int type;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		inventories = (Expression<Inventory>) exprs[0];
		type = parseResult.mark;
		return true;
	}

	@Override
	protected Object[] get(Event event) {
		Inventory[] inventories = this.inventories.getArray(event);
		switch (type) {
			case HOLDER:
				List<InventoryHolder> holders = new ArrayList<>();
				for (Inventory inventory : inventories) {
					InventoryHolder holder = inventory.getHolder();
					if (holder != null)
						holders.add(holder);
				}
				return holders.toArray(new InventoryHolder[0]);
			case ROWS:
				List<Number> rows = new ArrayList<>();
				for (Inventory inventory : inventories) {
					int size = inventory.getSize();
					if (size < 9) // Hoppers have a size of 5, we don't want to return 0
						rows.add(1);
					else
						rows.add(size / 9);
				}
				return rows.toArray(new Number[0]);
			case SLOTS:
				List<Number> sizes = new ArrayList<>();
				for (Inventory inventory : inventories) {
					sizes.add(inventory.getSize());
				}
				return sizes.toArray(new Number[0]);
			case VIEWERS:
				List<HumanEntity> viewers = new ArrayList<>();
				for (Inventory inventory : inventories) {
					viewers.addAll(inventory.getViewers());
				}
				return viewers.stream().filter(viewer -> viewer instanceof Player).toArray(Player[]::new);
			default:
				return (Object[]) Array.newInstance(getReturnType(), 0);
		}
	}
	
	@Override
	public boolean isSingle() {
		return inventories.isSingle() && type != VIEWERS;
	}

	@Override
	public Class<?> getReturnType() {
		return type == HOLDER ? InventoryHolder.class : (type == ROWS || type == SLOTS) ? Number.class : Player.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return (type == HOLDER ? "holder of " : type == ROWS ? "rows of " : type == SLOTS ? "slots of " : "viewers of ") + inventories.toString(e, debug);
	}

}
