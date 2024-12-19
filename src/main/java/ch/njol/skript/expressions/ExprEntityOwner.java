package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.UUIDUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Tameable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Name("Entity Owner")
@Description({
	"The owner of a tameable entity (i.e. horse or wolf) or a dropped item.",
	"NOTES:",
	"Getting the owner of a dropped item will only return a loaded entity or a player that has played before. "
	    + "If the entity was killed, or the player has never played before, will return null.",
	"Setting the owner of a dropped item means only that entity or player can pick it up. "
		+ "This is UUID based, so it can also be set to a specific UUID.",
	"Dropping an item does not automatically make the entity or player the owner."
})
@Examples({
	"set owner of target entity to player",
	"delete owner of target entity",
	"set {_t} to uuid of tamer of target entity",
	"",
	"set the owner of all dropped items to player"
})
@Since("2.5, INSERT VERSION (dropped items)")
public class ExprEntityOwner extends SimplePropertyExpression<Entity, Object> {

	static {
		Skript.registerExpression(ExprEntityOwner.class, Object.class, ExpressionType.PROPERTY,
			"[the] (owner|tamer) of %livingentities%",
			"%livingentities%'[s] (owner|tamer)",
			"[the] [dropped item] owner of %itementities%",
			"%itementities%'[s] [dropped item] owner");
	}

	@Override
	public @Nullable Object convert(Entity entity) {
		if (entity instanceof Tameable tameable && tameable.isTamed()) {
			return tameable.getOwner();
		} else if (entity instanceof Item item) {
			UUID uuid = item.getOwner();
			if (uuid == null)
				return null;
			return UUIDUtils.fromUUID(uuid, true);
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET -> CollectionUtils.array(OfflinePlayer.class, Entity.class, String.class);
			default -> null;
		};
	}
	
	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		UUID newId = null;
		OfflinePlayer newPlayer = null;
		if (delta != null) {
			if (delta[0] instanceof OfflinePlayer offlinePlayer) {
				newPlayer = offlinePlayer;
				newId = offlinePlayer.getUniqueId();
			} else {
				newId = UUIDUtils.asUUID(delta[0]);
			}
		}

		for (Entity entity : getExpr().getArray(event)) {
			if (entity instanceof Tameable tameable) {
				tameable.setOwner(newPlayer);
			} else if (entity instanceof Item item) {
				item.setOwner(newId);
			}
		}
	}

	@Override
	public Class<Object> getReturnType() {
		return Object.class;
	}

	@Override
	public Class<?>[] possibleReturnTypes() {
		return CollectionUtils.array(Entity.class, OfflinePlayer.class);
	}

	@Override
	protected String getPropertyName() {
		return "owner";
	}
	
}
