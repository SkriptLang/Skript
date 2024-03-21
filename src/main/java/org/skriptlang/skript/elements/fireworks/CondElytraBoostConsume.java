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
package org.skriptlang.skript.elements.fireworks;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Elytra Boost Will Consume")
@Description("Checks if the firework will be consumed in an <a href='events.html#elytra_boost'>elytra boost event</a>.")
@Examples({
	"on player elytra boost:",
		"\twill consume firework",
		"\tset to consume the firework"
})
@RequiredPlugins("Paper")
@Since("INSERT VERSION")
public class CondElytraBoostConsume extends Condition {

	static {
		if (Skript.classExists("com.destroystokyo.paper.event.player.PlayerElytraBoostEvent"))
			Skript.registerCondition(CondElytraBoostConsume.class, "[event] (will [:not]|not:won't) consume [the] firework");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (getParser().isCurrentEvent(PlayerElytraBoostEvent.class)) {
			Skript.error("You can only use the 'will consume firework' condition in the 'on elytra boost' event!");
			return false;
		}
		setNegated(parseResult.hasTag("not"));
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof PlayerElytraBoostEvent))
			return false;
		return isNegated() && !((PlayerElytraBoostEvent) event).shouldConsume();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "will " + (isNegated() ? " not " : "") + "consume the firework";
	}

}
