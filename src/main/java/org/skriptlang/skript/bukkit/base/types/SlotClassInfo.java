package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.util.slot.Slot;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ConditionPropertyHandler;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

@ApiStatus.Internal
public class SlotClassInfo extends ClassInfo<Slot> {

	public SlotClassInfo() {
		super(Slot.class, "slot");
		this.user("(inventory )?slots?")
			.name("Slot")
			.description("Represents a single slot of an <a href='#inventory'>inventory</a>. " +
					"Notable slots are the <a href='#ExprArmorSlot'>armour slots</a> and <a href='./expressions/#ExprFurnaceSlot'>furnace slots</a>. ",
				"The most important property that distinguishes a slot from an <a href='#itemstack'>item</a> is its ability to be changed, e.g. it can be set, deleted, enchanted, etc. " +
					"(Some item expressions can be changed as well, e.g. items stored in variables. " +
					"For that matter: slots are never saved to variables, only the items they represent at the time when the variable is set).",
				"Please note that <a href='#ExprTool'>tool</a> can be regarded a slot, but it can actually change it's position, i.e. doesn't represent always the same slot.")
			.usage("")
			.examples("set tool of player to dirt",
				"delete helmet of the victim",
				"set the color of the player's tool to green",
				"enchant the player's chestplate with projectile protection 5")
			.since("")
			.defaultExpression(new EventValueExpression<>(Slot.class))
			.changer(new SlotChanger())
			.parser(new SlotParser())
			.serializeAs(ItemStack.class)
			.property(Property.NAME,
				"The custom name of the item in the slot, if it has one. Can be set or reset.",
				Skript.instance(),
				new SlotNameHandler())
			.property(Property.DISPLAY_NAME,
				"The custom name of the item in the slot, if it has one. Can be set or reset.",
				Skript.instance(),
				new SlotNameHandler())
			.property(Property.AMOUNT,
				"The amount of items in the slot's stack. Can be set.",
				Skript.instance(),
				new SlotAmountHandler())
			.property(Property.IS_EMPTY,
				"Whether this slot does not contain a (non-air) item.",
				Skript.instance(),
				ConditionPropertyHandler.of(slot -> {
					ItemStack item = slot.getItem();
					return item == null || item.getType() == Material.AIR;
				}));
	}

	public static class SlotChanger implements Changer<Slot> {
		//<editor-fold desc="slot changer" defaultstate="collapsed">
		@SuppressWarnings("unchecked")
		@Override
		public Class<Object> @Nullable [] acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.RESET)
				return null;
			if (mode == ChangeMode.SET)
				return new Class[] {ItemType[].class, ItemStack[].class};
			return new Class[] {ItemType.class, ItemStack.class};
		}

		@Override
		public void change(final Slot[] slots, final Object @Nullable [] deltas, final ChangeMode mode) {
			if (mode == ChangeMode.SET) {
				if (deltas != null) {
					if (deltas.length == 1) {
						final Object delta = deltas[0];
						for (final Slot slot : slots) {
							slot.setItem(delta instanceof ItemStack ? (ItemStack) delta : ((ItemType) delta).getItem().getRandom());
						}
					} else if (deltas.length == slots.length) {
						for (int i = 0; i < slots.length; i++) {
							final Object delta = deltas[i];
							slots[i].setItem(delta instanceof ItemStack ? (ItemStack) delta : ((ItemType) delta).getItem().getRandom());
						}
					}
				}
				return;
			}
			final Object delta = deltas == null ? null : deltas[0];
			for (final Slot slot : slots) {
				switch (mode) {
					case ADD:
						assert delta != null;
						if (delta instanceof ItemStack) {
							final ItemStack i = slot.getItem();
							if (i == null || i.getType() == Material.AIR || ItemUtils.itemStacksEqual(i, (ItemStack) delta)) {
								if (i != null && i.getType() != Material.AIR) {
									i.setAmount(Math.min(i.getAmount() + ((ItemStack) delta).getAmount(), i.getMaxStackSize()));
									slot.setItem(i);
								} else {
									slot.setItem((ItemStack) delta);
								}
							}
						} else {
							slot.setItem(((ItemType) delta).getItem().addTo(slot.getItem()));
						}
						break;
					case REMOVE:
					case REMOVE_ALL:
						assert delta != null;
						if (delta instanceof ItemStack) {
							final ItemStack i = slot.getItem();
							if (i != null && ItemUtils.itemStacksEqual(i, (ItemStack) delta)) {
								final int a = mode == ChangeMode.REMOVE_ALL ? 0 : i.getAmount() - ((ItemStack) delta).getAmount();
								if (a <= 0) {
									slot.setItem(null);
								} else {
									i.setAmount(a);
									slot.setItem(i);
								}
							}
						} else {
							if (mode == ChangeMode.REMOVE)
								slot.setItem(((ItemType) delta).removeFrom(slot.getItem()));
							else
								// REMOVE_ALL
								slot.setItem(((ItemType) delta).removeAll(slot.getItem()));
						}
						break;
					case DELETE:
						slot.setItem(null);
						break;
					case RESET:
						assert false;
				}
			}
		}
		//</editor-fold>
	}

	private static class SlotNameHandler implements ExpressionPropertyHandler<Slot, String> {
		//<editor-fold desc="slot name handler" defaultstate="collapsed">
		@Override
		public String convert(Slot slot) {
			ItemStack stack = slot.getItem();
			if (stack != null && stack.hasItemMeta()) {
				ItemMeta meta = stack.getItemMeta();
				return meta.hasDisplayName() ? meta.getDisplayName() : null;
			}
			return null;
		}

		@Override
		public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
			if (mode == ChangeMode.SET)
				return new Class[] {String.class};
			return null;
		}

		@Override
		public void change(Slot named, Object @Nullable [] delta, ChangeMode mode) {
			assert mode == ChangeMode.SET;
			assert delta != null;
			String name = (String) delta[0];
			ItemStack stack = named.getItem();
			if (stack != null && !ItemUtils.isAir(stack.getType())) {
				ItemMeta meta = stack.hasItemMeta() ? stack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(stack.getType());
				meta.setDisplayName(name);
				stack.setItemMeta(meta);
				named.setItem(stack);
			}
		}

		@Override
		public @NotNull Class<String> returnType() {
			return String.class;
		}
		//</editor-fold>
	}

	private static class SlotAmountHandler implements ExpressionPropertyHandler<Slot, Number> {
		//<editor-fold desc="amount property for slots" defaultstate="collapsed">
		@Override
		public Number convert(Slot slot) {
			return slot.getAmount();
		}

		@Override
		public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
			if (mode == ChangeMode.SET)
				return new Class[] {Integer.class};
			return null;
		}

		@Override
		public void change(Slot slot, Object @Nullable [] delta, ChangeMode mode) {
			if (mode == ChangeMode.SET) {
				assert delta != null;
				slot.setAmount((Integer) delta[0]);
			}
		}

		@Override
		public @NotNull Class<Number> returnType() {
			return Number.class;
		}
		//</editor-fold>
	}

	private static class SlotParser extends Parser<Slot> {
		//<editor-fold desc="slot parser" defaultstate="collapsed">
		@Override
		public boolean canParse(final ParseContext context) {
			return false;
		}

		@Override
		public String toString(Slot o, int flags) {
			ItemStack i = o.getItem();
			if (i == null)
				return new ItemType(Material.AIR).toString(flags);
			return ItemType.toString(i, flags);
		}

		@Override
		public String toVariableNameString(Slot o) {
			return "slot:" + o.toString();
		}
		//</editor-fold>
	}

}
