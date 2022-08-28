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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

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
import ch.njol.skript.util.Color;
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.Kleenean;
import ch.njol.util.NullableChecker;
import ch.njol.util.coll.iterator.ArrayIterator;
import ch.njol.util.coll.iterator.CheckedIterator;
import ch.njol.util.coll.iterator.IteratorIterable;

@Name("Sets")
@Description("Collection sets of items or blocks of a specific type or colours, useful for looping.")
@Examples({
		"loop items of type ore and log:",
				"\tblock contains loop-item",
				"\tmessage \"Theres at least one %loop-item% in this block\"",
		"drop all blocks at the player # drops one of every block at the player"
})
@Since("<i>unknown</i> (before 1.4.2), INSERT VERSION (colors)")
public class ExprSets extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprSets.class, Object.class, ExpressionType.COMBINED,
				"[(all [[of] the]|the|every)] item(s|[ ]types)", "[(all [[of] the]|the)] items of type[s] %itemtypes%",
				"[(all [[of] the]|the|every)] block(s|[ ]types)", "[(all [[of] the]|the)] blocks of type[s] %itemtypes%",
				"[(all [[of] the]|the|every)] colo[u]rs");
	}

	@Nullable
	private Expression<ItemType> types;
	private int pattern = -1;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		if (exprs.length > 0)
			types = (Expression<ItemType>) exprs[0];
		pattern = matchedPattern;
		if (types instanceof Literal) {
			for (ItemType type : ((Literal<ItemType>) types).getAll())
				type.setAll(true);
		}
		return true;
	}

	private Object[] buffer = null;

	@Override
	protected Object[] get(Event event) {
		if (buffer != null)
			return buffer;
		List<Object> elements = new ArrayList<>();
		for (Object element : new IteratorIterable<>(iterator(event)))
			elements.add(element);
		if (types instanceof Literal)
			return buffer = elements.toArray();
		return elements.toArray();
	}

	@Override
	@Nullable
	public Iterator<Object> iterator(Event event) {
		if (pattern == 4)
			return new ArrayIterator<>(SkriptColor.values());
		if (pattern == 0 || pattern == 2)
			return new Iterator<Object>() {

				private final Iterator<Material> iter = new ArrayIterator<>(Material.values());

				@Override
				public boolean hasNext() {
					return iter.hasNext();
				}

				@Override
				public ItemStack next() {
					return new ItemStack(iter.next());
				}

				@Override
				public void remove() {}

			};
		Iterator<ItemType> it = new ArrayIterator<>(types.getArray(event));
		if (!it.hasNext())
			return null;
		Iterator<Object> iter;
		iter = new Iterator<Object>() {

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

		return new CheckedIterator<>(iter, ob -> {
			if (ob == null)
				return false;
			if (ob instanceof ItemStack)
				if (!((ItemStack) ob).getType().isBlock())
					return false;
			return true;
		});
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends Object> getReturnType() {
		if (pattern == 4)
			return Color.class;
		return ItemStack.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (pattern == 4)
			return "all of the colors";
		if (types != null)
			return (pattern == 1 ? "items" : "blocks") + " of type " + types.toString(event, debug);
		return "all of the " + (pattern == 0 ? "items" : "blocks");
	}

	@Override
	public boolean isLoopOf(String s) {
		return (pattern == 4 && s.equalsIgnoreCase("color") || s.equalsIgnoreCase("colour"))
			|| (pattern == 2 || pattern == 3 && s.equalsIgnoreCase("block"))
			|| (pattern < 2 && s.equalsIgnoreCase("item"));
	}

}
