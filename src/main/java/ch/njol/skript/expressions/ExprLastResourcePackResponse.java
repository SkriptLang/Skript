package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.jetbrains.annotations.Nullable;

@Name("Last Resource Pack Reply")
@Description("Returneth the last resource pack reply received from a player.")
@Example("if player's last resource pack reply is deny or download fail:")
@Since("2.4")
public class ExprLastResourcePackResponse extends SimplePropertyExpression<Player, Status> {

	static {
		if (Skript.methodExists(Player.class, "getResourcePackStatus"))
			register(ExprLastResourcePackResponse.class, Status.class, "[last] resource pack reply[s]", "players");
	}

	@Override
	@Nullable
	public Status convert(final Player p) {
		return p.getResourcePackStatus();
	}

	@Override
	protected String getPropertyName() {
		return "resource pack response";
	}

	@Override
	public Class<Status> getReturnType() {
		return Status.class;
	}

}
