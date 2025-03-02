package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Name("Entity/Player/World from UUID")
@Description({
	"Get an entity, player, offline player or world from a UUID.",
	"Unloaded entities or players that are offline (when using 'player from %uuid%') will return nothing."
})
@Examples({
	"set {_player} to player from \"a0789aeb-7b46-43f6-86fb-cb671fed5775\" parsed as uuid",
	"set {_offline player} to offline player from {_some uuid}",
	"set {_entity} to entity from {_some uuid}",
	"set {_world} to world from {_some uuid}"
})
@Since("INSERT VERSION")
public class ExprFromUUID extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprFromUUID.class, Object.class, ExpressionType.PROPERTY,
			"[:offline[ ]]player[s] from %uuids%",
			"entit(y|ies) from %uuids%",
			"world[s] from %uuids%"
		);
	}

	private Expression<UUID> uuids;
	private boolean player, offline, world;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		uuids = (Expression<UUID>) exprs[0];
		player = matchedPattern == 0;
		offline = parseResult.hasTag("offline");
		world = matchedPattern == 2;
		return true;
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		List<Object> entities = new ArrayList<>();

		for (UUID uuid : uuids.getArray(event)) {
			if (player) {
				if (offline) {
					entities.add(Bukkit.getOfflinePlayer(uuid));
					continue;
				}

				Player player = Bukkit.getPlayer(uuid);
				if (player != null)
					entities.add(player);

			} else if (world) {
				World world = Bukkit.getWorld(uuid);
				if (world != null)
					entities.add(world);

			} else {
				Entity entity = Bukkit.getEntity(uuid);
				if (entity != null)
					entities.add(entity);
			}
		}

		if (player) {
			if (offline)
				//noinspection SuspiciousToArrayCall
				return entities.toArray(OfflinePlayer[]::new);
			//noinspection SuspiciousToArrayCall
			return entities.toArray(Player[]::new);
		}

		if (world)
			//noinspection SuspiciousToArrayCall
			return entities.toArray(World[]::new);
		//noinspection SuspiciousToArrayCall
		return entities.toArray(Entity[]::new);
	}

	@Override
	public boolean isSingle() {
		return uuids.isSingle();
	}

	@Override
	public Class<?> getReturnType() {
		if (world) {
			return World.class;
		} else if (player) {
			if (offline)
				return OfflinePlayer.class;
			return Player.class;
		}

		return Entity.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		if (world) {
			builder.append("worlds");
		} else if (player) {
			if (offline)
				builder.append("offline");
			builder.append("players");
		} else {
			builder.append("entities");
		}

		builder.append("from", uuids);

		return builder.toString();
	}

}
