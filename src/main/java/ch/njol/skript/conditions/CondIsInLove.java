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
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;

@Name("Entity In Love")
@Description("Whether or not animals are currently in a love state")
@Examples({
	"on right click on living entity:",
		"\tevent-entity is in love",
		"\tsend \"&c&oOhhh, this entity in love <3\" to player"
})
@Since("INSERT VERSION")
public class CondIsInLove extends PropertyCondition<LivingEntity> {

	static {
		register(CondIsInLove.class, "in lov(e|ing) [state]", "livingentities");
	}

	@Override
	public boolean check(LivingEntity livingEntity) {
		if (livingEntity instanceof Animals)
			return ((Animals) livingEntity).isLoveMode();
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "in love";
	}

}
