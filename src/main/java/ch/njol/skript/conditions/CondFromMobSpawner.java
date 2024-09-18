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

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Entity;

@Name("From A Mob Spawner")
@Description("Check if an entity was spawned from a mob spawner")
@Examples({
	"send true if target is from a mob spawner"
})
@Since("INSERT VERSION")
public class CondFromMobSpawner extends PropertyCondition<Entity> {

	static {
		register(CondFromMobSpawner.class, PropertyType.BE,
			"from a mob spawner", "entities");
	}

	@Override
	public boolean check(Entity value) {
		return value.fromMobSpawner();
	}

	@Override
	protected String getPropertyName() {
		return "from a mob spawner";
	}

}
