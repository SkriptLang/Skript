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
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;

@Name("Armor Stand - Has Properties")
@Description("Allows users to check the properties of an armor stand (i.e. whether it's small or a marker).")
@Examples({
	"send true if {_armorstand} is small",
	"if {_armorstands::*} are not markers:",
})
@Since("INSERT VERSION")
public class CondArmorStandProperties extends PropertyCondition<LivingEntity> {

	static {
		register(CondArmorStandProperties.class, "(:small|[a] marker[s])", "livingentities");
	}

	private boolean small;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		small = parseResult.hasTag("small");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(LivingEntity livingEntity) {
		if (livingEntity instanceof ArmorStand)
			return small ? ((ArmorStand) livingEntity).isSmall() : ((ArmorStand) livingEntity).isMarker();
		return false;
	}

	@Override
	public String getPropertyName() {
		if (small)
			return "small";
		return "a marker";
	}

}
