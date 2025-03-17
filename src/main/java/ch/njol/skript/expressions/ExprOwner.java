package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.util.common.AnyOwner;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
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
		return CollectionUtils.array(UUID.class, LivingEntity.class, OfflinePlayer.class, Player.class);
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
				continue;
			}
//			Bukkit.broadcastMessage("Delta: " + (delta == null ? "unknown" : delta[0]));
//			Bukkit.broadcastMessage("DeltaClas: " + deltaClass);
//			Bukkit.broadcastMessage("OwnerType: " + owner.getOwnerType());
//			Bukkit.broadcastMessage("ReturnType: " + owner.getReturnType());
//			Bukkit.broadcastMessage("Owner: " + owner.getOwner());
//			Bukkit.broadcastMessage("owner ClassInfo: " + Classes.getSuperClassInfo(owner.getReturnType()));
//			if (deltaClass != null) {
//				Bukkit.broadcastMessage("delta ClassInfo: " + Classes.getSuperClassInfo(deltaClass));
//				Bukkit.broadcastMessage("isAssignableFrom: " + owner.getReturnType().isAssignableFrom(deltaClass));
//			}
			if (deltaClass != null && !owner.getReturnType().isAssignableFrom(deltaClass)) {
				error("The owner of " + Utils.a(owner.getOwnerType()) + " cannot be set to " + Utils.a(Classes.getSuperClassInfo(deltaClass).toString())
					+ ", it must be set to " + Utils.a(Classes.getSuperClassInfo(owner.getReturnType()).toString()) + ".");
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
