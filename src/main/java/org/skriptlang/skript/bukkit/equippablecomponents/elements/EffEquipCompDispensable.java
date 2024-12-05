package org.skriptlang.skript.bukkit.equippablecomponents.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.jetbrains.annotations.Nullable;

@Name("Equippable Component - Dispensable")
@Description("If the item can be dispensed by a dispenser.")
@Examples({
	"set {_item} to be dispensable",
	"",
	"set {_component} to the equippable component of {_item}",
	"make {_component} undispensable",
	"set the equippable component of {_item} to {_component}"
})
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class EffEquipCompDispensable extends Effect {

	static {
		Skript.registerEffect(EffEquipCompDispensable.class,
			"(set|make) [the] %itemstacks/itemtypes/slots/equippablecomponents% [to [be]] dispensable",
			"(set|make) [the] %itemstacks/itemtypes/slots/equippablecomponents% [to [be]] (un|in|non)dispensable"
		);
	}

	private Expression<?> objects;
	private boolean dispensable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		objects = exprs[0];
		dispensable = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Object object : objects.getArray(event)) {
			if (object instanceof EquippableComponent component) {
				component.setDispensable(dispensable);
			} else {
				ItemStack itemStack = ItemUtils.asItemStack(object);
				if (itemStack == null)
					continue;
				ItemMeta meta = itemStack.getItemMeta();
				EquippableComponent component = meta.getEquippable();
				component.setDispensable(dispensable);
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
	public String toString(@Nullable Event event, boolean debug) {
		return "set the " + objects.toString(event, debug) + " to be " + (dispensable ? "dispensable" : "undispensable");
	}

}
