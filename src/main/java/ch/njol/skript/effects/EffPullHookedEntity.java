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
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Pull Hooked Entity")
@Description("Pull the hooked entity to the caster of this fish hook.")
@Examples({"on fishing state of caught entity:",
			"\tpull hooked entity"})
@Events("fishing")
@Since("INSERT VERSION")
public class EffPullHookedEntity extends Effect {

	static {
		Skript.registerEffect(EffPullHookedEntity.class, "pull hook[ed] entity [of [fish[ing]] hook]");
	}

	@Override
	@SuppressWarnings("null")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
		if (!getParser().isCurrentEvent(PlayerFishEvent.class)) {
			Skript.error("The 'pull hooked entity' effect can only be used in fish event.");
			return false;
		}
		return true;
	}

	@Override
	protected void execute(Event e) {
		((PlayerFishEvent) e).getHook().pullHookedEntity();
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "pull hooked entity of fishing hook";
	}

}
