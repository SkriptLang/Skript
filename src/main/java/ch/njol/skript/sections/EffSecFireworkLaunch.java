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

package ch.njol.skript.sections;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;

@Name("Launch Firework")
@Description({
	"Launch firework effects at the given location(s).",
	"This can be used as an effect and as a section.",
	"If it is used as a section, the section is run before the entity is added to the world.",
	"You can modify the firework in this section, using for example <code>event-entity</code>.",
	"Do note that other event values, such as <code>player</code>, won't work in this section."
})
@Examples({
	"launch ball large coloured red, purple and white fading to light green and black at player's location with duration 1",
	"",
	"command /firework:",
	"\ttrigger:",
		"\t\tlaunch a firework with effects ball large coloured red at player:",
			"\t\t\tset metadata value \"cancel damage\" of event-entity to true",
	"",
	"# Firework launch section example",
	"on damage:",
		"\tdamage cause is entity explosion",
		"\tmetadata value \"cancel damage\" of event-projectile is true",
		"\tcancel event",
})
@Since("2.4, INSERT VERSION (section)")
public class EffSecFireworkLaunch extends EffectSection {

	public static class FireworkSectionLaunchEvent extends Event {

		private final Firework firework;

		public FireworkSectionLaunchEvent(Firework firework) {
			this.firework = firework;
		}

		public Firework getFirework() {
			return firework;
		}

		@Override
		@NotNull
		public HandlerList getHandlers() {
			throw new IllegalStateException();
		}

	}

	static {
		Skript.registerSection(EffSecFireworkLaunch.class, "(launch|deploy) [[a] firework [with effect[s]]] %fireworkeffects% at %locations% [([with] (duration|power)|timed) %-number%]");
		EventValues.registerEventValue(FireworkSectionLaunchEvent.class, Firework.class, new Getter<Firework, FireworkSectionLaunchEvent>() {
			@Override
			public Firework get(FireworkSectionLaunchEvent fireworkLaunchEvent) {
				return fireworkLaunchEvent.getFirework();
			}
		}, EventValues.TIME_NOW);
	}

	private Expression<FireworkEffect> effects;
	private Expression<Location> locations;

	@Nullable
	public static Entity lastSpawned;

	@Nullable
	private Expression<Number> power;

	@Nullable
	private Trigger trigger;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult,
			@Nullable SectionNode sectionNode, @Nullable List<TriggerItem> triggerItems) {

		effects = (Expression<FireworkEffect>) exprs[0];
		locations = (Expression<Location>) exprs[1];
		power = (Expression<Number>) exprs[2];

		if (sectionNode != null)
			trigger = loadCode(sectionNode, "firework launch", FireworkSectionLaunchEvent.class);

		return true;
	}

	@Override
	@Nullable
	protected TriggerItem walk(Event event) {
		FireworkEffect[] effects = this.effects.getArray(event);
		// Uses org.bukkit consumer for older versions support. If newer, bukkit maps this class to java utils.
		Consumer<Firework> consumer = null;
		if (trigger != null) {
			consumer = firework -> {
				FireworkSectionLaunchEvent fireworkLaunchEvent = new FireworkSectionLaunchEvent(firework);
				Variables.setLocalVariables(fireworkLaunchEvent, Variables.copyLocalVariables(event));
				TriggerItem.walk(trigger, fireworkLaunchEvent);
				Variables.setLocalVariables(event, Variables.copyLocalVariables(fireworkLaunchEvent));
				Variables.removeLocals(fireworkLaunchEvent);
			};
		}

		int power = this.power != null ? this.power.getOptionalSingle(event).orElse(1).intValue() : 1;
		for (Location location : locations.getArray(event)) {
			World world = location.getWorld();
			if (world == null)
				continue;
			@SuppressWarnings("deprecation")
			Firework firework = EntityData.spawn(location, Firework.class, consumer);
			FireworkMeta meta = firework.getFireworkMeta();
			meta.addEffects(effects);
			meta.setPower(power);
			firework.setFireworkMeta(meta);
			lastSpawned = firework;
		}

		return super.walk(event, false);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "launch fireworks " + effects.toString(event, debug) +
				" at " + locations.toString(event, debug) +
				" timed " + (power != null ? power.toString(event, debug) : "1");
	}

}
