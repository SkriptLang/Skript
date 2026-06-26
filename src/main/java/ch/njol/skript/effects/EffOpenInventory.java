package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

@Name("Open/Close Inventory")
@Description({"Opens an inventory to a player. The player can then access and modify the inventory as if it was a chest that they just opened.",
		"Please note that currently 'show' and 'open' have the same effect, but 'show' will eventually show an unmodifiable view of the inventory in the future."})
@Example("show the victim's inventory to the player")
@Example("open the player's inventory for the player")
@Since("2.0, 2.1.1 (closing), 2.2-Fixes-V10 (anvil), 2.4 (hopper, dropper, dispenser)")
public class EffOpenInventory extends Effect {

	private static final Patterns<InventoryType> PATTERNS = new Patterns<>(new Object[][]{
		{"close %players%'[s] inventory [view]", null},
		{"close [the] inventory [view] (to|of|for) %players%", null},
		{"open %inventory/inventorytype% (to|for) %players%", null},
		{"open [a] (crafting table|workbench) (to|for) %players%", InventoryType.WORKBENCH},
		{"open [a] chest (to|for) %players%", InventoryType.CHEST},
		{"open [a[n]] anvil (to|for) %players%", InventoryType.ANVIL},
		{"open [a] hopper (to|for) %players%", InventoryType.HOPPER},
		{"open [a] dropper (to|for) %players%", InventoryType.DROPPER},
		{"open [a] dispenser (to|for) %players%", InventoryType.DISPENSER}
	});

	static {
		Skript.registerEffect(EffOpenInventory.class, PATTERNS.getPatterns());
	}

	private boolean open;
	private @Nullable InventoryType inventoryType = null;
	private @Nullable Expression<?> inventoryExpr = null;

	private Expression<Player> players;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {

		open = matchedPattern > 1;
		if (open) {
			if (matchedPattern == 2) {
				inventoryExpr = exprs[0];
			} else {
				inventoryType = PATTERNS.getInfo(matchedPattern);
			}
		}

		players = (Expression<Player>) exprs[exprs.length - 1];

		if (exprs[0] instanceof Literal<?> lit && lit.getSingle() instanceof InventoryType type && !type.isCreatable()) {
			Skript.error("Cannot create an inventory of type " + Classes.toString(type));
			return false;
		}
		return true;
	}

	@Override
	protected void execute(final Event event) {
		if (inventoryExpr != null) {
			Object object = inventoryExpr.getSingle(event);
			openForPlayers(event, object);
		} else {
			if (open) {
				openForPlayers(event, inventoryType);
			} else {
				for (Player player : players.getArray(event)) {
					player.closeInventory();
				}
			}
		}
	}

	private void openForPlayers(Event event, Object target) {
		if (target == null)
			return;

		Player[] targetPlayers = this.players.getArray(event);

		if (target instanceof Inventory inventory) {
			for (Player player : targetPlayers) {
				player.openInventory(inventory);
			}
		} else if (target instanceof InventoryType type && type.isCreatable()) {
			for (Player player : targetPlayers) {
				openInventoryType(player, type);
			}
		}
	}

	@SuppressWarnings("UnstableApiUsage")
	private void openInventoryType(Player player, InventoryType type) {
		if (type.getMenuType() != null) {
			player.openInventory(type.getMenuType().create(player, null));
			return;
		}

		player.openInventory(Bukkit.createInventory(null, type));
	}

	@Override
	public String toString(final @Nullable Event event, final boolean debug) {
		if (!open) {
			return "close inventory view of " + players.toString(event, debug);
		}

		String openedThing;
		if (inventoryExpr != null) {
			openedThing = inventoryExpr.toString(event, debug);
		} else if (inventoryType != null) {
			openedThing = inventoryType.name().toLowerCase().replace('_', ' ');
		} else {
			openedThing = "inventory";
		}

		return "open " + openedThing + " to " + players.toString(event, debug);
	}
}
