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
package ch.njol.skript.expressions;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerQuitEvent.QuitReason;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.registrations.EventValues;

@Name("Quit Reason")
@Description("The <a href='classes.html#quitreason'>quit reason</a> as to why a player disconnected in a <a href='events.html#quit'>quit</a> event.")
@Examples({
	"on quit:",
		"\tquit reason was kicked",
		"\tplayer is banned",
		"\tclear {server::player::%uuid of player%::*}"
})
@RequiredPlugins("Paper 1.16.5+")
@Since("INSERT VERSION")
public class ExprQuitReason extends EventValueExpression<QuitReason> {

	static {
		if (Skript.classExists("org.bukkit.event.player.PlayerQuitEvent$QuitReason"))
			Skript.registerExpression(ExprQuitReason.class, QuitReason.class, ExpressionType.SIMPLE, "[the] (quit|disconnect) (cause|reason)");
	}

	public ExprQuitReason() {
		super(QuitReason.class);
	}

	// Allow for 'the quit reason was ...' as that's proper grammar support for this event value.
	@Override
	public boolean setTime(int time) {
		if (time == EventValues.TIME_FUTURE)
			return super.setTime(time);
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "quit reason";
	}

}
