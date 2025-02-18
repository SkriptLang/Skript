package ch.njol.skript.bukkitutil;

import io.papermc.paper.entity.TeleportFlag;
import io.papermc.paper.entity.TeleportFlag.EntityState;
import io.papermc.paper.entity.TeleportFlag.Relative;

/**
 * A utility enum for accessing Paper's teleport flags (1.19.4+)
 */
public enum SkriptTeleportFlag {

	RETAIN_OPEN_INVENTORY(EntityState.RETAIN_OPEN_INVENTORY),
	RETAIN_PASSENGERS(EntityState.RETAIN_PASSENGERS),
	RETAIN_VEHICLE(EntityState.RETAIN_VEHICLE),
	RETAIN_DIRECTION(Relative.PITCH, Relative.YAW),
	RETAIN_PITCH(Relative.PITCH),
	RETAIN_YAW(Relative.YAW),
	RETAIN_MOVEMENT(Relative.X, Relative.Y, Relative.Z),
	RETAIN_X(Relative.X),
	RETAIN_Y(Relative.Y),
	RETAIN_Z(Relative.Z);

	final TeleportFlag[] teleportFlags;

	SkriptTeleportFlag(TeleportFlag... teleportFlags) {
		this.teleportFlags = teleportFlags;
	}

	public TeleportFlag[] getTeleportFlags() {
		return teleportFlags;
	}

}
