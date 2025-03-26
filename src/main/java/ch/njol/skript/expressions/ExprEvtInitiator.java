package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

@Name("Initiator Inventory")
@Description("Returns the initiator inventory in an on <a href=\"./events.html?search=#inventory_item_move\">inventory item move</a> event.")
@Examples({
	"on inventory item move:",
		"\tholder of event-initiator-inventory is a chest",
		"\tbroadcast \"Item transport happening at %location at holder of event-initiator-inventory%!\""
})
@Events("Inventory Item Move")
@Since("2.8.0")
public class ExprEvtInitiator extends SimpleExpression<Inventory> {

	static {
		Skript.registerExpression(ExprEvtInitiator.class, Inventory.class, ExpressionType.SIMPLE, "[the] [event-]initiator[( |-)inventory]");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(InventoryMoveItemEvent.class)) {
			Skript.error("'event-initiator' can only be used in an 'inventory item move' event.");
			return false;
		}
		return true;
	}

	@Override
	protected Inventory[] get(Event event) {
		if (!(event instanceof InventoryMoveItemEvent))
			return new Inventory[0];
		return CollectionUtils.array(((InventoryMoveItemEvent) event).getInitiator());
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Inventory> getReturnType() {
		return Inventory.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "event-initiator-inventory";
	}

}
