package ch.njol.skript.expressions;

import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;

@Name("Teleport Cause")
@Description("The <a href='classes.html#teleportcause'>teleport cause</a> within a player <a href='events.html#teleport'>teleport</a> event.")
@Examples({
	"on teleport:",
		"\tteleport cause is nether portal, end portal or end gateway",
		"\tcancel event"
})
@Since("2.2-dev35")
public class ExprTeleportCause extends EventValueExpression<TeleportCause> {

	static {
		register(ExprTeleportCause.class, TeleportCause.class, "teleport (cause|reason|type)");
	}

	public ExprTeleportCause() {
		super(TeleportCause.class);
	}

}
