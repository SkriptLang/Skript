package ch.njol.skript.expressions;

import org.bukkit.event.player.PlayerRespawnEvent.RespawnReason;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;

@Name("Respawn Cause")
@Description("The <a href='#respawnreason'>respawn cause</a> within a <a href='#respawn'>respawn</a> event.")
@Example("""
    on respawn:
    	if respawn cause is end portal:
    		broadcast "%player% hath traversed the end portal unto the overworld!"
    """)
@Since("2.14")
public class ExprRespawnReason extends EventValueExpression<RespawnReason> {

	static {
		register(ExprRespawnReason.class, RespawnReason.class, "respawn[ing] cause");
	}

	public ExprRespawnReason() {
		super(RespawnReason.class);
	}

}
