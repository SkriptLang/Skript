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
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Armor Stand - Behaviour")
@Description("Allows users to modify the behaviour of an armor stand (i.e. whether it can tick and can move).")
@Examples({
	"allow {_armorstand} to move",
	"prevent {_armorstands::*} from ticking"
})
@Since("INSERT VERSION")
public class EffArmorStandBehaviour extends Effect {

	static {
		if (Skript.methodExists(ArmorStand.class, "canTick"))
			Skript.registerEffect(EffArmorStandBehaviour.class,
				"allow %livingentities% to (:tick|move)",
				"prevent %livingentities% from (being able to (:tick|move)|tick:ticking|moving)"
			);
	}

	private boolean prevent;
	private boolean ticking;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Entity> entities;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
		prevent = matchedPattern == 1;
		ticking = parseResult.hasTag("tick");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Entity entity : entities.getArray(event)) {
			if (entity instanceof ArmorStand) {
				if (ticking) {
					((ArmorStand) entity).setCanTick(!prevent);
				} else {
					((ArmorStand) entity).setCanMove(!prevent);
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (prevent ? "prevent " : "allow ") + entities.toString(event, debug) + (prevent ? " from being able to " : " to ") + (ticking ? "tick" : "move");
	}

}
