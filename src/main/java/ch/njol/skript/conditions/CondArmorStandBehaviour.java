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
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;

@Name("Armor Stand - Has Behaviour")
@Description("Allows users to check the behaviour of an armor stand (i.e. whether it is ticking or moving).")
@Examples({
	"if {_armorstands::*} are not able to tick:",
	"if {_armorstand} is able to move:"
})
@Since("INSERT VERSION")
public class CondArmorStandBehaviour extends PropertyCondition<LivingEntity> {

	static {
		if (Skript.methodExists(ArmorStand.class, "canTick"))
			register(CondArmorStandBehaviour.class, "able to (:tick|move)", "livingentities");
	}

	private boolean tick;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		tick = parseResult.hasTag("tick");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(LivingEntity livingEntity) {
		if (livingEntity instanceof ArmorStand)
			return tick ? ((ArmorStand) livingEntity).canTick() : ((ArmorStand) livingEntity).canMove();
		return false;
	}

	@Override
	public String getPropertyName() {
		if (tick)
			return "able to tick";
		return "able to move";
	}
}
