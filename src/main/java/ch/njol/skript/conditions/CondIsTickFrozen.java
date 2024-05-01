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
import ch.njol.skript.bukkitutil.ServerUtils;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.ServerTickManager;
import org.bukkit.entity.Entity;

@Name("Is Entity Tick Frozen")
@Description("Checks if the specified entities are frozen due to the server's ticking state.")
@Examples({"if target entity is tick frozen:"})
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.20.4+")
public class CondIsTickFrozen extends PropertyCondition<Entity> {


	static {
		if (Skript.methodExists(Server.class, "getServerTickManager")) {
			register(CondIsTickFrozen.class, PropertyType.BE, "tick frozen", "entities");
		}
	}

	@Override
	public boolean check(Entity entity) {
		return ServerUtils.getServerTickManager().isFrozen(entity);
	}

	@Override
	protected String getPropertyName() {
		return "tick frozen";
	}
}

