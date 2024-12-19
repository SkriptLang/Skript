package org.skriptlang.skript.bukkit.equippablecomponents.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.bukkitutil.NamespacedUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.jetbrains.annotations.Nullable;

@Name("Equippable Component - Camera Overlay")
@Description({
	"The camera overlay for the player when the item is equipped.",
	"Example: The jack-o'-lantern view when having a jack-o'-lantern equipped as a helmet."
})
@Examples({
	"set the camera overlay of {_item} to \"custom_overlay\"",
	"",
	"set {_component} to the equippable component of {_item}",
	"set the camera overlay of {_component} to \"custom_overlay\"",
	"set the equippable component of {_item} to {_component}"
})
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class ExprEquipCompCameraOverlay extends PropertyExpression<Object, String> {

	static {
		Skript.registerExpression(ExprEquipCompCameraOverlay.class, String.class, ExpressionType.PROPERTY,
			"[the] camera overlay of %itemstacks/itemtypes/slots/equippablecomponents%"
		);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(exprs[0]);
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event, Object[] source) {
		return get(source, object -> {
			if (object instanceof EquippableComponent component)  {
				return component.getCameraOverlay().toString();
			} else {
				ItemStack itemStack = ItemUtils.asItemStack(object);
				if (itemStack == null)
					return null;
				return itemStack.getItemMeta().getEquippable().getCameraOverlay().toString();
			}
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.DELETE)
			return CollectionUtils.array(String.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, Changer.ChangeMode mode) {
		NamespacedKey key = null;
		if (delta[0] != null && delta[0] instanceof String string)
			key = NamespacedUtils.getNamespacedKey(string);

		for (Object object : getExpr().getArray(event)) {
			if (object instanceof EquippableComponent component) {
				component.setCameraOverlay(key);
			} else {
				ItemStack itemStack = ItemUtils.asItemStack(object);
				if (itemStack == null)
					continue;
				ItemMeta meta = itemStack.getItemMeta();
				EquippableComponent component = meta.getEquippable();
				component.setCameraOverlay(key);
				meta.setEquippable(component);
				itemStack.setItemMeta(meta);
				if (object instanceof Slot slot) {
					slot.setItem(itemStack);
				} else if (object instanceof ItemType itemType) {
					itemType.setItemMeta(meta);
				} else if (object instanceof  ItemStack itemStack1) {
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
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the camera overlay of " + getExpr().toString(event, debug);
	}

}
