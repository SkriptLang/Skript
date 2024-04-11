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
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.MainHand;

@Name("Left Handed")
@Description({
	"Checks if living entities or players are left or right-handed. Armor stands are neither right nor left-handed.",
	"Paper 1.17.1+ is required for non-player entities."
})
@Examples({
	"on damage of player:",
		"\tif victim is left handed:",
			"\t\tcancel event"
})
@Since("2.8.0")
@RequiredPlugins("Paper 1.17.1+ (entities)")
public class CondIsLeftHanded extends PropertyCondition<LivingEntity> {

	private static final boolean CAN_USE_ENTITIES = Skript.methodExists(Mob.class, "isLeftHanded");

	static {
		if (CAN_USE_ENTITIES) {
			register(CondIsLeftHanded.class, PropertyType.BE, "(:left|right)( |-)handed", "livingentities");
		} else {
			register(CondIsLeftHanded.class, PropertyType.BE, "(:left|right)( |-)handed", "players");
		}
	}

	private MainHand hand;

	@Override
	public boolean init(Expression[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		hand = parseResult.hasTag("left") ? MainHand.LEFT : MainHand.RIGHT;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(LivingEntity livingEntity) {
		// check if entity is a mob and if the method exists
		if (CAN_USE_ENTITIES && livingEntity instanceof Mob)
			return ((Mob) livingEntity).isLeftHanded() == (hand == MainHand.LEFT);

		// check if entity is a player
		if (livingEntity instanceof HumanEntity)
			return ((HumanEntity) livingEntity).getMainHand() == hand;

		// invalid entity
		return false;
	}

	@Override
	protected String getPropertyName() {
		return (hand == MainHand.LEFT ? "left" : "right") + " handed";
	}
	
}
