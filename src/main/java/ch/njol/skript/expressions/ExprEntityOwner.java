package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Name("Entity Owner")
@Description({
	"The owner of a tameable entity (i.e. horse or wolf) or a dropped item.",
	"NOTES:",
	"Getting the owner of a dropped item will only return an alive entity or a player that has played before. "
	    + "If the entity was killed, or the player has never played before, will return null.",
	"Setting the owner of a dropped item means only that entity or player can pick it up.",
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
			"[the] %livingentities%'[s] (owner|tamer)",
			"[the] [dropped item] owner of %itementities%",
			"[the] %itementities%'[s] [dropped item] owner");
	}

	private boolean useTameable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		useTameable = matchedPattern <= 1;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Object convert(Entity entity) {
		if (entity instanceof Tameable tameable && tameable.isTamed()) {
			return tameable.getOwner();
		} else if (entity instanceof Item item) {
			UUID uuid = item.getOwner();
			if (uuid == null)
				return null;
			Entity checkEntity = Bukkit.getEntity(uuid);
			if (checkEntity != null)
				return checkEntity;
			OfflinePlayer checkPlayer = Bukkit.getOfflinePlayer(uuid);
			if (checkPlayer.hasPlayedBefore())
				return checkPlayer;
			return null;
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET -> {
				if (useTameable)
					yield CollectionUtils.array(OfflinePlayer.class);
				yield CollectionUtils.array(OfflinePlayer.class, Entity.class, String.class);
			}
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
			} else if (delta[0] instanceof Entity entity) {
				newId = entity.getUniqueId();
			} else if (delta[0] instanceof String string) {
				try {
					newId = UUID.fromString(string);
				} catch (Exception ignored) {}
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
		return CollectionUtils.array(Entity.class, OfflinePlayer.class, String.class);
	}

	@Override
	protected String getPropertyName() {
		return "owner";
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "owner of " + getExpr().toString(event, debug);
	}
	
}
