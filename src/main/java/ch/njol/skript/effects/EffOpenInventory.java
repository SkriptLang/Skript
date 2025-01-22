package ch.njol.skript.sections;

import java.util.List;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import ch.njol.skript.util.Version;

@Name("Open/Close Inventory")
@Description({
	"Opens an inventory to a player. The player can then access and modify the inventory as if it was a chest that they just opened.",
	"Note that 'show' and 'open' have different effects, 'show' will show just a view of the inventory.",
	"Whereas 'open' will attempt to make an inventory real and usable. Like a workbench allowing recipes to work.",
	"When using as a section. The section allows to modification via the event-inventory."
})
@Examples({
	"show crafting table to player #unmodifiable, use open instead to allow for recipes to work",
	"open a crafting table to the player",
	"open a loom to the player",
	"open the player's inventory for the player",
	"",
	"show chest inventory to player:",
	"\tset slot 1 of event-inventory to stone named \"example\"",
	"\topen event-inventory to all players"
})
@Since("2.0, 2.1.1 (closing), 2.2-Fixes-V10 (anvil), INSERT VERSION (enchanting, cartography, grindstone, loom) & section support")
public class EffOpenInventory extends EffectSection {

	private static enum OpenableInventorySyntax {

		ANVIL("anvil"),
		CARTOGRAPHY("cartography [table]", Skript.methodExists(HumanEntity.class, "openCartographyTable", Location.class, boolean.class),
				"Opening a cartography table inventory requires PaperSpigot."),
		ENCHANTING("enchant(ment|ing) [table]", new Version(1, 14)),
		GRINDSTONE("grindstone", Skript.methodExists(HumanEntity.class, "openGrindstone", Location.class, boolean.class),
				"Opening a grindstone inventory requires PaperSpigot."),
		LOOM("loom", Skript.methodExists(HumanEntity.class, "openLoom", Location.class, boolean.class),
				"Opening a loom inventory requires PaperSpigot."),
		SMITHING("smithing [table]", Skript.methodExists(HumanEntity.class, "openSmithingTable", Location.class, boolean.class),
				"Opening a smithing table inventory requires PaperSpigot."),
		STONECUTTER("stone[ ]cutter", Skript.methodExists(HumanEntity.class, "openSmithingTable", Location.class, boolean.class),
				"Opening a stone cutter inventory requires PaperSpigot."),
		WORKBENCH("(crafting [table]|workbench)");

		private @Nullable String methodError;
		private @Nullable Version version;
		private final String property;
		private boolean methodExists = true;

		OpenableInventorySyntax(String property) {
			this.property = property;
		}

		OpenableInventorySyntax(String property, Version version) {
			this.property = property;
			this.version = version;
		}

		OpenableInventorySyntax(String property, boolean methodExists, String methodError) {
			this.methodExists = methodExists;
			this.methodError = methodError;
			this.property = property;
		}

		private String getFormatted() {
			return this.toString().toLowerCase(Locale.ENGLISH) + ":" + property;
		}

		@Nullable
		private Version getVersion() {
			return version;
		}

		private boolean doesMethodExist() {
			return methodExists;
		}

		@Nullable
		private String getMethodError() {
			return methodError;
		}

		private static String construct() {
			StringBuilder builder = new StringBuilder("(");
			OpenableInventorySyntax[] values = OpenableInventorySyntax.values();
			for (int i = 0; i < values.length; i++ ) {
				builder.append(values[i].getFormatted());
				if (i + 1 < values.length)
					builder.append("|");
			}
			return builder.append("|%-inventory%)").toString();
		}
	}

	public static class InventorySectionEvent extends Event {

		private final Inventory inventory;

		public InventorySectionEvent(Inventory inventory) {
			this.inventory = inventory;
		}

		public Inventory getInventory() {
			return inventory;
		}

		@Override
		public HandlerList getHandlers() {
			throw new IllegalStateException();
		}

	}

	static {
		EventValues.registerEventValue(InventorySectionEvent.class, Inventory.class, InventorySectionEvent::getInventory);
		Skript.registerSection(EffSecOpenInventory.class,
				"[show|create] %inventory/inventorytype% (to|for) %players%",
				"open [a[n]] " + OpenableInventorySyntax.construct() + " [view|window|inventory] (to|for) %players%",

				"close [the] inventory [view] (of|for) %players%",
				"close %players%'[s] inventory [view]");
	}

	private @Nullable OpenableInventorySyntax syntax;
	private @Nullable Expression<?> object;
	private @Nullable Trigger trigger;

	private Expression<Player> players;
	private boolean open;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult,
			SectionNode sectionNode, List<TriggerItem> triggerItems) {

		object = exprs.length > 1 ? exprs[0] : null;
		if (matchedPattern == 1) {
			open = true;
			if (!parseResult.tags.isEmpty()) { // %-inventory% was not used
				syntax = OpenableInventorySyntax.valueOf(parseResult.tags.get(0).toUpperCase(Locale.ENGLISH));
				if (syntax.getVersion() != null && !Skript.isRunningMinecraft(syntax.getVersion())) {
					Skript.error("Opening an inventory of type '" + syntax.toString().toLowerCase(Locale.ENGLISH) + "' is only present on Minecraft version " + syntax.getVersion());
					return false;
				}
				if (!syntax.doesMethodExist()) {
					Skript.error(syntax.getMethodError());
					return false;
				}
			}
		}

		players = (Expression<Player>) exprs[exprs.length - 1];
		if (object instanceof Literal && object != null) {
			Literal<?> literal = (Literal<?>) object;
			Object object = literal.getSingle();
			if (object instanceof InventoryType && !((InventoryType) object).isCreatable()) {
				Skript.error("You can't open a '" + literal.toString() + "' inventory to players. It's not creatable.");
				return false;
			}
		}

		if (exprs[0] instanceof Literal<?> lit && lit.getSingle() instanceof InventoryType inventoryType && !inventoryType.isCreatable()) {
			Skript.error("Cannot create an inventory of type " + Classes.toString(inventoryType));
			return false;
		}

		if (open && hasSection())
			trigger = loadCode(sectionNode, "open inventory", InventorySectionEvent.class);

		return true;
	}

	@Override
	protected TriggerItem walk(Event event) {
		if (object != null) {
			Inventory inventory = null;
			Object o = object.getSingle(event);
			if (o instanceof Inventory i) {
				inventory = i;
			} else if (o instanceof InventoryType inventoryType && inventoryType.isCreatable()) {
				inventory = createInventory(inventoryType);
			}
			if (inventory == null)
				return super.walk(event, false);

			if (trigger != null) {
				InventorySectionEvent inventoryEvent = new InventorySectionEvent(inventory);
				Object localVars = Variables.copyLocalVariables(event);
				Variables.setLocalVariables(inventoryEvent, localVars);
				TriggerItem.walk(trigger, inventoryEvent);
				Variables.setLocalVariables(event, Variables.copyLocalVariables(inventoryEvent));
				Variables.removeLocals(inventoryEvent);
			}

			for (Player player : players.getArray(event))
				player.openInventory(inventory);

		} else {
			for (Player player : players.getArray(event)) {
				if (!open) {
					player.closeInventory();
					continue;
				}
				switch (syntax) {
					case ANVIL:
						player.openAnvil(null, true);
						break;
					case CARTOGRAPHY:
						player.openCartographyTable(null, true);
						break;
					case ENCHANTING:
						player.openEnchanting(null, true);
						break;
					case GRINDSTONE:
						player.openGrindstone(null, true);
						break;
					case LOOM:
						player.openLoom(null, true);
						break;
					case SMITHING:
						player.openSmithingTable(null, true);
						break;
					case STONECUTTER:
						player.openStonecutter(null, true);
						break;
					case WORKBENCH:
						player.openWorkbench(null, true);
						break;
				}
			}
		}
		return super.walk(event, false);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (object != null)
			return "show " + object.toString(event, debug) + " to " + players.toString(event, debug);
		if (open)
			return "open " + syntax.name().toLowerCase(Locale.ENGLISH) + " to " + players.toString(event, debug);
		return "close inventory of " + players.toString(event, debug);
	}

	public static @Nullable Inventory createInventory(InventoryType type) {
		if (!type.isCreatable())
			return null;
		try {
			return Bukkit.createInventory(null, type);
		} catch (NullPointerException | IllegalArgumentException e) {
			// Spigot forgot to label some InventoryType's as non creatable in some versions < 1.19.4
			// So this throws NullPointerException aswell ontop of the IllegalArgumentException.
			// See https://hub.spigotmc.org/jira/browse/SPIGOT-7301
			Skript.error("You can't open a '" + Classes.toString(type) + "' inventory to players. It's not creatable.");
		}
		return null;
	}

}
