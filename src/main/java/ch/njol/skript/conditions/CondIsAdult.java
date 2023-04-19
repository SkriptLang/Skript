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
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;

@Name("Is An Adult")
@Description("Returns whether or not animals are an adult, requires 1.16.5+ for mobs")
@Examples({
	"on spawn:",
		"\tevent-entity is not an adult",
		"\tmake event-entity an adult"
})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.16.5+ (Mobs)")
public class CondIsAdult extends PropertyCondition<LivingEntity> {

	// This is required since before 1.16 the 'isAdult' method only supported Animals
	private static final boolean HAS_MOB_SUPPORT = Skript.isRunningMinecraft(1, 16, 5);

	static {
		register(CondIsAdult.class, "[an] adult", "livingentities");
	}

	@Override
	public boolean check(LivingEntity livingEntity) {
		if (!(livingEntity instanceof Ageable))
			return false;
		if (HAS_MOB_SUPPORT || livingEntity instanceof Animals)
			return ((Ageable) livingEntity).isAdult();
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "an adult";
	}

}
