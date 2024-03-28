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
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Elytra Boost Consume")
@Description("Change if the firework will be consumed in an <a href='events.html#elytra_boost'>elytra boost event</a>.")
@Examples({
	"on player elytra boost:",
		"\twill consume firework",
		"\tset to consume the firework"
})
@RequiredPlugins("Paper")
@Since("INSERT VERSION")
public class EffElytraBoostConsume extends Effect {

	static {
		if (Skript.classExists("com.destroystokyo.paper.event.player.PlayerElytraBoostEvent"))
			Skript.registerEffect(EffElytraBoostConsume.class, "[not:do not] consume firework", "set consum(e|ption of) firework to %boolean%");
	}

	@Nullable
	private Expression<Boolean> expression;
	private boolean consume;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (getParser().isCurrentEvent(PlayerElytraBoostEvent.class)) {
			Skript.error("You can only use the 'consume firework' effect in the 'on elytra boost' event!");
			return false;
		}
		if (matchedPattern == 1) {
			expression = (Expression<Boolean>) exprs[0];
		} else {
			consume = !parseResult.hasTag("not");
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (!(event instanceof PlayerElytraBoostEvent))
			return;
		PlayerElytraBoostEvent elytraBoostEvent = (PlayerElytraBoostEvent) event;
		if (expression != null) {
			Boolean consume = expression.getSingle(event);
			if (consume == null)
				return;
			elytraBoostEvent.setShouldConsume(consume);
		} else {
			elytraBoostEvent.setShouldConsume(consume);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return expression == null ? (consume ? "consume" : "do not consume") + " firework" : "set consume firework to " + expression.toString(event, debug);
	}

}
