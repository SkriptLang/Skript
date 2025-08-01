package ch.njol.skript.expressions;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Last Resource Pack Response")
@Description("Returns the last resource pack response received from a player.")
@Examples("if player's last resource pack response is deny or download fail:")
@Since("2.4")
@RequiredPlugins("Paper 1.9 or newer")
public class ExprLastResourcePackResponse extends SimplePropertyExpression<Player, Status> {

	static {
		if (Skript.methodExists(Player.class, "getResourcePackStatus"))
			register(ExprLastResourcePackResponse.class, Status.class, "[last] resource pack response[s]", "players");
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
