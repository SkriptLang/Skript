package ch.njol.skript.sections;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.EffOpenInventory;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;

@Name("Open/Close Inventory Section")
@Description({
	"Opens an inventory to a player.",
	"The section then allows to modify the event-inventory."
})
@Examples({
	"show chest inventory to player:",
		"\tset slot 1 of event-inventory to stone named \"example\"",
		"\topen event-inventory to all players"
})
@Since("INSERT VERSION")
public class SecOpenInventory extends Section {

	public static class InventorySectionEvent extends Event {
		private final Inventory inventory;

		public InventorySectionEvent(Inventory inventory) {
			this.inventory = inventory;
		}

		public Inventory getInventory() {
			return inventory;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	static {
		Skript.registerSection(SecOpenInventory.class, "[show|open|create] %inventory/inventorytype%  [to %players%]");
	}

	private @Nullable Expression<?> inventory;

	private @Nullable Expression<Player> players;

	private @Nullable Trigger trigger;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult,
			SectionNode sectionNode, List<TriggerItem> triggerItems) {

		inventory = exprs[0];
		players = (Expression<Player>) exprs[1];
		if (inventory instanceof Literal) {
			Literal<?> literal = (Literal<?>) inventory;
			Object object = literal.getSingle();
			if (object instanceof InventoryType && !((InventoryType) object).isCreatable()) {
				Skript.error("You can't open a '" + literal.toString() + "' inventory to players. It's not creatable.");
				return false;
			}
		}
		trigger = loadCode(sectionNode, "open inventory", InventorySectionEvent.class);
		return true;
	}

	@Override
	protected TriggerItem walk(Event event) {
		Inventory inventory = null;
		Object object = this.inventory.getSingle(event);
		if (object == null)
			return super.walk(event, false);
		if (object instanceof Inventory) {
			inventory = (Inventory) object;
		} else if (object instanceof InventoryType) {
			inventory = EffOpenInventory.createInventory((InventoryType) object);
		} else {
			assert false;
		}
		if (inventory == null)
			return super.walk(event, false);
		InventorySectionEvent inventoryEvent = new InventorySectionEvent(inventory);
		Object localVars = Variables.copyLocalVariables(event);
		Variables.setLocalVariables(inventoryEvent, localVars);
		TriggerItem.walk(trigger, inventoryEvent);
		Variables.setLocalVariables(event, Variables.copyLocalVariables(inventoryEvent));
		Variables.removeLocals(inventoryEvent);
		if (players != null)
			for (Player player : players.getArray(event))
				player.openInventory(inventory);
		return super.walk(event, false);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "open " + inventory.toString(event, debug) + " to " + players.toString(event, debug);
	}

}
