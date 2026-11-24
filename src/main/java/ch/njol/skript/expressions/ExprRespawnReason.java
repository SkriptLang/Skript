package ch.njol.skript.expressions;

import org.bukkit.event.player.PlayerRespawnEvent.RespawnReason;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;

@Name("Respawn Reason")
@Description("The [reason](docs:Skript/TypeRespawnreason) in a [respawn](docs:Skript/EvtRespawn) event.")
@Example("""
	on respawn:
		if respawn reason is end portal:
			broadcast "%player% took the end portal to the overworld!"
	""")
@Since("2.14")
public class ExprRespawnReason extends EventValueExpression<RespawnReason> {

	static {
		register(ExprRespawnReason.class, RespawnReason.class, "respawn[ing] reason");
	}

	public ExprRespawnReason() {
		super(RespawnReason.class);
	}

}
