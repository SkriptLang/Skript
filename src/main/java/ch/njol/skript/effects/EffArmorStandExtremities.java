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
package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Armor Stand - Extremities")
@Description("Allows users to modify the extremities of an armor stand (i.e. whether its has arms or a base plate).")
@Examples({
	"show {_armorstand}'s arms",
	"hide the base plate of {_armorstands::*}"
})
@Since("INSERT VERSION")
@Keywords({"arms", "base plate"})
public class EffArmorStandExtremities extends Effect {

	static {
		Skript.registerEffect(EffArmorStandExtremities.class,
			"(show|reveal|:hide) %livingentities%'[s] (base[ ]plate|:arms)",
			"(show|reveal|:hide) [the] (base[ ]plate|:arms) of %livingentities%"
		);
	}

	private boolean show;
	private boolean arms;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<LivingEntity> entities;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<LivingEntity>) exprs[0];
		show = !parseResult.hasTag("hide");
		arms = parseResult.hasTag("arms");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof ArmorStand) {
				if (arms) {
					((ArmorStand) entity).setArms(show);
				} else {
					((ArmorStand) entity).setBasePlate(show);
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (show ? "show " : "hide ") + "the " + (arms ? "arms " : "base plate ") + "of " + entities.toString(event, debug);
	}
}
