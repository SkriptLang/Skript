package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.common.AnyOwner;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@SuppressWarnings("rawtypes")
public class ExprOwner extends SimplePropertyExpression<AnyOwner, Object> {

	static {
		register(ExprOwner.class, Object.class, "owner[s]", "ownables");
	}

	@Override
	public @Nullable Object convert(AnyOwner from) {
		Bukkit.broadcastMessage("ExprOwner#convert was called");
		return from.getOwner();
	}

	@Override
	public Class<?>[] possibleReturnTypes() {
		return CollectionUtils.array(UUID.class, LivingEntity.class, OfflinePlayer.class, Player.class);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
		return switch (mode) {
			case SET -> CollectionUtils.array(OfflinePlayer.class, UUID.class);
			case DELETE -> CollectionUtils.array();
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, Changer.ChangeMode mode) {
		Bukkit.broadcastMessage("ExprOwner#change was called");
		for (AnyOwner owner : getExpr().getArray(event)) {
			if (!owner.supportsChangingOwner()) {
				Bukkit.broadcastMessage("Unable to find a support change");
				continue;
			}
			if (delta != null && !owner.supportsChangeValue(delta[0].getClass())) {
				Bukkit.broadcastMessage("Unable to find a safe cast, " + delta[0].getClass().getName());
				continue;
			}
			if (delta == null) {
				Bukkit.broadcastMessage("AnyOwner#setOwner was called with delta as null");
			} else {
				Bukkit.broadcastMessage("AnyOwner#setOwner was called with delta[0] " +  delta[0].getClass().getName());
			}
			owner.setOwner(delta == null ? null : delta[0]);
		}
	}

	@Override
	public Class<?> getReturnType() {
		return Object.class;
	}

	@Override
	protected String getPropertyName() {
		return "owner";
	}
}
