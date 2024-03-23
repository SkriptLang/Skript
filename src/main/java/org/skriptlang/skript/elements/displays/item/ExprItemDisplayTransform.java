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
package org.skriptlang.skript.elements.displays.item;

import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Item Display Transform")
@Description("Returns or changes the <a href='classes.html#itemdisplaytransform'>item display transform</a> of <a href='classes.html#display'>item displays</a>.")
@Examples("set the item transform of the last spawned item display to fixed # Reset to default")
@RequiredPlugins("Spigot 1.19.4+")
@Since("INSERT VERSION")
public class ExprItemDisplayTransform extends SimplePropertyExpression<Display, ItemDisplayTransform> {

	static {
		if (Skript.isRunningMinecraft(1, 19, 4))
			registerDefault(ExprItemDisplayTransform.class, ItemDisplayTransform.class, "[item] [display] transform", "displays");
	}

	@Override
	@Nullable
	public ItemDisplayTransform convert(Display display) {
		if (!(display instanceof ItemDisplay))
			return null;
		return ((ItemDisplay) display).getItemDisplayTransform();
	}

	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case ADD:
			case REMOVE:
			case REMOVE_ALL:
				break;
			case RESET:
			case DELETE:
				return CollectionUtils.array();
			case SET:
				return CollectionUtils.array(ItemDisplayTransform.class);
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		ItemDisplayTransform transform = mode == ChangeMode.SET ? (ItemDisplayTransform) delta[0] : ItemDisplayTransform.FIXED;
		for (Display display : getExpr().getArray(event)) {
			if (!(display instanceof ItemDisplay))
				continue;
			((ItemDisplay) display).setItemDisplayTransform(transform);
		}
	}

	@Override
	public Class<? extends ItemDisplayTransform> getReturnType() {
		return ItemDisplayTransform.class;
	}

	@Override
	protected String getPropertyName() {
		return "item display transform";
	}

}
