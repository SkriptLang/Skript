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

import org.bukkit.entity.LivingEntity;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;

@Name("Is Climbing")
@Description("Whether a living entity is climbing, such as a spider up a wall or a player on a ladder.")
@Examples({
	"spawn a spider at location of spawn",
	"wait a second",
	"if the last spawned spider is climbing:",
		"\tmessage\"The spider is now climbing!\""
})
@RequiredPlugins("Minecraft 1.17+")
@Since("INSERT VERSION")
public class CondIsClimbing extends PropertyCondition<LivingEntity> {

	static {
		if (Skript.methodExists(LivingEntity.class, "isClimbing"))
			register(CondIsClimbing.class, "climbing", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity.isClimbing();
	}

	@Override
	protected String getPropertyName() {
		return "climbing";
	}

}
