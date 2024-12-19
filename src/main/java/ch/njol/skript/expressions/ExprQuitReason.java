package ch.njol.skript.expressions;

import org.bukkit.event.player.PlayerQuitEvent.QuitReason;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
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
@Since("2.8.0")
public class ExprQuitReason extends EventValueExpression<QuitReason> {

	static {
		if (Skript.classExists("org.bukkit.event.player.PlayerQuitEvent$QuitReason"))
			register(ExprQuitReason.class, QuitReason.class, "(quit|disconnect) (cause|reason)");
	}

	public ExprQuitReason() {
		super(QuitReason.class);
	}

	@Override
	public boolean setTime(int time) {
		return time != EventValues.TIME_FUTURE; // allow past and present
	}

}
