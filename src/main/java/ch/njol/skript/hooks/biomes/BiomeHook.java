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
package ch.njol.skript.hooks.biomes;

import java.io.IOException;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.hooks.Hook;
import ch.njol.skript.hooks.biomes.BiomeMapUtil.To19Mapping;
import ch.njol.skript.util.EnumUtils;

/**
 * Hook for using 1.13 biome names on older Minecraft versions. note that this
 * class used to allow using 1.8 biome names on 1.9 or newer.
 */
public class BiomeHook extends Hook<Skript> {
	
	@SuppressWarnings("null")
	public static BiomeHook instance;
	
	/**
	 * Used on Minecraft 1.9-1.12 to provide biome support.
	 */
	@Nullable
	public static EnumUtils<To19Mapping> util19;
	
	public static EnumUtils<To19Mapping> getUtil() {
		assert util19 != null;
		return util19;
	}

	public BiomeHook() throws IOException {}
	
	@Override
	protected boolean init() {
		instance = this;
		
		return true;
	}

	@Override
	public String getName() {
		return "Skript";
	}
	
	@SuppressWarnings("null")
	@Override
	protected void loadClasses() throws IOException {
		if (!Skript.isRunningMinecraft(1, 13)) {// Load only if running MC<1.13
			Skript.getAddonInstance().loadClasses(getClass().getPackage().getName());
			util19 = new EnumUtils<>(To19Mapping.class, "biomes");
		}
	}
	
}
