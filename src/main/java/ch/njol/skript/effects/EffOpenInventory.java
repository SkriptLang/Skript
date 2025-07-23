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
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Name("Open/Close Inventory")
@Description("""
	Opens an inventory to a player. The player can then access and modify the inventory as if it was a chest that he just opened.
	Please note that currently 'show' and 'open' have the same effect, but 'show' will eventually show an unmodifiable view of the inventory in the future.
	""")
@Example("open the player's inventory for the player")
@Example("open an anvil window for all players")
@Example("show grindstone view to {_player}")
@Example("open a dropper inventory for player")
@Example("close the inventory for all players")
@Since("2.0, 2.1.1 (closing), 2.2-Fixes-V10 (anvil), 2.4 (hopper, dropper, dispenser)")
public class EffOpenInventory extends Effect {

	// TODO: Add support for Paper's MenuType (1.21.3+)

	private enum OpenableType {
		ANVIL("anvil", InventoryType.ANVIL),
		CARTOGRAPHY("cartography [table]", "cartography table", InventoryType.CARTOGRAPHY),
		CRAFTING("(crafting [table]|workbench)", "crafting table", InventoryType.CRAFTING),
		DISPENSER("dispenser", InventoryType.DISPENSER),
		DROPPER("dropper", InventoryType.DROPPER),
		ENCHANTING("enchant(ing|ment) [table]", "enchantment table", InventoryType.ENCHANTING),
		GRINDSTONE("grindstone", InventoryType.GRINDSTONE),
		HOPPER("hopper", InventoryType.HOPPER),
		LOOM("loom", InventoryType.LOOM),
		SMITHING("smithing [table]", "smithing table", InventoryType.SMITHING),
		STONECUTTER("stonecutter", InventoryType.STONECUTTER)
		;


		private final String pattern;
		private final String toString;
		private final InventoryType inventoryType;

		OpenableType(String pattern, String toString, InventoryType inventoryType) {
			this.pattern = "(open|show) [a[n]] " + pattern + "[view|window|inventory] (to|for) %players%";
			this.toString = toString;
			this.inventoryType = inventoryType;
		}

		OpenableType(String pattern, InventoryType inventoryType) {
			this(pattern, pattern, inventoryType);
		}

	}

	private static final OpenableType[] TYPES = OpenableType.values();
	
	static {
		List<String> patterns = new ArrayList<>();
		for (OpenableType type : TYPES) {
			patterns.add(type.pattern);
		}
		patterns.add("(open|show) %inventory/inventorytype% (to|for) %players%");
		patterns.add("close [the] inventory [view] (to|of|for) %players%");
		patterns.add("close %players%'[s] inventory [view]");
		Skript.registerEffect(EffOpenInventory.class, patterns.toArray(String[]::new));
	}

	private @Nullable OpenableType openableType = null;
	private @Nullable Expression<?> invObject = null;
	private Expression<Player> players;
	private boolean open = true;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern <= TYPES.length) {
			if (matchedPattern < TYPES.length) {
				openableType = TYPES[matchedPattern];
				//noinspection unchecked
				players = (Expression<Player>) exprs[0];
			} else {
				invObject = exprs[0];
				//noinspection unchecked
				players = (Expression<Player>) exprs[1];
				if (invObject instanceof Literal<?> literal && literal.getSingle() instanceof InventoryType inventoryType && !inventoryType.isCreatable()) {
					Skript.error("Cannot create an inventory of type " + Classes.toString(inventoryType));
					return false;
				}
			}
		} else {
			open = false;
			//noinspection unchecked
			players = (Expression<Player>) exprs[0];
		}
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		Consumer<Player> consumer = null;
		if (open) {
			if (openableType != null) {
				consumer = getPlayerConsumer(openableType.inventoryType);
			} else {
				assert invObject != null;
				Object object = invObject.getSingle(event);
				if (object == null)
					return;
				if (object instanceof Inventory inventory) {
					consumer = player -> player.openInventory(inventory);
				} else if (object instanceof InventoryType inventoryType) {
					consumer = getPlayerConsumer(inventoryType);
					if (consumer == null) {
						error("Cannot create an inventory of type " + Classes.toString(inventoryType));
						return;
					}
				}
			}
		} else {
			consumer = Player::closeInventory;
		}
		assert consumer != null;

		for (Player player : players.getArray(event)) {
			consumer.accept(player);
		}
	}

	/**
	 * Handles how to open {@code inventoryType} for a {@link Player}.
	 *
	 * @param inventoryType The {@link InventoryType} to open for a {@link Player}.
	 * @return {@link Consumer} for a {@link Player} or {@code null} if {@code inventoryType} is not creatable.
	 */
	@SuppressWarnings("deprecation")
	private @Nullable Consumer<Player> getPlayerConsumer(InventoryType inventoryType) {
		if (!inventoryType.isCreatable())
			return null;

		// Having 'switch' outside rather than inside the consumer reduces any re-iterating
		return switch (inventoryType) {
			case ANVIL -> player -> player.openAnvil(null, true);
			case CARTOGRAPHY -> player -> player.openCartographyTable(null, true);
			case CRAFTING -> player -> player.openWorkbench(null, true);
			case ENCHANTING -> player -> player.openEnchanting(null, true);
			case GRINDSTONE -> player -> player.openGrindstone(null, true);
			case LOOM -> player -> player.openLoom(null, true);
			case SMITHING -> player -> player.openSmithingTable(null, true);
			case STONECUTTER -> player -> player.openStonecutter(null, true);
			default -> player -> player.openInventory(Bukkit.createInventory(player, inventoryType));
		};
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (open) {
			builder.append("open");
			if (openableType != null) {
				builder.append(openableType.toString, "inventory");
			} else {
				assert invObject != null;
				builder.append(invObject);
			}
		} else {
			builder.append("close the inventory");
		}
		builder.append("for", players);
		return builder.toString();
	}
	
}
