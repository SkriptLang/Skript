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

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.config.Node;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Güttinger
 */
@Name("Inventory")
@Description("The inventory of a block or player. You can usually omit this expression and can directly add or remove items to/from blocks or players.")
@Examples({"add a plank to the player's inventory",
		"clear the player's inventory",
		"remove 5 wool from the inventory of the clicked block"})
@Since("1.0")
public class ExprInventory extends SimpleExpression<Object> {

	private boolean inLoop;
	@SuppressWarnings("null")
	private Expression<?> holders;

	static {
		PropertyExpression.register(ExprInventory.class, Object.class, "inventor(y|ies)", "inventoryholders/itemtypes");
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		// prevent conflict with ExprItemsIn (https://github.com/SkriptLang/Skript/issues/6290)
		if (exprs[0].getSource() instanceof ExprItemsIn)
			return false;
		// if we're dealing with a loop of just this expression
		Node n = SkriptLogger.getNode();
		inLoop = n != null && ("loop " + parseResult.expr).equals(n.getKey());
		holders = exprs[0];
		return true;
	}

	@Override
	protected Object[] get(Event e) {
		List<Inventory> inventories = new ArrayList<>();
		for (Object holder : holders.getArray(e)) {
			if (holder instanceof InventoryHolder) {
				inventories.add(((InventoryHolder) holder).getInventory());
			} else if (holder instanceof ItemType) {
				ItemMeta meta = ((ItemType) holder).getItemMeta();
				if (!(meta instanceof  BlockStateMeta))
					continue;
				BlockState state = ((BlockStateMeta) meta).getBlockState();
				if (!(state instanceof Container))
					continue;
				Inventory underlyingInv = ((Container) state).getInventory();
				// The proxy is used here to ensure that any changes to the inventory are reflected in the
				// BlockStateMeta and ItemMeta of `holder`
				Inventory proxy = (Inventory) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Inventory.class}, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						Object returnValue = method.invoke(underlyingInv, args);
						// calling update here causes the changes to the inventory to be synced to the meta
						boolean updateSucceeded = state.update();
						if (updateSucceeded) {
							((BlockStateMeta) meta).setBlockState(state);
							((ItemType) holder).setItemMeta(meta);
						}
						return returnValue;
					}
				});
				inventories.add(proxy);
			}
		}
		Inventory[] invArray = inventories.toArray(new Inventory[0]);
		if (inLoop) {
			/*
			 * Return the items in the inventory if in a loop using the items
			 * in inventory expression to not duplicate code
			 */
			ExprItemsIn expr = new ExprItemsIn();
			expr.init(new Expression[] {
					new SimpleExpression() {
						@Override
						protected Object[] get(Event e) {
							return invArray;
						}

						@Override
						public boolean isSingle() {
							return invArray.length == 1;
						}

						@Override
						public Class<?> getReturnType() {
							return Inventory.class;
						}

						@Override
						public String toString(@Nullable Event e, boolean debug) {
							return "loop of inventory expression";
						}

						@Override
						public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
							return true;
						}
					}
			}, 0, Kleenean.FALSE, null);
			return expr.get(e);
		}
		return invArray;
	}

	@Override
	public boolean isSingle() {
		return !inLoop && holders.isSingle();
	}

	@Override
	public Class<?> getReturnType() {
		return inLoop ? Slot.class : Inventory.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "inventor" + (holders.isSingle() ? "y" : "ies") + " of " + holders.toString(e, debug);
	}
	
}
