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

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.util.AABB;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.iterator.EmptyIterator;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilID;

/**
 * @author Peter Güttinger
 */
public class GriefPreventionHook extends RegionsPlugin<GriefPrevention> {
	
	public GriefPreventionHook() throws IOException {}
	
	boolean supportsUUIDs;
	@Nullable
	Method getClaim;
	@Nullable
	Field claimsField;
	
	@SuppressWarnings("null")
	@Override
	protected boolean init() {
		// ownerID is a public field
		supportsUUIDs = Skript.fieldExists(Claim.class, "ownerID");
		try {
			getClaim = DataStore.class.getDeclaredMethod("getClaim", long.class);
			getClaim.setAccessible(true);
			if (!Claim.class.isAssignableFrom(getClaim.getReturnType()))
				getClaim = null;
		} catch (final NoSuchMethodException e) {} catch (final SecurityException e) {}
		try {
			claimsField = DataStore.class.getDeclaredField("claims");
			claimsField.setAccessible(true);
			if (!List.class.isAssignableFrom(claimsField.getType()))
				claimsField = null;
		} catch (final NoSuchFieldException e) {} catch (final SecurityException e) {}
		if (getClaim == null && claimsField == null) {
			Skript.error("Skript " + Skript.getVersion() + " is not compatible with GriefPrevention " + plugin.getDescription().getVersion() + "."
					+ " Please report this at https://github.com/SkriptLang/Skript/issues/ if this error occurred after you updated GriefPrevention.");
			return false;
		}
		return super.init();
	}
	
	@Nullable
	Claim getClaim(final long id) {
		if (getClaim != null) {
			try {
				return (Claim) getClaim.invoke(plugin.dataStore, id);
			} catch (final IllegalAccessException e) {
				assert false : e;
			} catch (final IllegalArgumentException e) {
				assert false : e;
			} catch (final InvocationTargetException e) {
				throw new RuntimeException(e.getCause());
			}
		} else {
			assert claimsField != null;
			try {
				final List<?> claims = (List<?>) claimsField.get(plugin.dataStore);
				for (final Object claim : claims) {
					if (!(claim instanceof Claim))
						continue;
					if (((Claim) claim).getID() == id)
						return (Claim) claim;
				}
			} catch (final IllegalArgumentException e) {
				assert false : e;
			} catch (final IllegalAccessException e) {
				assert false : e;
			}
		}
		return null;
	}
	
	@Override
	public String getName() {
		return "GriefPrevention";
	}
	
	@Override
	public boolean canBuild_i(final Player p, final Location l) {
		return plugin.allowBuild(p, l) == null; // returns reason string if not allowed to build
	}
	
	static {
		Variables.yggdrasil.registerSingleClass(GriefPreventionRegion.class);
	}
	
	@YggdrasilID("GriefPreventionRegion")
	public final class GriefPreventionRegion extends Region {
		
		private transient Claim claim;
		
		@SuppressWarnings({"null", "unused"})
		private GriefPreventionRegion() {}
		
		public GriefPreventionRegion(final Claim c) {
			claim = c;
		}
		
		@Override
		public boolean contains(final Location l) {
			return claim.contains(l, false, false);
		}
		
		@Override
		public boolean isMember(final OfflinePlayer p) {
			return isOwner(p);
		}
		
		@Override
		public Collection<OfflinePlayer> getMembers() {
			return getOwners();
		}
		
		@Override
		public boolean isOwner(final OfflinePlayer p) {
			String name = p.getName();
			if (name != null)
				return name.equalsIgnoreCase(claim.getOwnerName());
			return false; // Assume no ownership when player has never visited server
		}
		
		@SuppressWarnings({"null", "deprecation"})
		@Override
		public Collection<OfflinePlayer> getOwners() {
			if (claim.isAdminClaim() || (supportsUUIDs && claim.ownerID == null)) // Not all claims have owners!
				return Collections.emptyList();
			else if (supportsUUIDs)
				return Arrays.asList(Bukkit.getOfflinePlayer(claim.ownerID));
			else
				return Arrays.asList(Bukkit.getOfflinePlayer(claim.getOwnerName()));
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
		
		@SuppressWarnings("null")
		@Override
		public Fields serialize() {
			final Fields f = new Fields();
			f.putPrimitive("id", claim.getID());
			return f;
		}
		
		@Override
		public void deserialize(final Fields fields) throws StreamCorruptedException {
			final long id = fields.getPrimitive("id", long.class);
			final Claim c = getClaim(id);
			if (c == null)
				throw new StreamCorruptedException("Invalid claim " + id);
			claim = c;
		}
		
		@Override
		public RegionsPlugin<?> getPlugin() {
			return GriefPreventionHook.this;
		}
		
		@Override
		public boolean equals(final @Nullable Object o) {
			if (o == this)
				return true;
			if (o == null)
				return false;
			if (!(o instanceof GriefPreventionRegion))
				return false;
			return claim.equals(((GriefPreventionRegion) o).claim);
		}
		
		@Override
		public int hashCode() {
			return claim.hashCode();
		}
		
	}
	
	@SuppressWarnings("null")
	@Override
	public Collection<? extends Region> getRegionsAt_i(final Location l) {
		final Claim c = plugin.dataStore.getClaimAt(l, false, null);
		if (c != null)
			return Arrays.asList(new GriefPreventionRegion(c));
		return Collections.emptySet();
	}
	
	@Override
	@Nullable
	public Region getRegion_i(final World world, final String name) {
		try {
			final Claim c = getClaim(Long.parseLong(name));
			if (c != null && world.equals(c.getLesserBoundaryCorner().getWorld()))
				return new GriefPreventionRegion(c);
			return null;
		} catch (final NumberFormatException e) {
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
}
