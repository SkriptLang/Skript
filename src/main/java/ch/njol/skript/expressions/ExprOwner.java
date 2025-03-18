package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.util.common.AnyOwner;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.AnimalTamer;
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
		return from.getOwner();
	}

	@Override
	public Class<?>[] possibleReturnTypes() {
		return CollectionUtils.array(UUID.class, LivingEntity.class, OfflinePlayer.class, Player.class, AnimalTamer.class);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
		return switch (mode) {
			case SET -> CollectionUtils.array(Object.class);
			case RESET, DELETE -> CollectionUtils.array();
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, Changer.ChangeMode mode) {
		@Nullable Class<?> deltaClass = delta == null ? null : delta[0].getClass();
		for (AnyOwner owner : getExpr().getArray(event)) {
			if (!owner.supportsChangingOwner()) {
				// TODO: throw a runtime error for an owner that doesn't support changers?
				continue;
			}
			if (deltaClass != null && !owner.isAcceptedType(deltaClass)) {
				error(owner.getErrorMessage(deltaClass));
				continue;
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
