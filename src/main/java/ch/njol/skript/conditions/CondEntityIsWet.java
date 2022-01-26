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
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.bukkit.entity.Entity;

@Name("Entity is Wet")
@Description("Checks whether an entity is wet or not (in water, rain or a bubble column).")
@Examples("if player is wet:")
@RequiredPlugins("Paper 1.16+")
@Since("2.6.1")
public class CondEntityIsWet extends PropertyCondition<Entity> {
	
	static {
		if (Skript.methodExists(Entity.class, "isInWaterOrRainOrBubbleColumn"))
			register(CondEntityIsWet.class, PropertyType.BE, "wet", "entities");
	}

	@Override
	public boolean check(Entity entity) {
		return entity.isInWaterOrRainOrBubbleColumn();
	}

	@Override
	protected String getPropertyName() {
		return "wet";
	}

}
