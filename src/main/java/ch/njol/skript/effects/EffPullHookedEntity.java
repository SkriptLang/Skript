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
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.FishHook;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Pull Hooked Entity")
@Description("Pull the hooked entity to the caster of this fish hook.")
@Examples({
	"on fishing state of caught entity:",
		"\tpull hooked entity"
})
@Events("fishing")
@Since("INSERT VERSION")
public class EffPullHookedEntity extends Effect {

	static {
		Skript.registerEffect(EffPullHookedEntity.class, "(reel|pull) [in] hook[ed] entity [ofType:of %fishinghooks%]");
	}

	private Expression<FishHook> fishHook;

	@Override
	@SuppressWarnings({"null", "unchecked"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerFishEvent.class) && !parseResult.hasTag("ofType")) {
			Skript.error("The 'pull hooked entity' effect can either be used in the fishing event or by providing a fishing hook");
			return false;
		}
		fishHook = (Expression<FishHook>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (FishHook fishHook : fishHook.getArray(event)) {
			fishHook.pullHookedEntity();
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "pull hooked entity of " + fishHook.toString(event, debug);
	}

}
