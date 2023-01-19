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

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Ignite/Extinguish")
@Description({"Lights entities on fire or extinguishes them."})
@Examples({"ignite the player",
		"extinguish the player"})
@Since("1.4")
public class EffIgnite extends Effect {
	static {
		Skript.registerEffect(EffIgnite.class,
				"(ignite|set fire to) %entities% [for %-timespan%]", "(set|light) %entities% on fire [for %-timespan%]",
				"extinguish %entities%");
	}
	
	private final static int DEFAULT_DURATION = 8 * 20; // default is 8 seconds for lava and fire, I didn't test other sources
	
	@SuppressWarnings("null")
	private Expression<Entity> entities;
	private boolean ignite;
	@Nullable
	private Expression<Timespan> duration = null;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
		ignite = exprs.length > 1;
		if (ignite)
			duration = (Expression<Timespan>) exprs[1];
		return true;
	}
	
	@Override
	protected void execute(final Event event) {
		final int d;
		if (duration != null) {
			final Timespan t = duration.getSingle(event);
			if (t == null)
				return;
			d = (int) (t.getTicks_i() >= Integer.MAX_VALUE ? Integer.MAX_VALUE : t.getTicks_i());
		} else {
			d = ignite ? DEFAULT_DURATION : 0;
		}
		for (final Entity en : entities.getArray(event)) {
			if (event instanceof EntityDamageEvent && ((EntityDamageEvent) event).getEntity() == en && !Delay.isDelayed(event)) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
					@Override
					public void run() {
						en.setFireTicks(d);
					}
				});
			} else {
				if (event instanceof EntityCombustEvent && ((EntityCombustEvent) event).getEntity() == en && !Delay.isDelayed(event))
					((EntityCombustEvent) event).setCancelled(true);// can't change the duration, thus simply cancel the event (and create a new one)
				en.setFireTicks(d);
			}
		}
	}
	
	@Override
	public String toString(final @Nullable Event event, final boolean debug) {
		if (ignite)
			return "set " + entities.toString(event, debug) + " on fire for " + (duration != null ? duration.toString(event, debug) : Timespan.fromTicks(DEFAULT_DURATION).toString());
		else
			return "extinguish " + entities.toString(event, debug);
	}
	
}
