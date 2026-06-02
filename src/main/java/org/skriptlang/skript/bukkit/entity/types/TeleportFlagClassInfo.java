package org.skriptlang.skript.bukkit.entity.types;

import org.skriptlang.skript.bukkit.entity.types.TeleportFlagClassInfo.SkriptTeleportFlag;
import ch.njol.skript.classes.EnumClassInfo;
import io.papermc.paper.entity.TeleportFlag;
import io.papermc.paper.entity.TeleportFlag.EntityState;
import io.papermc.paper.entity.TeleportFlag.Relative;

public class TeleportFlagClassInfo extends EnumClassInfo<SkriptTeleportFlag> {

	public TeleportFlagClassInfo() {
		super(SkriptTeleportFlag.class, "teleportflag", "teleport flags");
		this.user("teleport ?flags?")
			.name("Teleport Flag")
			.description("Teleport Flags are settings to retain during a teleport.")
			.since("2.10");
	}

	public enum SkriptTeleportFlag {

		RETAIN_OPEN_INVENTORY(EntityState.RETAIN_OPEN_INVENTORY),
		RETAIN_PASSENGERS(EntityState.RETAIN_PASSENGERS),
		RETAIN_VEHICLE(EntityState.RETAIN_VEHICLE),
		RETAIN_DIRECTION(Relative.VELOCITY_ROTATION),
		RETAIN_PITCH(Relative.PITCH),
		RETAIN_YAW(Relative.YAW),
		RETAIN_MOVEMENT(Relative.VELOCITY_X, Relative.VELOCITY_Y, Relative.VELOCITY_Z),
		RETAIN_X(Relative.VELOCITY_X),
		RETAIN_Y(Relative.VELOCITY_Y),
		RETAIN_Z(Relative.VELOCITY_Z);

		final TeleportFlag[] teleportFlags;

		SkriptTeleportFlag(TeleportFlag... teleportFlags) {
			this.teleportFlags = teleportFlags;
		}

		public TeleportFlag[] getTeleportFlags() {
			return teleportFlags;
		}

	}

}
