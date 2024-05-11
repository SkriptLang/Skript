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
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.jetbrains.annotations.Nullable;

@Name("Allow / Prevent Leash Drop")
@Description("Allows or prevents the leash to drop in an unleash event.")
@Examples({
	"on unleash:",
		"\tif player is not set:",
			"\t\tprevent the leash from dropping",
		"\telse if player is op:",
			"\t\tallow the leash to drop"
})
@Events("Unleash")
@Since("INSERT VERSION")
public class EffDropLeash extends Effect {

	static {
		if (Skript.methodExists(EntityUnleashEvent.class, "setDropLeash", boolean.class))
			Skript.registerEffect(EffDropLeash.class,
					"(force|allow) [the] (lead|leash) [item] to drop",
					"(block|disallow|prevent) [the] (lead|leash) [item] from dropping"
		);
	}

	private boolean allowLeashDrop;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!getParser().isCurrentEvent(EntityUnleashEvent.class)) {
			Skript.error("The 'drop leash' effect can only be used in an 'unleash' event");
			return false;
		}
		allowLeashDrop = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (!(event instanceof EntityUnleashEvent))
			return;
		((EntityUnleashEvent) event).setDropLeash(allowLeashDrop);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return allowLeashDrop ? "allow the leash to drop" : "prevent the leash from dropping";
	}

}
