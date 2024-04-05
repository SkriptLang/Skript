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
import java.util.Arrays;

import org.bukkit.event.Event;
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
import ch.njol.util.Kleenean;

@Name("Unbreakable Items")
@Description("Creates unbreakable copies of given items.")
@Examples("unbreakable iron sword #Creates unbreakable iron sword")
@Since("2.2-dev13b")
public class ExprUnbreakable extends PropertyExpression<ItemType, ItemType> {

	@Nullable
	private static final MethodHandle setUnbreakableMethod;
	
	static {
		Skript.registerExpression(ExprUnbreakable.class, ItemType.class, ExpressionType.PROPERTY, "unbreakable %itemtypes%");
		
		MethodHandle handle;
		try {
			handle = MethodHandles.lookup().findVirtual(Class.forName("package org.bukkit.inventory.meta.ItemMeta.Spigot"),
					"setUnbreakable", MethodType.methodType(void.class, boolean.class));
		} catch (NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
			handle = null;
		}
		setUnbreakableMethod = handle;
	}
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		setExpr((Expression<? extends ItemType>) exprs[0]);
		return true;
	}
	
	@Override
	protected ItemType[] get(final Event e, final ItemType[] source) {
		return get(source, itemType -> {
			ItemType clone = itemType.clone();

			ItemMeta meta = clone.getItemMeta();
			meta.setUnbreakable(true);
			clone.setItemMeta(meta);

			return clone;
		});
	}

	@Override
	public Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (e == null)
			return "unbreakable items";
		return "unbreakable " + Arrays.toString(getExpr().getAll(e));
	}
}
