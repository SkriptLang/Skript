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
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;

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
import ch.njol.skript.util.slot.EquipmentSlot;
import ch.njol.skript.util.slot.InventorySlot;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Tool")
@Description("The item an entity is holding in their main or off hand.")
@Examples({"player's tool is a pickaxe",
	"player's off hand tool is a shield",
	"set tool of all players to a diamond sword",
	"set offhand tool of target entity to a bow"})
@Since("1.0")
public class ExprTool extends PropertyExpression<LivingEntity, Slot> {
	static {
		Skript.registerExpression(ExprTool.class, Slot.class, ExpressionType.PROPERTY,
			"[the] ((tool|held item|weapon)|1¦(off[ ]hand (tool|item))) [of %livingentities%]",
			"%livingentities%'[s] ((tool|held item|weapon)|1¦(off[ ]hand (tool|item)))");
	}

	private boolean offHand;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		setExpr((Expression<LivingEntity>) exprs[0]);
		offHand = parser.mark == 1;
		return true;
	}

	@Override
	protected Slot[] get(final Event e, final LivingEntity[] source) {
		final boolean delayed = Delay.isDelayed(e);
		return get(source, new Getter<Slot, LivingEntity>() {
			@Override
			@Nullable
			public Slot get(final LivingEntity ent) {
				if (!delayed) {
					if (!offHand && e instanceof PlayerItemHeldEvent && ((PlayerItemHeldEvent) e).getPlayer() == ent) {
						final PlayerInventory i = ((PlayerItemHeldEvent) e).getPlayer().getInventory();
						return new InventorySlot(i, getTime() >= 0 ? ((PlayerItemHeldEvent) e).getNewSlot() : ((PlayerItemHeldEvent) e).getPreviousSlot());
					} else if (e instanceof PlayerBucketEvent && ((PlayerBucketEvent) e).getPlayer() == ent) {
						final PlayerInventory i = ((PlayerBucketEvent) e).getPlayer().getInventory();
						boolean isOffHand = ((PlayerBucketEvent) e).getHand() == org.bukkit.inventory.EquipmentSlot.OFF_HAND || offHand;
						return new InventorySlot(i, isOffHand ? EquipmentSlot.EquipSlot.OFF_HAND.slotNumber
							: ((PlayerBucketEvent) e).getPlayer().getInventory().getHeldItemSlot()) {
							@Override
							@Nullable
							public ItemStack getItem() {
								return getTime() <= 0 ? super.getItem() : ((PlayerBucketEvent) e).getItemStack();
							}

							@Override
							public void setItem(final @Nullable ItemStack item) {
								if (getTime() >= 0) {
									((PlayerBucketEvent) e).setItemStack(item);
								} else {
									super.setItem(item);
								}
							}
						};
					}
				}
				final EntityEquipment eq = ent.getEquipment();
				if (eq == null)
					return null;
				return new EquipmentSlot(eq, offHand ? EquipmentSlot.EquipSlot.OFF_HAND : EquipmentSlot.EquipSlot.TOOL) {
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
	public String toString(final @Nullable Event e, final boolean debug) {
		String hand = offHand ? "off hand" : "";
		return String.format("%s tool of %s", hand, getExpr().toString(e, debug));
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean setTime(final int time) {
		return super.setTime(time, getExpr(), PlayerItemHeldEvent.class, PlayerBucketFillEvent.class, PlayerBucketEmptyEvent.class);
	}

}
