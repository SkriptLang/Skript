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
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import io.papermc.paper.entity.Shearable;
import org.bukkit.entity.Cow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Snowman;

@Name("Entity Is Sheared")
@Description("Checks whether an entity is sheared.")
@Examples({
	"if targeted entity of player is sheared:",
		"\tsend \"This entity has nothing left to shear!\" to player"
})
@Since("INSERT VERSION")
public class CondIsSheared extends PropertyCondition<LivingEntity> {

	private static final boolean interfaceMethod = Skript.classExists("io.papermc.paper.entity.Shearable");

	static {
		register(CondIsSheared.class, "(sheared|shorn)", "livingentity");
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Cow) // As sheared mooshroom cow is a Cow which does not implements Shearable
			return true;
		if (interfaceMethod) {
			if (!(entity instanceof Shearable))
				return false;
			return !((Shearable) entity).readyToBeSheared();
		}
		if (entity instanceof Sheep)
			return ((Sheep) entity).isSheared();
		if (entity instanceof Snowman)
			return ((Snowman) entity).isDerp();
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "sheared";
	}
}
