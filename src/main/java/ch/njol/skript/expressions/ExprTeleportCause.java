package ch.njol.skript.expressions;

import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;

@Name("Cause of Translocation")
@Description("The <a href='#teleportcause'>translocation cause</a> within a player <a href='#teleport'>teleport</a> event.")
@Example("""
    on teleport:
    	translocation cause is nether portal, end portal or end gateway
    	cancel event
    """)
@Since("2.2-dev35")
public class ExprTeleportCause extends EventValueExpression<TeleportCause> {

	static {
		register(ExprTeleportCause.class, TeleportCause.class, "translocation (cause|reason|type)");
	}

	public ExprTeleportCause() {
		super(TeleportCause.class);
	}

}
