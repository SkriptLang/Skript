/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Getter;
import ch.njol.skript.util.slot.CursorSlot;
import ch.njol.skript.util.slot.EquipmentSlot;
import ch.njol.skript.util.slot.InventorySlot;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;

@Name("Tool/Offhand")
@Description("The item an entity is holding in their main or off hand.")
@Examples({
	"player's tool is a pickaxe",
	"player's off hand tool is a shield",
	"set tool of all players to a diamond sword",
	"set offhand tool of target entity to a bow"
})
@Since("1.0, 2.2-dev37 (offhand)")
public class ExprTool extends PropertyExpression<LivingEntity, Slot> {

	static {
		Skript.registerExpression(ExprTool.class, Slot.class, ExpressionType.PROPERTY,
				"[the] ((tool|held item|weapon)|offhand:(off[ ]hand (tool|item))) [of %livingentities%]",
				"%livingentities%'[s] ((tool|held item|weapon)|1¦(off[ ]hand (tool|item)))");
	}

	private boolean offHand;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<LivingEntity>) exprs[0]);
		offHand = parseResult.hasTag("offhand");
		return true;
	}

	@Override
	protected Slot[] get(Event event, LivingEntity[] source) {
		boolean delayed = Delay.isDelayed(event);
		return get(source, new Getter<Slot, LivingEntity>() {
			@Override
			@Nullable
			public Slot get(LivingEntity entity) {
				if (!delayed) {
					if (offHand && event instanceof InventoryClickEvent && ((InventoryClickEvent) event).getWhoClicked().equals(entity) && getTime() == 1) {
						// When a player uses a number key to swap an item from hotbar to offhand. This simplifies the process with future states.
						if ((((InventoryClickEvent) event).getClick() == ClickType.NUMBER_KEY && ((InventoryClickEvent) event).getSlot() == EquipmentSlot.EquipSlot.OFF_HAND.slotNumber)) {
							PlayerInventory inventory = ((InventoryClickEvent) event).getWhoClicked().getInventory();
							return new InventorySlot(inventory, ((InventoryClickEvent) event).getHotbarButton());
						} else if (((InventoryClickEvent) event).getClick() == ClickType.SWAP_OFFHAND) {
							PlayerInventory inventory = ((InventoryClickEvent) event).getWhoClicked().getInventory();
							return new InventorySlot(inventory, ((InventoryClickEvent) event).getSlot());
						} else if (((InventoryClickEvent) event).getSlot() == EquipmentSlot.EquipSlot.OFF_HAND.slotNumber) {
							switch (((InventoryClickEvent) event).getAction()) {
								case NOTHING: // When you double click to collect to cursor, it's not COLLECT_TO_CURSOR...
								case PICKUP_ALL:
								case PICKUP_HALF:
								case PICKUP_ONE:
								case PICKUP_SOME:
									return new InventorySlot(((InventoryClickEvent) event).getClickedInventory(), ((InventoryClickEvent) event).getRawSlot());
								case PLACE_ALL:
								case PLACE_ONE:
								case PLACE_SOME:
								case SWAP_WITH_CURSOR:
									return new CursorSlot((Player) ((InventoryClickEvent) event).getWhoClicked());
								default:
									break;
							}
						}
						return null;
					} else if (!offHand && event instanceof PlayerItemHeldEvent && ((PlayerItemHeldEvent) event).getPlayer() == entity) {
						PlayerInventory inventory = ((PlayerItemHeldEvent) event).getPlayer().getInventory();
						return new InventorySlot(inventory, getTime() >= 0 ? ((PlayerItemHeldEvent) event).getNewSlot() : ((PlayerItemHeldEvent) event).getPreviousSlot());
					} else if (event instanceof PlayerBucketEvent && ((PlayerBucketEvent) event).getPlayer() == entity) {
						PlayerInventory inventory = ((PlayerBucketEvent) event).getPlayer().getInventory();
						boolean isOffHand = ((PlayerBucketEvent) event).getHand() == org.bukkit.inventory.EquipmentSlot.OFF_HAND || offHand;
						return new InventorySlot(inventory, isOffHand ? EquipmentSlot.EquipSlot.OFF_HAND.slotNumber
							: ((PlayerBucketEvent) event).getPlayer().getInventory().getHeldItemSlot()) {
							@Override
							@Nullable
							public ItemStack getItem() {
								return getTime() <= 0 ? super.getItem() : ((PlayerBucketEvent) event).getItemStack();
							}

							@Override
							public void setItem(@Nullable ItemStack item) {
								if (getTime() >= 0) {
									((PlayerBucketEvent) event).setItemStack(item);
								} else {
									super.setItem(item);
								}
							}
						};
					}
				}
				EntityEquipment equipment = entity.getEquipment();
				if (equipment == null)
					return null;
				return new EquipmentSlot(equipment, offHand ? EquipmentSlot.EquipSlot.OFF_HAND : EquipmentSlot.EquipSlot.TOOL) {
					@Override
					public String toString(@Nullable Event event, boolean debug) {
						String time = getTime() == 1 ? "future " : getTime() == -1 ? "former " : "";
						String hand = offHand ? "off hand" : "";
						String item = Classes.toString(getItem());
						return String.format("%s %s tool of %s", time, hand, item);
					}
				};
			}
		});
	}

	@Override
	public Class<Slot> getReturnType() {
		return Slot.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String hand = offHand ? "off hand" : "";
		return String.format("%s tool of %s", hand, getExpr().toString(event, debug));
	}

	@Override
	public boolean setTime(int time) {
		return super.setTime(time, PlayerItemHeldEvent.class, PlayerBucketFillEvent.class, PlayerBucketEmptyEvent.class, InventoryClickEvent.class);
	}

}
