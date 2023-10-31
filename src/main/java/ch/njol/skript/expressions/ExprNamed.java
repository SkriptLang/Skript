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

import ch.njol.skript.lang.Literal;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Getter;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Named Item/Inventory")
@Description("Directly names an item/inventory, useful for defining a named item/inventory in a script. " +
		"If you want to (re)name existing items/inventories you can either use this expression or use <code>set <a href='#ExprName'>name of &lt;item/inventory&gt;</a> to &lt;text&gt;</code>.")
@Examples({"give a diamond sword of sharpness 100 named \"&lt;gold&gt;Excalibur\" to the player",
		"set tool of player to the player's tool named \"&lt;gold&gt;Wand\"",
		"set the name of the player's tool to \"&lt;gold&gt;Wand\"",
		"open hopper inventory named \"Magic Hopper\" to player"})
@Since("2.0, 2.2-dev34 (inventories)")
public class ExprNamed extends PropertyExpression<Object, Object> {
	static {
		Skript.registerExpression(ExprNamed.class, Object.class, ExpressionType.PROPERTY,
				"%itemtype/inventorytype% (named|with name[s]) %string%");
	}
	
	@SuppressWarnings("null")
	private Expression<String> name;
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (exprs[0].getReturnType().equals(ItemStack.class)) {
			setExpr(exprs[0].getConvertedExpression(ItemType.class));
		} else {
			setExpr(exprs[0]);
		}
		name = (Expression<String>) exprs[1];
		check_type_okay:
		if (getExpr() instanceof Literal) {
			Literal<?> literal = (Literal<?>) getExpr();
			Object object = literal.getSingle();
			if (!(object instanceof InventoryType)) break check_type_okay;
			if (!isCreatable((InventoryType) object)) {
				Skript.error("You can't create a '" + literal + "' inventory. It's not creatable!");
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected Object[] get(final Event event, final Object[] source) {
		String name = this.name.getSingle(event);
		if (name == null)
			return get(source, object -> {
				if (object instanceof InventoryType && !isCreatable(((InventoryType) object)))
					return null;
				return object; // Return the same ItemType they passed without applying a name.
			});
		return get(source, new Getter<Object, Object>() {
            @Override
            @Nullable
            public Object get(Object object) {
                if (object instanceof InventoryType) {
                    InventoryType type = (InventoryType) object;
                    if (!isCreatable(type))
                        return null;
                    else
                        return Bukkit.createInventory(null, (InventoryType) object, name);
                }
                if (object instanceof ItemStack) {
                    ItemStack stack = (ItemStack) object;
                    stack = stack.clone();
                    ItemMeta meta = stack.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(name);
                        stack.setItemMeta(meta);
                    }
                    return new ItemType(stack);
                }
                ItemType item = (ItemType) object;
                item = item.clone();
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(name);
                item.setItemMeta(meta);
                return item;
            }
        });
	}
	
	@Override
	public Class<? extends Object> getReturnType() {
		Class<?> returnType = getExpr().getReturnType();
		if (returnType == InventoryType.class)
			return Inventory.class;
		else if (returnType == Object.class)
			return Object.class;
		return ItemType.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return getExpr().toString(e, debug) + " named " + name;
	}
	
	private boolean isCreatable(InventoryType type) {
		// Spigot forgot to label some InventoryTypes as non-creatable in some versions < 1.19.4
		// So this throws NullPointerException as well as an IllegalArgumentException.
		// See https://hub.spigotmc.org/jira/browse/SPIGOT-7301
		if (Skript.isRunningMinecraft(1, 14) && type == InventoryType.COMPOSTER) return false;
		if (Skript.isRunningMinecraft(1, 20) && type == InventoryType.CHISELED_BOOKSHELF) return false;
		return type.isCreatable();
	}
	
}
