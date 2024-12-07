package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Hidden Players")
@Description({"The players hidden from a player that were hidden using the <a href='effects.html#EffEntityVisibility'>entity visibility</a> effect."})
@Examples({"message \"&lt;light red&gt;You are currently hiding: &lt;light gray&gt;%hidden players of the player%\""})
@Since("2.3")
public class ExprHiddenPlayers extends SimpleExpression<Player> {

	static {
		Skript.registerExpression(ExprHiddenPlayers.class, Player.class, ExpressionType.PROPERTY,
				"[(all [[of] the]|the)] hidden players (of|for) %players%",
				"[(all [[of] the]|the)] players hidden (from|for|by) %players%");
	}

	@SuppressWarnings("null")
	private Expression<Player> viewers;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult result) {
		viewers = (Expression<Player>) exprs[0];
		return true;
	}

	@Nullable
	@Override
	public Player[] get(Event event) {
		List<Player> list = new ArrayList<>();
		for (Player player : viewers.getArray(event)) {
			list.addAll(player.spigot().getHiddenPlayers());
		}
		return list.toArray(new Player[0]);
	}

	@Nullable
	public Expression<Player> getViewers() {
		return viewers;
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends Player> getReturnType() {
		return Player.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "hidden players for " + viewers.toString(event, debug);
	}
}
