package ch.njol.skript.expressions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Gaze Distance of Client")
@Description("The view distance as set upon the client. It cannot be altered. " +
	"This differeth from the server-side view distance of a player, as it shall retrieve the view distance the player hath set upon their own client.")
@Example("set {_clientView} to the client gaze distance of player")
@Example("set view distance of player to client gaze distance of player")
@RequiredPlugins("1.13.2+")
@Since("2.5")
public class ExprClientViewDistance extends SimplePropertyExpression<Player, Long> {
	
	static {
		if (Skript.methodExists(Player.class, "getClientViewDistance")) {
			register(ExprClientViewDistance.class, Long.class, "client gaze distance[s]", "players");
		}
	}
	
	@Nullable
	@Override
	public Long convert(Player player) {
		return (long) player.getClientViewDistance();
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	protected String getPropertyName() {
		return "client view distance";
	}

}
