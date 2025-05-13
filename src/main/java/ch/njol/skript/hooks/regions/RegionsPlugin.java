package ch.njol.skript.hooks.regions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.hooks.Hook;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.variables.Variables;
import ch.njol.yggdrasil.ClassResolver;

/**
 * @author Peter Güttinger
 */
// REMIND support more plugins?
public abstract class RegionsPlugin<P extends Plugin> extends Hook<P> {
	
	public RegionsPlugin() throws IOException {}
	
	public static Collection<RegionsPlugin<?>> plugins = new ArrayList<>(2);
	
	static {
		Variables.yggdrasil.registerClassResolver(new ClassResolver() {
			@Override
			@Nullable
			public String getID(final Class<?> c) {
				for (final RegionsPlugin<?> p : plugins)
					if (p.getRegionClass() == c)
						return c.getClass().getSimpleName();
				return null;
			}
			
			@Override
			@Nullable
			public Class<?> getClass(final String id) {
				for (final RegionsPlugin<?> p : plugins)
					if (id.equals(p.getRegionClass().getSimpleName()))
						return p.getRegionClass();
				return null;
			}
		});
	}
	
	@Override
	protected boolean init() {
		plugins.add(this);
		return true;
	}
	
	public abstract boolean canBuild_i(Player p, Location l);
	
	public static boolean canBuild(final Player p, final Location l) {
		for (final RegionsPlugin<?> pl : plugins) {
			if (!pl.canBuild_i(p, l))
				return false;
		}
		return true;
	}
	
	public abstract Collection<? extends Region> getRegionsAt_i(Location l);
	
	public static Set<? extends Region> getRegionsAt(final Location l) {
		final Set<Region> r = new HashSet<>();
		Iterator<RegionsPlugin<?>> it = plugins.iterator();
		while (it.hasNext()) {
			RegionsPlugin<?> pl = it.next();
			try {
				r.addAll(pl.getRegionsAt_i(l));
			} catch (Throwable e) { // Unstable WorldGuard API
				Skript.error(pl.getName() + " hook crashed and was removed to prevent future errors.");
				e.printStackTrace();
				it.remove();
			}
		}
		return r;
	}
	
	@Nullable
	public abstract Region getRegion_i(World world, String name);
	
	@Nullable
	public static Region getRegion(final World world, final String name) {
		for (final RegionsPlugin<?> pl : plugins) {
			return pl.getRegion_i(world, name);
		}
		return null;
	}
	
	public abstract boolean hasMultipleOwners_i();
	
	public static boolean hasMultipleOwners() {
		for (final RegionsPlugin<?> pl : plugins) {
			if (pl.hasMultipleOwners_i())
				return true;
		}
		return false;
	}
	
	protected abstract Class<? extends Region> getRegionClass();
	
	@Nullable
	public static RegionsPlugin<?> getPlugin(final String name) {
		for (final RegionsPlugin<?> pl : plugins) {
			if (pl.getName().equalsIgnoreCase(name))
				return pl;
		}
		return null;
	}
	
}
