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
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.util.AABB;
import ch.njol.skript.variables.Variables;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilID;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class WorldGuardHook extends RegionsPlugin<WorldGuardPlugin> {

	public WorldGuardHook() {
		super("WorldGuard");
	}

	@Override
	public boolean init() {
		if (!Skript.classExists("com.sk89q.worldedit.math.BlockVector3")) {
			Skript.error("WorldEdit you're using is not compatible with Skript. Disabling WorldGuard support!");
			return false;
		}

		return super.init();
	}
	
	@Override
	public boolean canBuild_i(Player player, Location location) {
		if (player.hasPermission("worldguard.region.bypass." + location.getWorld().getName()))
			return true; // Build access always granted by permission
		WorldGuardPlatform platform = WorldGuard.getInstance().getPlatform();
		RegionQuery query = platform.getRegionContainer().createQuery();
		return query.testBuild(BukkitAdapter.adapt(location), plugin().wrapPlayer(player));
	}

	@Override
	public Collection<? extends Region> getRegionsAt_i(@Nullable Location location) {
		List<Region> regions = new ArrayList<>();

		if (location == null) // Working around possible cause of issue #280
			return Collections.emptyList();
		if (location.getWorld() == null)
			return Collections.emptyList();

		WorldGuardPlatform platform = WorldGuard.getInstance().getPlatform();
		RegionManager manager = platform.getRegionContainer().get(BukkitAdapter.adapt(location.getWorld()));
		if (manager == null)
			return regions;
		ApplicableRegionSet applicable = manager.getApplicableRegions(BukkitAdapter.asBlockVector(location));
		for (ProtectedRegion region : applicable)
			regions.add(new WorldGuardRegion(location.getWorld(), region));
		return regions;
	}

	@Override
	@Nullable
	public Region getRegion_i(World world, String name) {
		WorldGuardPlatform platform = WorldGuard.getInstance().getPlatform();
		RegionManager manager = platform.getRegionContainer()
			.get(BukkitAdapter.adapt(world));
		ProtectedRegion region = manager == null ? null : manager.getRegion(name);
		if (region != null)
			return new WorldGuardRegion(world, region);
		return null;
	}

	@Override
	public boolean hasMultipleOwners_i() {
		return true;
	}

	@Override
	protected Class<? extends Region> getRegionClass() {
		return WorldGuardRegion.class;
	}
	
	static {
		Variables.yggdrasil.registerSingleClass(WorldGuardRegion.class);
	}
	
	@YggdrasilID("WorldGuardRegion")
	public final class WorldGuardRegion extends Region {

		@UnknownNullability
		private transient ProtectedRegion region;
		@UnknownNullability
		final World world;

		@SuppressWarnings("unused")
		private WorldGuardRegion() {
			world = null;
		}
		
		public WorldGuardRegion(World world, ProtectedRegion region) {
			this.world = world;
			this.region = region;
		}
		
		@Override
		public boolean contains(Location location) {
			return location.getWorld().equals(world) && region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
		}
		
		@Override
		public boolean isMember(OfflinePlayer player) {
			return region.isMember(plugin().wrapOfflinePlayer(player));
		}
		
		@Override
		public Collection<OfflinePlayer> getMembers() {
			Collection<UUID> uuids = region.getMembers().getUniqueIds();
			Collection<OfflinePlayer> players = new ArrayList<>(uuids.size());
			for (UUID uuid : uuids)
				players.add(Bukkit.getOfflinePlayer(uuid));
			return players;
		}
		
		@Override
		public boolean isOwner(OfflinePlayer player) {
			return region.isOwner(plugin().wrapOfflinePlayer(player));
		}
		
		@Override
		public Collection<OfflinePlayer> getOwners() {
			Collection<UUID> uuids = region.getOwners().getUniqueIds();
			Collection<OfflinePlayer> players = new ArrayList<>(uuids.size());
			for (UUID uuid : uuids)
				players.add(Bukkit.getOfflinePlayer(uuid));
			return players;
		}
		
		@Override
		public Iterator<Block> getBlocks() {
			final BlockVector3 min = region.getMinimumPoint(), max = region.getMaximumPoint();
			return new AABB(world, new Vector(min.getBlockX(), min.getBlockY(), min.getBlockZ()),
					new Vector(max.getBlockX(), max.getBlockY(), max.getBlockZ())).iterator();
		}
		
		@Override
		public Fields serialize() throws NotSerializableException {
			Fields fields = new Fields(this);
			fields.putObject("region", region.getId());
			return fields;
		}
		
		@Override
		public void deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
			String name = fields.getAndRemoveObject("region", String.class);
			fields.setFields(this);
			if (name == null)
				throw new StreamCorruptedException("Region is null");

			WorldGuardPlatform platform = WorldGuard.getInstance().getPlatform();
			ProtectedRegion region = platform.getRegionContainer().get(BukkitAdapter.adapt(world)).getRegion(name);
			if (region == null)
				throw new StreamCorruptedException("Invalid region " + name + " in world " + world);
			this.region = region;
		}
		
		@Override
		public String toString() {
			return region.getId() + " in world " + world.getName();
		}
		
		@Override
		public RegionsPlugin<WorldGuardPlugin> getPlugin() {
			return WorldGuardHook.this;
		}
		
		@Override
		public boolean equals(@Nullable Object other) {
			if (other == this)
				return true;
			if (other == null)
				return false;
			if (!(other instanceof WorldGuardRegion))
				return false;
			return world.equals(((WorldGuardRegion) other).world) && region.equals(((WorldGuardRegion) other).region);
		}
		
		@Override
		public int hashCode() {
			return world.hashCode() * 31 + region.hashCode();
		}
	}
	
}
