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

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.iterator.ArrayIterator;
import ch.njol.util.coll.iterator.CheckedIterator;
import ch.njol.util.coll.iterator.IteratorIterable;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

@Name("Items")
@Description("Items or blocks of a specific type, useful for looping.")
@Examples({
	"loop items of type ore and log:",
	"\tblock contains loop-item",
	"\tmessage \"Theres at least one %loop-item% in this block\"",
	"drop all blocks at the player # drops one of every block at the player"
})
@Since("<i>unknown</i> (before 1.4.2)")
public class ExprItems extends SimpleExpression<ItemStack> {

	private static final ItemStack[] ALL_ITEMS = Arrays.stream(Material.values())
		.map(ItemStack::new)
		.toArray(ItemStack[]::new);

	static {
		Skript.registerExpression(ExprItems.class, ItemStack.class, ExpressionType.COMBINED,
			"[(all [[of] the]|the|every)] block(s|[ ]type[s])",
			"[(all [[of] the]|the|every)] blocks of type[s] %itemtypes%",
			"[(all [[of] the]|the|every)] items of type[s] %itemtypes%");
	}

	@Nullable
	private Expression<ItemType> itemTypeExpr;
	private boolean items;
	private ItemStack[] buffer = null;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		items = matchedPattern == 2;
		itemTypeExpr = matchedPattern == 0 ? null : (Expression<ItemType>) exprs[0];
		if (itemTypeExpr instanceof Literal) {
			for (ItemType itemType : ((Literal<ItemType>) itemTypeExpr).getAll())
				itemType.setAll(true);
		}
		return true;
	}

	@Override
	@Nullable
	protected ItemStack[] get(Event event) {
		if (buffer != null)
			return buffer;
		List<ItemStack> items = new ArrayList<>();
		for (ItemStack item : new IteratorIterable<>(iterator(event)))
			items.add(item);
		if (itemTypeExpr instanceof Literal)
			return buffer = items.toArray(new ItemStack[0]);
		return items.toArray(new ItemStack[0]);
	}

	@Override
	@Nullable
	public Iterator<ItemStack> iterator(Event event) {
		if (!items && itemTypeExpr == null)
			return new Iterator<ItemStack>() {

				private final Iterator<ItemStack> iterator = new ArrayIterator<>(Arrays.stream(ALL_ITEMS.clone())
					.filter(itemStack -> itemStack.getType().isBlock())
					.toArray(ItemStack[]::new));

				@Override
				public boolean hasNext() {
					return iterator.hasNext();
				}

				@Override
				public ItemStack next() {
					return new ItemStack(iterator.next());
				}

				@Override
				public void remove() {}
			};

		Iterator<ItemType> it = new ArrayIterator<>(itemTypeExpr.getArray(event));
		if (!it.hasNext())
			return null;

		Iterator<ItemStack> iter = new Iterator<ItemStack>() {

			Iterator<ItemStack> current = it.next().getAll().iterator();

			@Override
			public boolean hasNext() {
				while (!current.hasNext() && it.hasNext()) {
					current = it.next().getAll().iterator();
				}
				return current.hasNext();
			}

			@Override
			public ItemStack next() {
				if (!hasNext())
					throw new NoSuchElementException();
				return current.next();
			}

			@Override
			public void remove() {}

		};

		return new CheckedIterator<>(iter, object -> {
			if (object == null)
				return false;
			return items || object.getType().isBlock();
		});
	}

	@Override
	public Class<? extends ItemStack> getReturnType() {
		return ItemStack.class;
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "all of the " + (items ? "items" : "blocks") + (itemTypeExpr != null ? " of type " + itemTypeExpr.toString(event, debug) : "");
	}

}
