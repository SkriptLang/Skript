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
package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import org.bukkit.Nameable;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

@Name("Is Named")
@Description("Checks whether or not a item, block, slot, inventory, player or entity is named or has a given name")
@Examples({
	"on right click with a cake:",
	"\tif event-item is named \"&bBirthday Cake\"",
	"\t\tsend \"Happy Birthday, %player's name%!\"",
	"",
	"on damage of a player:",
	"\tattacker's tool is named",
	"\tcancel event",
	"",
	"on inventory click:",
	"\tevent-inventory is named \"Example Inventory\"",
	"\tcancel event",
	""
})
@Since("INSERT VERSION")
public class CondIsNamed extends Condition {

	@Nullable
	static final MethodHandle TITLE_METHOD;
	private Expression<Object> objects;
	@Nullable
	private Expression<String> name;

	static {
		PropertyCondition.register(CondIsNamed.class, PropertyType.BE, "named [%-string%]", "itemtypes/blocks/slots/inventories/offlineplayers/entities");

		MethodHandle _METHOD = null;
		try {
			_METHOD = MethodHandles.lookup().findVirtual(Inventory.class, "getTitle", MethodType.methodType(String.class));
		} catch (IllegalAccessException | NoSuchMethodException ignored) {}
		TITLE_METHOD = _METHOD;
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		objects = (Expression<Object>) exprs[0];
		name = (Expression<String>) exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		String name = this.name != null ? this.name.getSingle(event) : null;
		return objects.check(event, object -> {
			if (object instanceof ItemType) {
				ItemMeta itemMeta = ((ItemType) object).getItemMeta();
				if (name != null && itemMeta.hasDisplayName()) {
					return itemMeta.getDisplayName().equalsIgnoreCase(name);
				}
				return itemMeta.hasDisplayName();
			} else if (object instanceof Slot) {
				ItemStack itemStack = ((Slot) object).getItem();
				ItemMeta itemMeta = itemStack.getItemMeta();
				if (name != null)
					return itemMeta.getDisplayName().equalsIgnoreCase(name);
				return itemMeta.hasDisplayName();
			} else if (object instanceof Block) {
				BlockState state = ((Block) object).getState();
				if (state instanceof Nameable)
					if (name != null)
						return ((Nameable) state).getCustomName().equalsIgnoreCase(name);
					return ((Nameable) state).getCustomName() != null;
			} else if (object instanceof Inventory) {
				if (TITLE_METHOD != null) {
					try {
						if (name != null) {
							return TITLE_METHOD.invoke(object).equals(name);
						}
						return TITLE_METHOD.invoke(object) != null;
					} catch (Throwable error) {
						Skript.exception(error);
						return false;
					}
				} else {
					List<HumanEntity> viewers = ((Inventory) object).getViewers();
					String defaultTitle = ((Inventory) object).getType().getDefaultTitle();
					if (!viewers.isEmpty()) {
						if (name != null)
							return viewers.get(0).getOpenInventory().getTitle().equalsIgnoreCase(name);
						return viewers.get(0).getOpenInventory().getTitle() != null && !viewers.get(0).getOpenInventory().getTitle().equalsIgnoreCase(defaultTitle);
					}
				}
			} else if (object instanceof OfflinePlayer) {
				if (name != null)
					return ((OfflinePlayer) object).getName().equalsIgnoreCase(name);
				return true;
			} else if (object instanceof Entity) {
				if (name != null)
					return ((Entity) object).getCustomName().equalsIgnoreCase(name);
				return ((Entity) object).getCustomName() != null;
			}
			return false;
		}, isNegated() );
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.BE, event, debug, objects,
			"named" + (name == null ? "" : " " + name.toString(event, debug)));
	}
}
