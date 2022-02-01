package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.Nullable;

public class ExprPlayerProtocolVersion extends SimplePropertyExpression<Player, Integer> {

	static {
		if (Skript.classExists("com.destroystokyo.paper.network.NetworkClient")) {
			register(ExprPlayerProtocolVersion.class, Integer.class, "[the] [client] protocol version", "players");
		}
	}

	@Override
	public @Nullable Integer convert(Player player) {
		return player.getProtocolVersion();
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "protocol version";
	}

}
