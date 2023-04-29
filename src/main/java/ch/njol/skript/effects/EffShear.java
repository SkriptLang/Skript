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
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

import io.papermc.paper.entity.Shearable;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Snowman;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Shear")
@Description({
	"Shears or un-shears a shearable entity with drops by shearing and a 'sheared' sound. Using with 'force' will force this effect despite the entity's 'shear state'.",
	"\nPlease note that..:",
	"\n- If your server is not running with Paper 1.19.4 or higher, this effect will only change its 'shear state', and the 'force' effect is unavailable",
	"\n- Force-shearing or un-shearing on a sheared mushroom cow is not possible"
})
@Examples({
	"on rightclick on a sheep holding a sword:",
		"\tshear the clicked sheep",
		"\tchance of 10%",
		"\tforce shear the clicked sheep"
})
@Since("2.0 (cows, sheep & snowmen), INSERT VERSION (all shearable entities)")
@RequiredPlugins("Paper 1.19.4+ (all shearable entities)")
public class EffShear extends Effect {

	private static final boolean INTERFACE_METHOD = Skript.classExists("io.papermc.paper.entity.Shearable");

	static {
		Skript.registerEffect(EffShear.class,
				(INTERFACE_METHOD ? "[:force] " : "") + "shear %livingentities%",
				"un[-]shear %livingentities%");
	}

	private Expression<LivingEntity> entity;
	private boolean force;
	private boolean shear;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entity = (Expression<LivingEntity>) exprs[0];
		force = parseResult.hasTag("force");
		shear = matchedPattern == 0;
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entity.getArray(event)) {
			if (shear && INTERFACE_METHOD) {
				if (!(entity instanceof Shearable))
					continue;
				Shearable shearable = ((Shearable) entity);
				if (!force && !shearable.readyToBeSheared())
					continue;
				shearable.shear();
				continue;
			}
			if (entity instanceof Sheep) {
				((Sheep) entity).setSheared(shear);
			} else if (entity instanceof Snowman) {
				((Snowman) entity).setDerp(shear);
			}
		}
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (shear ? "" : "un") + "shear " + entity.toString(event, debug);
	}
	
}
