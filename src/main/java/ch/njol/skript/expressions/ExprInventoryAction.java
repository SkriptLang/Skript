package ch.njol.skript.expressions;

import org.bukkit.event.inventory.InventoryAction;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;

@Name("Inventory Action")
@Description("The <a href='./classes.html#inventoryaction'>inventory action</a> of an inventory event. Please click on the link for more information.")
@Examples("inventory action is pickup all")
@Since("2.2-dev16")
public class ExprInventoryAction extends EventValueExpression<InventoryAction> {

	static {
		register(ExprInventoryAction.class, InventoryAction.class, "inventory action");
	}

	public ExprInventoryAction() {
		super(InventoryAction.class);
	}

}
