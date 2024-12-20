package org.skriptlang.skript.bukkit.equippablecomponents.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.slot.EquipmentSlot.EquipSlot;
import ch.njol.skript.util.slot.EquipmentSlot;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.jetbrains.annotations.Nullable;

@Name("Equippaable Component - Equipment Slot")
@Description("The equipment slot an item can be equipped to.")
@Examples({
	"set the equipment slot of {_item} to chest slot",
	"",
	"set {_component} to the equippable component of {_item}",
	"set the equipment slot of {_component} to boots slot",
	"set the equippable component of {_item} to {_component}"
})
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class ExprEquipCompSlot extends PropertyExpression<Object, EquipSlot> {

	static {
		Skript.registerExpression(ExprEquipCompSlot.class,  EquipSlot.class, ExpressionType.PROPERTY,
			"[the] equipment slot of %itemstacks/itemtypes/slots/equippablecomponents%"
		);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(exprs[0]);
		return true;
	}

	@Override
	protected EquipSlot[] get(Event event, Object[] source) {
		return get(source, object -> {
			if (object instanceof EquippableComponent component)
				return EquipmentSlot.convertToSkriptEquipSlot(component.getSlot());

			ItemStack itemStack = ItemUtils.asItemStack(object);
			if (itemStack == null)
				return null;
			org.bukkit.inventory.EquipmentSlot bukkitSlot = itemStack.getItemMeta().getEquippable().getSlot();
			return EquipmentSlot.convertToSkriptEquipSlot(bukkitSlot);
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(EquipSlot.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (delta == null)
			return;
		EquipSlot providedSlot = (EquipSlot) delta[0];
		if (providedSlot == null)
			return;
		org.bukkit.inventory.EquipmentSlot bukkitSlot = providedSlot.getBukkitEquipSlot();
		for (Object object : getExpr().getArray(event)) {
			if (object instanceof EquippableComponent component) {
				component.setSlot(bukkitSlot);
			} else {
				ItemStack itemStack = ItemUtils.asItemStack(object);
				if (itemStack == null)
					continue;
				ItemMeta meta = itemStack.getItemMeta();
				EquippableComponent component = meta.getEquippable();
				component.setSlot(bukkitSlot);
				meta.setEquippable(component);
				itemStack.setItemMeta(meta);
				if (object instanceof Slot slot) {
					slot.setItem(itemStack);
				} else if (object instanceof ItemType itemType) {
					itemType.setItemMeta(meta);
				} else if (object instanceof ItemStack itemStack1) {
					itemStack1.setItemMeta(meta);
				}
			}
		}
	}

	@Override
	public boolean isSingle() {
		return getExpr().isSingle();
	}

	@Override
	public Class<EquipSlot> getReturnType() {
		return EquipSlot.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the equipment slot of " + getExpr().toString(event, debug);
	}

}
