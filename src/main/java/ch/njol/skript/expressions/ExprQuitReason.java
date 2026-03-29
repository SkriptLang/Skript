package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.registrations.EventValues;
import org.bukkit.event.player.PlayerQuitEvent.QuitReason;

@Name("Departure Reason")
@Description("The <a href='#quitreason'>reason of departure</a> wherefore a player did disconnect in a <a href='#quit'>quit</a> event.")
@Example("""
	on quit:
		quit reason was kicked
		player is banned
		clear {server::player::%uuid of player%::*}
	""")
@Since("2.8.0")
public class ExprQuitReason extends EventValueExpression<QuitReason> {

	static {
		// TODO - remove this when Spigot support is dropped
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
