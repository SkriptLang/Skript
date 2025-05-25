package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.BukkitUtils;
import ch.njol.skript.doc.*;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.slot.EquipmentSlot;
import ch.njol.skript.util.slot.InventorySlot;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;

@Name("Tool")
@Description("The item an entity is holding in their main or off hand.")
@Example("player's tool is tagged with minecraft tag \"pickaxes\"")
@Example("player's off hand tool is a shield")
@Example("set tool of all players to a diamond sword")
@Example("set offhand tool of target entity to a bow")
@Since("1.0")
public class ExprTool extends PropertyExpression<LivingEntity, Slot> {

	static {
		Skript.registerExpression(ExprTool.class, Slot.class, ExpressionType.PROPERTY,
			"[the] (tool|held item|weapon) [of %livingentities%]",
			"%livingentities%'[s] (tool|held item|weapon)",
			"[the] off[ ]hand (tool|item) [of %livingentities%]",
			"%livingentities%'[s] off[ ]hand (tool|item)"
		);
	}

	private boolean offHand;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		//noinspection unchecked
		setExpr((Expression<LivingEntity>) exprs[0]);
		offHand = matchedPattern >= 2;
		return true;
	}

	@Override
	protected Slot[] get(Event event, LivingEntity[] source) {
		boolean delayed = Delay.isDelayed(event);
		return get(source, entity -> {
			if (!delayed) {
				if (!offHand && event instanceof PlayerItemHeldEvent playerItemHeldEvent
					&& playerItemHeldEvent.getPlayer() == entity) {

					PlayerInventory inventory = playerItemHeldEvent.getPlayer().getInventory();
					return new InventorySlot(inventory, getTime() >= 0 ? playerItemHeldEvent.getNewSlot() : playerItemHeldEvent.getPreviousSlot());

				} else if (event instanceof PlayerBucketEvent playerBucketEvent
					&& playerBucketEvent.getPlayer() == entity) {

					PlayerInventory inventory = playerBucketEvent.getPlayer().getInventory();
					boolean isOffHand = offHand || playerBucketEvent.getHand() == org.bukkit.inventory.EquipmentSlot.OFF_HAND;
					int inventorySlot = isOffHand ? BukkitUtils.getEquipmentSlotIndex(org.bukkit.inventory.EquipmentSlot.OFF_HAND)
						: inventory.getHeldItemSlot();
					return new InventorySlot(inventory, inventorySlot) {
						@Override
						public ItemStack getItem() {
							return getTime() <= 0 ? super.getItem() : playerBucketEvent.getItemStack();
						}

						@Override
						public void setItem(final @Nullable ItemStack item) {
							if (getTime() >= 0) {
								playerBucketEvent.setItemStack(item);
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
			return new EquipmentSlot(equipment, offHand ? org.bukkit.inventory.EquipmentSlot.OFF_HAND : org.bukkit.inventory.EquipmentSlot.HAND) {
				@Override
				public String toString(@Nullable Event event, boolean debug) {
					SyntaxStringBuilder syntaxBuilder = new SyntaxStringBuilder(event, debug);
					switch (getTime()) {
						case EventValues.TIME_FUTURE -> syntaxBuilder.append("future");
						case EventValues.TIME_PAST -> syntaxBuilder.append("former");
					}
					if (offHand)
						syntaxBuilder.append("off hand");
					syntaxBuilder.append("tool of", Classes.toString(getItem()));
					return syntaxBuilder.toString();
				}
			};
		});
	}

	@Override
	public Class<Slot> getReturnType() {
		return Slot.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder syntaxBuilder = new SyntaxStringBuilder(event, debug);
		if (offHand)
			syntaxBuilder.append("off hand");
		syntaxBuilder.append("tool of", getExpr());
		return syntaxBuilder.toString();
	}

	@Override
	public boolean setTime(int time) {
		return super.setTime(time, getExpr(), PlayerItemHeldEvent.class, PlayerBucketEvent.class);
	}

}
