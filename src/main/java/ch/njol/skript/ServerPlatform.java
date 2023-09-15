/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript;

/**
 * Represents all server platforms that Skript runs on. Only some of the
 * platforms are "officially" supported, though.
 */
public enum ServerPlatform {

	/**
	 * Unknown Bukkit revision. This is probably a bad thing...
	 */
	BUKKIT_UNKNOWN("Unknown Bukkit", false, false),

	/**
	 * CraftBukkit, but not Spigot or Paper.
	 */
	BUKKIT_CRAFTBUKKIT("CraftBukkit", false, false),

	/**
	 * Spigot, with its Bukkit API extensions. Officially supported.
	 */
	BUKKIT_SPIGOT("Spigot", true, true),

	/**
	 * Paper Minecraft server, which is a Spigot fork with additional features.
	 * Officially supported.
	 */
	BUKKIT_PAPER("Paper", true, true),

	/**
	 * Glowstone (or similar) fully open source Minecraft server, which
	 * supports Spigot API.
	 */
	BUKKIT_GLOWSTONE("Glowstone", true, false),

	/**
	 * Doesn't work at all currently.
	 */
	SPONGE("Sponge", false, false);

	public String name;
	public boolean works;
	public boolean supported;

	/**
	 *
	 * @param name Display name for platform.
	 * @param works If the platform usually works.
	 * @param supported If the platform is supported.
	 */
	ServerPlatform(String name, boolean works, boolean supported) {
		this.name = name;
		this.works = works;
		this.supported = supported;
	}
}
