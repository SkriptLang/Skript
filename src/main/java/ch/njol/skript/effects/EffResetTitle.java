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

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Title - Reset")
@Description("Resets the title of the player to the default values.")
@Examples({"reset the titles of all players",
		"reset the title"})
@Since("2.3")
public class EffResetTitle extends Effect {
	
	static {
		Skript.registerEffect(EffResetTitle.class,
				"reset [the] title[s] [of %players%]",
				"reset [the] %players%'[s] title[s]");
	}
	
	@SuppressWarnings("null")
	private Expression<Player> recipients;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		recipients = (Expression<Player>) exprs[0];
		return true;
	}
	
	@Override
	protected void execute(Event e) {
		for (Player recipient : recipients.getArray(e))
			recipient.resetTitle();
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "reset the title of " + recipients.toString(e, debug);
	}
	
}
