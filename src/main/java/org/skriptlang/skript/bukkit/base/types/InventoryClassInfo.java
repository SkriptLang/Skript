package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.InventoryUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.chat.BungeeConverter;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.ContainsHandler;
import org.skriptlang.skript.lang.properties.handlers.base.ConditionPropertyHandler;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Internal
public class InventoryClassInfo extends ClassInfo<Inventory> {

	public InventoryClassInfo() {
		super(Inventory.class, "inventory");
		this.user("inventor(y|ies)")
			.name("Inventory")
			.description("An inventory of a <a href='#player'>player</a> or <a href='#block'>block</a>. " +
					"Inventories have many effects and conditions regarding the items contained.",
				"An inventory has a fixed amount of <a href='#slot'>slots</a> which represent a specific place in the inventory, " +
					"e.g. the <a href='#ExprArmorSlot'>helmet slot</a> for players " +
					"(Please note that slot support is still very limited but will be improved eventually).")
			.usage("")
			.examples("")
			.since("1.0")
			.defaultExpression(new EventValueExpression<>(Inventory.class))
			.parser(new InventoryParser())
			.changer(new InventoryChanger())
			.property(Property.CONTAINS,
				"Inventories can contain items.",
				Skript.instance(),
				new InventoryContainsHandler())
			.property(Property.NAME,
				"The name of the inventory. Can be set or reset.",
				Skript.instance(),
				new InventoryNameHandler())
			.property(Property.DISPLAY_NAME,
				"The name of the inventory. Can be set or reset.",
				Skript.instance(),
				new InventoryNameHandler())
			.property(Property.IS_EMPTY,
				"Whether the inventory contains no items (all slots contain air).",
				Skript.instance(),
				ConditionPropertyHandler.of(inventory -> {
					for (ItemStack s : inventory.getContents()) {
						if (s != null && s.getType() != Material.AIR)
							return false; // There is an item here!
					}
					return true;
				}));
	}

	private static class InventoryParser extends Parser<Inventory> {
		//<editor-fold desc="inventory parser" defaultstate="collapsed">
		@Override
		public @Nullable Inventory parse(final String s, final ParseContext context) {
			return null;
		}

		@Override
		public boolean canParse(final ParseContext context) {
			return false;
		}

		@Override
		public String toString(final Inventory i, final int flags) {
			return "inventory of " + Classes.toString(i.getHolder());
		}

		@Override
		public String getDebugMessage(final Inventory i) {
			return "inventory of " + Classes.getDebugMessage(i.getHolder());
		}

		@Override
		public String toVariableNameString(final Inventory i) {
			return "inventory of " + Classes.toString(i.getHolder(), StringMode.VARIABLE_NAME);
		}
		//</editor-fold>
	}

	public static class InventoryChanger implements Changer<Inventory> {
		//<editor-fold desc="inventory changer" defaultstate="collapsed">

		private final Material[] cachedMaterials = Material.values();

		@Override
		public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
			if (mode == ChangeMode.RESET)
				return null;
			if (mode == ChangeMode.REMOVE_ALL)
				return CollectionUtils.array(ItemType[].class);
			if (mode == ChangeMode.SET)
				return CollectionUtils.array(ItemType[].class, Inventory.class);
			return CollectionUtils.array(ItemType[].class, Inventory[].class);
		}

		@Override
		public void change(Inventory[] inventories, Object @Nullable [] delta, ChangeMode mode) {
			for (Inventory inventory : inventories) {
				assert inventory != null;
				switch (mode) {
					case DELETE:
						inventory.clear();
						break;
					case SET:
						inventory.clear();
						//$FALL-THROUGH$
					case ADD:
						assert delta != null;

						if (delta instanceof ItemStack[] items) { // Old behavior - legacy code (is it used? no idea)
							if (items.length > 36) {
								return;
							}
							for (Object d : delta) {
								if (d instanceof Inventory itemStacks) {
									for (ItemStack itemStack : itemStacks) {
										if (itemStack != null)
											inventory.addItem(itemStack);
									}
								} else {
									((ItemType) d).addTo(inventory);
								}
							}
						} else {
							for (Object d : delta) {
								if (d instanceof ItemStack stack) {
									new ItemType(stack).addTo(inventory); // Can't imagine why would be ItemStack, but just in case...
								} else if (d instanceof ItemType itemType) {
									itemType.addTo(inventory);
								} else if (d instanceof Block block) {
									new ItemType(block).addTo(inventory);
								} else {
									Skript.error("Can't " + d.toString() + " to an inventory!");
								}
							}
						}

						break;
					case REMOVE:
					case REMOVE_ALL:
						assert delta != null;
						if (delta.length == cachedMaterials.length) {
							// Potential fast path: remove all items -> clear inventory
							boolean equal = true;
							for (int i = 0; i < delta.length; i++) {
								if (!(delta[i] instanceof ItemType itemType)) {
									equal = false;
									break; // Not an item, take slow path
								}
								if (itemType.getMaterial() != cachedMaterials[i]) {
									equal = false;
									break;
								}
							}
							if (equal) { // Take fast path, break out before slow one
								inventory.clear();
								break;
							}
						}

						// Slow path
						for (Object d : delta) {
							if (d instanceof Inventory itemStacks) {
								assert mode == ChangeMode.REMOVE;
								for (ItemStack itemStack : itemStacks) {
									if (itemStack != null)
										inventory.removeItem(itemStack);
								}
							} else {
								if (mode == ChangeMode.REMOVE)
									((ItemType) d).removeFrom(inventory);
								else
									((ItemType) d).removeAll(inventory);
							}
						}
						break;
					case RESET:
						assert false;
				}
				InventoryHolder holder = inventory.getHolder();
				if (holder instanceof Player player) {
					player.updateInventory();
				}
			}
		}
		//</editor-fold>
	}

	private static class InventoryContainsHandler implements ContainsHandler<Inventory, Object> {
		//<editor-fold desc="inventory contains property" defaultstate="collapsed">
		@Override
		public boolean contains(Inventory container, Object element) {
			if (element instanceof ItemType type) {
				return type.isContainedIn(container);
			} else if (element instanceof ItemStack stack) {
				return container.containsAtLeast(stack, stack.getAmount());
			}
			return false;
		}

		@Override
		public Class<?>[] elementTypes() {
			return new Class[]{ItemType.class, ItemStack.class};
		}
		//</editor-fold>
	}

	private static class InventoryNameHandler implements ExpressionPropertyHandler<Inventory, String> {
		//<editor-fold desc="inventory name property" defaultstate="collapsed">
		private static @Nullable BungeeComponentSerializer serializer = null;

		static {
			// Check for Adventure API
			if (Skript.classExists("net.kyori.adventure.text.Component") &&
				Skript.methodExists(Bukkit.class, "createInventory", InventoryHolder.class, int.class, Component.class))
				serializer = BungeeComponentSerializer.get();
		}

		@Override
		public String convert(Inventory inventory) {
			if (inventory.getViewers().isEmpty())
				return null;
			return InventoryUtils.getTitle(inventory.getViewers().get(0).getOpenInventory());
		}

		@Override
		public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
			if (mode == ChangeMode.SET || mode == ChangeMode.RESET)
				return new Class[]{String.class};
			return null;
		}

		@Override
		@SuppressWarnings("deprecation")
		public void change(Inventory inventory, Object @Nullable [] delta, ChangeMode mode) {

			String name = (delta == null || delta.length == 0) ? null : (String) delta[0];

			if (inventory.getViewers().isEmpty())
				return;
			// Create a clone to avoid a ConcurrentModificationException
			List<HumanEntity> viewers = new ArrayList<>(inventory.getViewers());

			InventoryType type = inventory.getType();
			if (!type.isCreatable())
				return;

			Inventory copy;
			if (serializer == null) {
				if (name == null)
					name = type.getDefaultTitle();
				if (type == InventoryType.CHEST) {
					copy = Bukkit.createInventory(inventory.getHolder(), inventory.getSize(), name);
				} else {
					copy = Bukkit.createInventory(inventory.getHolder(), type, name);
				}
			} else {
				Component component = type.defaultTitle();
				if (name != null) {
					BaseComponent[] components = BungeeConverter.convert(ChatMessages.parseToArray(name));
					component = serializer.deserialize(components);
				}
				if (type == InventoryType.CHEST) {
					copy = Bukkit.createInventory(inventory.getHolder(), inventory.getSize(), component);
				} else {
					copy = Bukkit.createInventory(inventory.getHolder(), type, component);
				}
			}
			copy.setContents(inventory.getContents());
			viewers.forEach(viewer -> viewer.openInventory(copy));
		}

		@Override
		public @NotNull Class<String> returnType() {
			return String.class;
		}
		//</editor-fold>
	}

}
