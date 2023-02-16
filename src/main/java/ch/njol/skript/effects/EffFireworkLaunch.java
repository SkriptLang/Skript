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

import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.FireworkMeta;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Launch firework")
@Description("Launch firework effects at the given location(s).")
@Examples("launch ball large colored red, purple and white fading to light green and black at player's location with duration 1")
@Since("2.4")
public class EffFireworkLaunch extends Effect {
	
	static {
		Skript.registerEffect(EffFireworkLaunch.class, "(launch|deploy) [[a] firework [with effect[s]]] %fireworkeffects% at %locations% [([with] (duration|power)|timed) %number%]");
	}

	@Nullable
	public static Entity lastSpawned = null;

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<FireworkEffect> effects;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Location> locations;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Number> lifetime;
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		effects = (Expression<FireworkEffect>) exprs[0];
		locations = (Expression<Location>) exprs[1];
		lifetime = (Expression<Number>) exprs[2];
		return true;
	}

	@Override
	protected void execute(Event event) {
		FireworkEffect[] effects = this.effects.getArray(event);
		int power = lifetime.getOptionalSingle(event).orElse(1).intValue();
		power = Math.min(127, Math.max(0, power));
		for (Location location : locations.getArray(event)) {
			World world = location.getWorld();
			if (world == null)
				continue;
			Firework firework = world.spawn(location, Firework.class);
			FireworkMeta meta = firework.getFireworkMeta();
			meta.addEffects(effects);
			meta.setPower(power);
			firework.setFireworkMeta(meta);
			lastSpawned = firework;
		}
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "Launch firework(s) " + effects.toString(event, debug) +
				" at location(s) " + locations.toString(event, debug) +
				" timed " + lifetime.toString(event, debug);
	}

}
