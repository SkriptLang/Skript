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

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.command.ScriptCommandEvent;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;

@Name("Cancel Command Cooldown")
@Description({"Only usable in commands. Makes it so the current command usage isn't counted towards the cooldown."})
@Examples({
		"command /nick &lt;text&gt;:",
		"\texecutable by: players",
		"\tcooldown: 10 seconds",
		"\ttrigger:",
		"\t\tif length of arg-1 is more than 16:",
		"\t\t\t# Makes it so that invalid arguments don't make you wait for the cooldown again",
		"\t\t\tcancel the cooldown",
		"\t\t\tsend \"Your nickname may be at most 16 characters.\"",
		"\t\t\tstop",
		"\t\tset the player's display name to arg-1"})
@Since("2.2-dev34")
public class EffCancelCooldown extends Effect {

	static {
		Skript.registerEffect(EffCancelCooldown.class,
				"(cancel|ignore) [the] [current] [command] cooldown",
				"un(cancel|ignore) [the] [current] [command] cooldown");
	}

	private boolean cancel;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!getParser().isCurrentEvent(ScriptCommandEvent.class)) {
			Skript.error("The cancel cooldown effect may only be used in a command", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		cancel = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event e) {
		if (!(e instanceof ScriptCommandEvent))
			return;

		((ScriptCommandEvent) e).setCooldownCancelled(cancel);
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return (cancel ? "" : "un") + "cancel the command cooldown";
	}

}
