package ch.njol.skript.bukkitutil;

import io.papermc.paper.entity.TeleportFlag;
import org.jetbrains.annotations.Nullable;

/**
 * A utility enum for accessing Paper's teleport flags (1.19.4+)
 */
public enum SkriptTeleportFlag {
	RETAIN_OPEN_INVENTORY(TeleportFlag.EntityState.RETAIN_OPEN_INVENTORY),
	RETAIN_PASSENGERS(TeleportFlag.EntityState.RETAIN_PASSENGERS),
	RETAIN_VEHICLE(TeleportFlag.EntityState.RETAIN_VEHICLE),
	RETAIN_DIRECTION(TeleportFlag.Relative.PITCH, TeleportFlag.Relative.YAW),
	RETAIN_PITCH(TeleportFlag.Relative.PITCH),
	RETAIN_YAW(TeleportFlag.Relative.YAW),
	RETAIN_MOVEMENT(TeleportFlag.Relative.X, TeleportFlag.Relative.Y, TeleportFlag.Relative.Z),
	RETAIN_X(TeleportFlag.Relative.X),
	RETAIN_Y(TeleportFlag.Relative.Y),
	RETAIN_Z(TeleportFlag.Relative.Z);

	final TeleportFlag[] teleportFlags;

	SkriptTeleportFlag(TeleportFlag... teleportFlags) {
		this.teleportFlags = teleportFlags;
	}

	@Nullable
	public TeleportFlag[] getTeleportFlags() {
		return teleportFlags;
	}

}
