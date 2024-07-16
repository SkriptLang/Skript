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
package org.skriptlang.skript.elements.expressions.itemframes;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.ThrowableProjectile;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.slot.DroppedItemSlot;
import ch.njol.skript.util.slot.ItemFrameSlot;
import ch.njol.skript.util.slot.Slot;
import ch.njol.skript.util.slot.ThrowableProjectileSlot;

@Name("Item of an Entity")
@Description({
	"An item associated with an entity. For dropped item entities, it gets the item that was dropped.",
	"For item frames, the item inside the frame is returned.",
	"For throwable projectiles (snowballs, enderpearls etc.) it gets the displayed item.",
	"Other entities do not have items associated with them."
})
@Examples("set item of target entity to stone")
@Since("2.2-dev35, 2.2-dev36 (improved), 2.5.2 (throwable projectiles)")
@RequiredPlugins("Minecraft 1.15.2+ (throwable projectiles)")
public class ExprItemFrameSlot extends SimplePropertyExpression<Entity, Slot> {

	private static final boolean PROJECTILE_SUPPORT = Skript.classExists("org.bukkit.entity.ThrowableProjectile");

	static {
		register(ExprItemFrameSlot.class, Slot.class, "item", "entities");
	}

	@Override
	@Nullable
	public Slot convert(Entity entity) {
		if (entity instanceof ItemFrame) {
			return new ItemFrameSlot((ItemFrame) entity);
		} else if (entity instanceof Item) {
			return new DroppedItemSlot((Item) entity);
		} else if (PROJECTILE_SUPPORT && entity instanceof ThrowableProjectile) {
			return new ThrowableProjectileSlot((ThrowableProjectile) entity);
		}
		return null; // Other entities don't have associated items
	}

	@Override
	public Class<? extends Slot> getReturnType() {
		return Slot.class;
	}

	@Override
	protected String getPropertyName() {
		return "item of entity";
	}

}
