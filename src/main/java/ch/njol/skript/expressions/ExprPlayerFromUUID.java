package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExprPlayerFromUUID extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprPlayerFromUUID.class, Object.class, ExpressionType.SIMPLE,
			"[:offline[ ]]player[s] from %uuids%");
	}

	private Expression<UUID> uuids;
	private boolean offline;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		uuids = (Expression<UUID>) expressions[0];
		offline = parseResult.hasTag("offline");
		return true;
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		List<Object> players = new ArrayList<>();

		for (UUID uuid : uuids.getArray(event)) {
			if (offline) {
				players.add(Bukkit.getOfflinePlayer(uuid));
				continue;
			}

			Player player = Bukkit.getPlayer(uuid);
			if (player != null)
				players.add(player);
		}

		if (offline)
			return players.toArray(new OfflinePlayer[0]);
		return players.toArray(new Player[0]);
	}

	@Override
	public boolean isSingle() {
		return uuids.isSingle();
	}

	@Override
	public Class<?> getReturnType() {
		if (offline)
			return OfflinePlayer.class;
		return Player.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (offline ? "offline " : "") + "player from " + uuids.toString(event, debug);
	}

}
