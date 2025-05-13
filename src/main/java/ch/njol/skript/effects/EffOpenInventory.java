package ch.njol.skript.effects;

import java.util.Locale;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.registrations.Classes;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Open/Close Inventory")
@Description({"Opens an inventory to a player. The player can then access and modify the inventory as if it was a chest that he just opened.",
		"Please note that currently 'show' and 'open' have the same effect, but 'show' will eventually show an unmodifiable view of the inventory in the future."})
@Examples({"show the victim's inventory to the player",
		"open the player's inventory for the player"})
@Since("2.0, 2.1.1 (closing), 2.2-Fixes-V10 (anvil), 2.4 (hopper, dropper, dispenser")
public class EffOpenInventory extends Effect {
	
	private final static int WORKBENCH = 0, CHEST = 1, ANVIL = 2, HOPPER = 3, DROPPER = 4, DISPENSER = 5;
	
	static {
		Skript.registerEffect(EffOpenInventory.class,
				"(open|show) ((0¦(crafting [table]|workbench)|1¦chest|2¦anvil|3¦hopper|4¦dropper|5¦dispenser) (view|window|inventory|)|%-inventory/inventorytype%) (to|for) %players%",
				"close [the] inventory [view] (to|of|for) %players%", "close %players%'[s] inventory [view]");
	}
	
	@Nullable
	private Expression<?> invi;
	
	boolean open;
	private int invType;
	
	@SuppressWarnings("null")
	private Expression<Player> players;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		int openFlag = 0;
		if(parseResult.mark >= 5) {
			openFlag = parseResult.mark ^ 5;
			invType = DISPENSER;
		} else if(parseResult.mark >= 4) {
			openFlag = parseResult.mark ^ 4;
			invType = DROPPER;
		} else if(parseResult.mark >= 3) {
			openFlag = parseResult.mark ^ 3;
			invType = HOPPER;
		} else if (parseResult.mark >= 2) {
			openFlag = parseResult.mark ^ 2;
			invType = ANVIL;
		} else if (parseResult.mark >= 1) {
			openFlag = parseResult.mark ^ 1;
			invType = CHEST;
		} else if (parseResult.mark >= 0) {
			invType = WORKBENCH;
			openFlag = parseResult.mark ^ 0;
		} else {
			openFlag = parseResult.mark;
		}
		
		open = matchedPattern == 0;
		invi = open ? exprs[0] : null;
		players = (Expression<Player>) exprs[exprs.length - 1];
		if (openFlag == 1 && invi != null) {
			Skript.warning("Using 'show' inventory instead of 'open' is not recommended as it will eventually show an unmodifiable view of the inventory in the future.");
		}
		if (exprs[0] instanceof Literal<?> lit && lit.getSingle() instanceof InventoryType inventoryType && !inventoryType.isCreatable()) {
			Skript.error("Cannot create an inventory of type " + Classes.toString(inventoryType));
			return false;
		}
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
		if (invi != null) {
			Inventory i;
			
			assert invi != null;
			Object o = invi.getSingle(e);
			if (o instanceof Inventory) {
				i = (Inventory) o;
			} else if (o instanceof InventoryType inventoryType && inventoryType.isCreatable()) {
				i = Bukkit.createInventory(null, inventoryType);
			} else {
				return;
			}
			
			if (i == null)
				return;
			for (final Player p : players.getArray(e)) {
				try {
					p.openInventory(i);
				} catch (IllegalArgumentException ex){
					Skript.error("You can't open a " + i.getType().name().toLowerCase(Locale.ENGLISH).replaceAll("_", "") + " inventory to a player.");
				}
			}
		} else {
			for (final Player p : players.getArray(e)) {
				if (open) {
					switch (invType) {
						case WORKBENCH:
							p.openWorkbench(null, true);
							break;
						case CHEST:
							p.openInventory(Bukkit.createInventory(p, InventoryType.CHEST));
							break;
						case ANVIL:
							p.openInventory(Bukkit.createInventory(p, InventoryType.ANVIL));
							break;
						case HOPPER:
							p.openInventory(Bukkit.createInventory(p, InventoryType.HOPPER));
							break;
						case DROPPER:
							p.openInventory(Bukkit.createInventory(p, InventoryType.DROPPER));
							break;
						case DISPENSER:
							p.openInventory(Bukkit.createInventory(p, InventoryType.DISPENSER));
					
					}
				} else
					p.closeInventory();
			}
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return (open ? "open " + (invi != null ? invi.toString(e, debug) : "crafting table") + " to " : "close inventory view of ") + players.toString(e, debug);
	}
	
}
