/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.bukkitutil;

import io.papermc.paper.entity.TeleportFlag;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A utility interface to access the Entity::teleport with TeleportFlag vararg in Paper 1.19+.
 */
@FunctionalInterface
public interface TeleportFlags {

	public enum SkriptTeleportFlag {
		RETAIN_OPEN_INVENTORY(TeleportFlag.EntityState.RETAIN_OPEN_INVENTORY),
		RETAIN_PASSENGERS(TeleportFlag.EntityState.RETAIN_PASSENGERS),
		RETAIN_VEHICLE(TeleportFlag.EntityState.RETAIN_VEHICLE),
		RETAIN_DIRECTION(),
		RETAIN_PITCH(TeleportFlag.Relative.PITCH),
		RETAIN_YAW(TeleportFlag.Relative.YAW),
		RETAIN_X(TeleportFlag.Relative.X),
		RETAIN_Y(TeleportFlag.Relative.Y),
		RETAIN_Z(TeleportFlag.Relative.Z);

		TeleportFlag teleportFlag;

		SkriptTeleportFlag() {}

		SkriptTeleportFlag(TeleportFlag teleportFlag) {
			this.teleportFlag = teleportFlag;
		}

		@Nullable
		public TeleportFlag getTeleportFlag() {
			return teleportFlag;
		}

	}

	void teleport(
		@NotNull Entity entity, @NotNull Location location, @NotNull TeleportFlag... flags
	);

	static void teleport(
		@NotNull TeleportFlags teleportFlagsInterface, @NotNull Entity entity, @NotNull Location location, @NotNull TeleportFlag... teleportFlags
	) {
		if (location.getWorld() == null) {
			location = location.clone();
			location.setWorld(entity.getWorld());
		}

		teleportFlagsInterface.teleport(entity, location, teleportFlags);
	}

}
