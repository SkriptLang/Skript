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

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.PaperEntityUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import io.papermc.paper.entity.LookAnchor;

@Name("Look At")
@Description("Forces the mob(s) or player(s) to look at an entity, vector or location. Vanilla max head pitches range from 10 to 50.")
@Examples({
	"force the head of the player to look towards event-entity's feet",
	"",
	"on entity explosion:",
		"\tset {_player} to the nearest player",
		"\t{_player} is set",
		"\tdistance between {_player} and the event-location is less than 15",
		"\tmake {_player} look towards vector from the {_player} to location of the event-entity",
	"",
	"force {_enderman} to face the block 3 meters above {_location} at head rotation speed 100.5 and max head pitch -40"
})
@Since("INSERT VERSION")
@RequiredPlugins("Paper 1.17+, Paper 1.19.1+ (Players & Look Anchors)")
public class EffLook extends Effect {

	private static final boolean LOOK_ANCHORS = Skript.classExists("io.papermc.paper.entity.LookAnchor");

	static {
		if (Skript.methodExists(Mob.class, "lookAt", Entity.class)) {
			if (LOOK_ANCHORS) {
				Skript.registerEffect(EffLook.class, "(force|make) %livingentities% [to] (face [towards]|look [(at|towards)]) " +
					"(%entity%['s (feet:feet|eyes)]|of:(feet:feet|eyes) of %entity%) " +
					"[at [head] [rotation] speed %-number%] [[and] max[imum] [head] pitch %-number%]",

					"(force|make) %livingentities% [to] (face [towards]|look [(at|towards)]) %vector/location% " +
					"[at [head] [rotation] speed %-number%] [[and] max[imum] [head] pitch %-number%]");
			} else {
				Skript.registerEffect(EffLook.class, "(force|make) %livingentities% [to] (face [towards]|look [(at|towards)]) %vector/location/entity% " +
					"[at [head] [rotation] speed %-number%] [[and] max[imum] [head] pitch %-number%]");
			}
		}
	}

	private LookAnchor anchor = LookAnchor.EYES;
	private Expression<LivingEntity> entities;

	@Nullable
	private Expression<Number> speed, maxPitch;

	/**
	 * Can be Vector, Location or an Entity.
	 */
	private Expression<?> target;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<LivingEntity>) exprs[0];
		if (LOOK_ANCHORS && matchedPattern == 0) {
			target = exprs[parseResult.hasTag("of") ? 2 : 1];
			speed = (Expression<Number>) exprs[3];
			maxPitch = (Expression<Number>) exprs[4];
			if (parseResult.hasTag("feet"))
				anchor = LookAnchor.FEET;
		} else {
			target = exprs[1];
			speed = (Expression<Number>) exprs[2];
			maxPitch = (Expression<Number>) exprs[3];
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		Object object = target.getSingle(event);
		if (object == null)
			return;
		Float speed = this.speed == null ? null : this.speed.getSingle(event).floatValue();
		Float maxPitch = this.maxPitch == null ? null : this.maxPitch.getSingle(event).floatValue();
		if (LOOK_ANCHORS) {
			PaperEntityUtils.lookAt(anchor, object, speed, maxPitch, entities.getArray(event));
			return;
		}
		PaperEntityUtils.lookAt(object, speed, maxPitch, entities.getArray(event));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "force " + entities.toString(event, debug) + " to look at " + target.toString(event, debug);
	}

}
