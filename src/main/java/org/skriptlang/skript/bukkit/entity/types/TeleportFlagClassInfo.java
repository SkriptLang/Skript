package org.skriptlang.skript.bukkit.entity.types;

import ch.njol.skript.Skript;
import org.skriptlang.skript.bukkit.entity.types.TeleportFlagClassInfo.SkriptTeleportFlag;
import ch.njol.skript.classes.EnumClassInfo;
import io.papermc.paper.entity.TeleportFlag;
import io.papermc.paper.entity.TeleportFlag.EntityState;
import io.papermc.paper.entity.TeleportFlag.Relative;
import org.skriptlang.skript.lang.script.ScriptWarning;

public class TeleportFlagClassInfo extends EnumClassInfo<SkriptTeleportFlag> {

	private static final boolean IS_ENTITY_STATE_DEPRECATED = Skript.isRunningMinecraft(1, 21, 10);

	public TeleportFlagClassInfo() {
		super(SkriptTeleportFlag.class, "teleportflag", "teleport flags", flag -> {
			switch (flag) {
				case RETAIN_PITCH, RETAIN_YAW -> ScriptWarning.printDeprecationWarning(
					"It is no longer possible to individually retain yaw or pitch velocities." +
					" Use 'yaw and pitch velocity' to retain both instead.");
				case RETAIN_OPEN_INVENTORY -> {
					if (IS_ENTITY_STATE_DEPRECATED) {
						ScriptWarning.printDeprecationWarning(
							"Inventories are no longer closed on teleportation, meaning this flag is no longer necessary.");
					}
				}
				case RETAIN_PASSENGERS -> {
					if (IS_ENTITY_STATE_DEPRECATED) {
						ScriptWarning.printDeprecationWarning(
							"Passengers are no longer removed on teleportation, meaning this flag is no longer necessary.");
					}
				}
				case RETAIN_VEHICLE -> {
					if (IS_ENTITY_STATE_DEPRECATED) {
						ScriptWarning.printDeprecationWarning(
							"The 'retain vehicle' flag has no functionality on modern versions due to technical limitations with the client.");
					}
				}
			}
		});
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
