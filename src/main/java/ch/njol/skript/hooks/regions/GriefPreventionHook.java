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
import ch.njol.util.coll.iterator.EmptyIterator;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilID;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.io.StreamCorruptedException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class GriefPreventionHook extends RegionsPlugin<GriefPrevention> {

	boolean supportsUUIDs;
	@Nullable
	Method getClaim;
	@Nullable
	Field claimsField;

	public GriefPreventionHook() {
		super("GriefPrevention");
	}

	@Override
	public boolean init() {
		// ownerID is a public field
		supportsUUIDs = Skript.fieldExists(Claim.class, "ownerID");
		try {
			getClaim = DataStore.class.getDeclaredMethod("getClaim", long.class);
			getClaim.setAccessible(true);
			if (!Claim.class.isAssignableFrom(getClaim.getReturnType()))
				getClaim = null;
		} catch (NoSuchMethodException | SecurityException ignored) {}
		try {
			claimsField = DataStore.class.getDeclaredField("claims");
			claimsField.setAccessible(true);
			if (!List.class.isAssignableFrom(claimsField.getType()))
				claimsField = null;
		} catch (NoSuchFieldException | SecurityException ignored) {}
		if (getClaim == null && claimsField == null) {
			Skript.error("Skript " + Skript.getVersion() + " is not compatible with " + getName() + " " + plugin().getDescription().getVersion() + "."
					+ " Please report this at https://github.com/SkriptLang/Skript/issues/ if this error occurred after you updated GriefPrevention.");
			return false;
		}
		return super.init();
	}

	@Override
	public boolean canBuild_i(Player player, Location location) {
		return plugin().allowBuild(player, location) == null; // returns reason string if not allowed to build
	}

	@Override
	public Collection<? extends Region> getRegionsAt_i(Location location) {
		Claim claim = plugin().dataStore.getClaimAt(location, false, null);
		if (claim != null)
			return Collections.singletonList(new GriefPreventionRegion(claim));
		return Collections.emptySet();
	}

	@Override
	@Nullable
	public Region getRegion_i(World world, String name) {
		try {
			Claim claim = getClaim(Long.parseLong(name));
			if (claim != null && world.equals(claim.getLesserBoundaryCorner().getWorld()))
				return new GriefPreventionRegion(claim);
			return null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public boolean hasMultipleOwners_i() {
		return false;
	}

	@Override
	protected Class<? extends Region> getRegionClass() {
		return GriefPreventionRegion.class;
	}

	@Nullable
	Claim getClaim(long id) {
		GriefPrevention plugin = plugin();

		if (getClaim != null) {
			try {
				return (Claim) getClaim.invoke(plugin.dataStore, id);
			} catch (IllegalAccessException | IllegalArgumentException e) {
				assert false : e;
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e.getCause());
			}
		} else {
			assert claimsField != null;
			try {
				List<?> claims = (List<?>) claimsField.get(plugin.dataStore);
				for (Object claim : claims) {
					if (!(claim instanceof Claim))
						continue;
					if (((Claim) claim).getID() == id)
						return (Claim) claim;
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				assert false : e;
			}
		}

		return null;
	}

	static {
		Variables.yggdrasil.registerSingleClass(GriefPreventionRegion.class);
	}

	@YggdrasilID("GriefPreventionRegion")
	public final class GriefPreventionRegion extends Region {

		@UnknownNullability
		private transient Claim claim;

		@SuppressWarnings("unused")
		private GriefPreventionRegion() {}

		public GriefPreventionRegion(Claim claim) {
			this.claim = claim;
		}

		@Override
		public boolean contains(Location location) {
			return claim.contains(location, false, false);
		}

		@Override
		public boolean isMember(OfflinePlayer player) {
			return isOwner(player);
		}

		@Override
		public Collection<OfflinePlayer> getMembers() {
			return getOwners();
		}

		@Override
		public boolean isOwner(OfflinePlayer player) {
			String name = player.getName();
			if (name != null)
				return name.equalsIgnoreCase(claim.getOwnerName());
			return false; // Assume no ownership when player has never visited server
		}

		@Override
		public Collection<OfflinePlayer> getOwners() {
			if (claim.isAdminClaim() || (supportsUUIDs && claim.ownerID == null)) // Not all claims have owners!
				return Collections.emptyList();
			else if (supportsUUIDs)
				return Collections.singletonList(Bukkit.getOfflinePlayer(claim.ownerID));
			else
				return Collections.singletonList(Bukkit.getOfflinePlayer(claim.getOwnerName()));
		}
		
		@Override
		public Iterator<Block> getBlocks() {
			final Location lower = claim.getLesserBoundaryCorner(), upper = claim.getGreaterBoundaryCorner();
			if (lower == null || upper == null || lower.getWorld() == null || upper.getWorld() == null || lower.getWorld() != upper.getWorld())
				return EmptyIterator.get();
			upper.setY(upper.getWorld().getMaxHeight() - 1);
			upper.setX(upper.getBlockX());
			upper.setZ(upper.getBlockZ());
			return new AABB(lower, upper).iterator();
		}
		
		@Override
		public String toString() {
			return "Claim #" + claim.getID();
		}

		@Override
		public Fields serialize() {
			Fields fields = new Fields();
			fields.putPrimitive("id", claim.getID());
			return fields;
		}

		@Override
		public void deserialize(Fields fields) throws StreamCorruptedException {
			long id = fields.getPrimitive("id", long.class);
			Claim claim = getClaim(id);
			if (claim == null)
				throw new StreamCorruptedException("Invalid claim " + id);
			this.claim = claim;
		}

		@Override
		public RegionsPlugin<GriefPrevention> getPlugin() {
			return GriefPreventionHook.this;
		}

		@Override
		public boolean equals(@Nullable Object other) {
			if (other == this)
				return true;
			if (other == null)
				return false;
			if (!(other instanceof GriefPreventionRegion))
				return false;
			return claim.equals(((GriefPreventionRegion) other).claim);
		}

		@Override
		public int hashCode() {
			return claim.hashCode();
		}
	}

}
