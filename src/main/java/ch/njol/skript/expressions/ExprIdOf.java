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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.iterator.SingleItemIterator;

/**
 * @author Peter Güttinger
 */
@Name("Id")
@Description("The id of a specific item. You usually don't need this expression as you can likely do everything with aliases.")
@Examples({"message \"the ID of %type of the clicked block% is %id of the clicked block%.\""})
@Since("1.0")
public class ExprIdOf extends PropertyExpression<ItemType, Long> {
	
	static {
		Skript.registerExpression(ExprIdOf.class, Long.class, ExpressionType.PROPERTY, "[the] id(1¦s|) of %itemtype%", "%itemtype%'[s] id(1¦s|)");
	}
	
	@Nullable
	private static final MethodHandle getMaterialMethod;
	
	static {
		MethodHandle mh;
		try {
			mh = MethodHandles.lookup().findStatic(Material.class, "getMaterial", MethodType.methodType(Material.class, int.class));
		} catch (NoSuchMethodException | IllegalAccessException e) {
			mh = null;
		}
		getMaterialMethod = mh;
	}
	
	private boolean single = false;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		if (getMaterialMethod == null) {
			Skript.error("Items do not have numeric ids on Minecraft 1.13 or newer.");
			return false;
		} else {
			Skript.warning("Items do not have numeric ids on Minecraft 1.13 or newer. This script will not work on those versions!");
		}
		
		setExpr((Expression<ItemType>) vars[0]);
		if (parser.mark != 1) {
			single = true;
			if (!getExpr().isSingle() || (getExpr() instanceof Literal && ((Literal<ItemType>) getExpr()).getSingle().getTypes().size() != 1)) {
				Skript.warning("'" + getExpr() + "' has multiple ids");
				single = false;
			}
		}
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected Long[] get(final Event e, final ItemType[] source) {
		if (single) {
			final ItemType t = getExpr().getSingle(e);
			if (t == null)
				return new Long[0];
			return new Long[] {(long) t.getTypes().get(0).getType().getId()};
		}
		final ArrayList<Long> r = new ArrayList<>();
		for (final ItemType t : source) {
			for (final ItemData d : t) {
				r.add(Long.valueOf(d.getType().getId()));
			}
		}
		return r.toArray(new Long[r.size()]);
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the id" + (single ? "" : "s") + " of " + getExpr().toString(e, debug);
	}
	
	boolean changeItemStack;
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (!getExpr().isSingle())
			return null;
		if (!ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, ItemStack.class, ItemType.class))
			return null;
		changeItemStack = ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, ItemStack.class);
		switch (mode) {
			case ADD:
			case REMOVE:
			case SET:
				return new Class[] {Number.class};
			case RESET:
			case DELETE:
			case REMOVE_ALL:
			default:
				return null;
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		assert delta != null;
		final int i = ((Number) delta[0]).intValue();
		final ItemType it = getExpr().getSingle(e);
		if (it == null)
			return;
		final ItemStack is = it.getRandom();
		if (is == null)
			return;
		int type = is.getType().getId();
		switch (mode) {
			case ADD:
				type += i;
				break;
			case REMOVE:
				type -= i;
				break;
			case SET:
				type = i;
				break;
			case RESET:
			case DELETE:
			case REMOVE_ALL:
			default:
				assert false;
				return;
		}
		Material m = null;
		try {
			assert getMaterialMethod != null; // Got past init
			m = (Material) getMaterialMethod.invoke(type);
		} catch (Throwable ex) {
			Skript.exception(ex);
		}
		if (m != null) {
			is.setType(m);
			if (changeItemStack)
				getExpr().change(e, new ItemStack[] {is}, ChangeMode.SET);
			else
				getExpr().change(e, new ItemType[] {new ItemType(is)}, ChangeMode.SET);
		}
	}
	
	@Override
	@Nullable
	public Iterator<Long> iterator(final Event e) {
		if (single) {
			final ItemType t = getExpr().getSingle(e);
			if (t == null)
				return null;
			if (t.numTypes() == 0)
				return null;
			return new SingleItemIterator<>((long) t.getTypes().get(0).getType().getId());
		}
		final Iterator<? extends ItemType> iter = getExpr().iterator(e);
		if (iter == null || !iter.hasNext())
			return null;
		return new Iterator<Long>() {
			private Iterator<ItemData> current = iter.next().iterator();
			
			@Override
			public boolean hasNext() {
				while (iter.hasNext() && !current.hasNext()) {
					current = iter.next().iterator();
				}
				return current.hasNext();
			}
			
			@Override
			public Long next() {
				if (!hasNext())
					throw new NoSuchElementException();
				return (long) current.next().getType().getId();
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	@Override
	public Class<Long> getReturnType() {
		return Long.class;
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return s.equalsIgnoreCase("id");
	}
	
}
