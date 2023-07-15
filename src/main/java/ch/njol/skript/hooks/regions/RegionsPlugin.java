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
package ch.njol.skript.hooks.regions;

import ch.njol.skript.Skript;
import ch.njol.skript.hooks.SimpleHook;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.variables.Variables;
import ch.njol.yggdrasil.ClassResolver;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class RegionsPlugin<T extends Plugin> extends SimpleHook<T> {

	private static final List<RegionsPlugin<?>> PLUGINS = new ArrayList<>();

	static {
		Variables.yggdrasil.registerClassResolver(new ClassResolver() {
			@Override
			@Nullable
			public String getID(Class<?> type) {
				for (RegionsPlugin<?> plugin : PLUGINS) {
					if (plugin.getRegionClass() == type)
						return type.getSimpleName();
				}
				return null;
			}

			@Override
			@Nullable
			public Class<?> getClass(String id) {
				for (RegionsPlugin<?> plugin : PLUGINS) {
					if (id.equals(plugin.getRegionClass().getSimpleName()))
						return plugin.getRegionClass();
				}

				return null;
			}
		});
	}

	public RegionsPlugin(String name) {
		super(name);
	}

	@Override
	public boolean init() {
		PLUGINS.add(this);
		return true;
	}

	public abstract boolean canBuild_i(Player player, Location location);

	public static boolean canBuild(Player player, Location location) {
		for (RegionsPlugin<?> plugin : PLUGINS) {
			if (!plugin.canBuild_i(player, location))
				return false;
		}

		return true;
	}

	public abstract Collection<? extends Region> getRegionsAt_i(Location location);

	public static Set<? extends Region> getRegionsAt(Location location) {
		Set<Region> regions = new HashSet<>();
		Iterator<RegionsPlugin<?>> iterator = PLUGINS.iterator();
		while (iterator.hasNext()) {
			RegionsPlugin<?> plugin = iterator.next();

			try {
				regions.addAll(plugin.getRegionsAt_i(location));
			} catch (Throwable e) { // Unstable WorldGuard API
				Skript.error(plugin.getName() + " hook crashed and was removed to prevent future errors.");
				e.printStackTrace();
				iterator.remove();
			}
		}

		return regions;
	}

	@Nullable
	public abstract Region getRegion_i(World world, String name);

	@Nullable
	public static Region getRegion(World world, String name) {
		for (RegionsPlugin<?> plugin : PLUGINS) {
			return plugin.getRegion_i(world, name);
		}

		return null;
	}

	public abstract boolean hasMultipleOwners_i();

	public static boolean hasMultipleOwners() {
		for (RegionsPlugin<?> plugin : PLUGINS) {
			if (plugin.hasMultipleOwners_i())
				return true;
		}

		return false;
	}

	protected abstract Class<? extends Region> getRegionClass();

	@Nullable
	public static RegionsPlugin<?> getPlugin(String name) {
		for (RegionsPlugin<?> plugin : PLUGINS) {
			if (plugin.getName().equalsIgnoreCase(name))
				return plugin;
		}

		return null;
	}

}
