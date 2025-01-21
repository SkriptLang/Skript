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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Name("Entity/Player from UUID")
@Description({
	"Get an entity or player from a UUID.",
	"Use 'offline' to get an offline player."
})
@Examples({
	"set {_player} to player from \"a0789aeb-7b46-43f6-86fb-cb671fed5775\" parsed as uuid",
	"set {_offline player} to offline player from {_some uuid}",
	"set {_entity} to entity from {_some uuid}"
})
@Since("INSERT VERSION")
public class ExprEntityFromUUID extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprEntityFromUUID.class, Object.class, ExpressionType.SIMPLE,
			"[:offline[ ]]player[s] from %uuids%",
			"entit(y|ies) from %uuids%"
		);
	}

	private Expression<UUID> uuids;
	private boolean offline, entity;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		uuids = (Expression<UUID>) expressions[0];
		offline = parseResult.hasTag("offline");
		entity = matchedPattern == 1;
		return true;
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		List<Object> entities = new ArrayList<>();

		for (UUID uuid : uuids.getArray(event)) {
			if (!entity) {
				if (offline) {
					entities.add(Bukkit.getOfflinePlayer(uuid));
					continue;
				}

				Player player = Bukkit.getPlayer(uuid);
				if (player != null)
					entities.add(player);

			} else {
				Entity entity = Bukkit.getEntity(uuid);
				if (entity != null)
					entities.add(entity);

			}
		}

		if (offline)
			return entities.toArray(new OfflinePlayer[0]);
		return entities.toArray(new Player[0]);
	}

	@Override
	public boolean isSingle() {
		return uuids.isSingle();
	}

	@Override
	public Class<?> getReturnType() {
		if (entity)
			return Entity.class;
		else if (offline)
			return OfflinePlayer.class;
		return Player.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		if (entity) {
			builder.append("entities");
		} else {
			if (offline)
				builder.append("offline");
			builder.append("players");
		}

		builder.append("from", uuids);

		return builder.toString();
	}

}
