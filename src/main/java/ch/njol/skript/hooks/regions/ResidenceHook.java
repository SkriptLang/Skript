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

import ch.njol.skript.hooks.regions.WorldGuardHook.WorldGuardRegion;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.variables.Variables;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilID;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import com.google.common.base.Objects;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class ResidenceHook extends RegionsPlugin<Residence> {

	public ResidenceHook() {
		super("Residence");
	}

	@Override
	public boolean init() {
		return super.init();
	}
	
	@Override
	public boolean canBuild_i(Player player, Location location) {
		ClaimedResidence residence = Residence.getInstance().getResidenceManager().getByLoc(location);
		if (residence == null)
			return true; // No claim here
		ResidencePermissions perms = residence.getPermissions();
		return perms.playerHas(player, Flags.build, true);
	}

	@Override
	public Collection<? extends Region> getRegionsAt_i(Location location) {
		List<ResidenceRegion> residences = new ArrayList<>();
		ClaimedResidence residence = Residence.getInstance().getResidenceManager().getByLoc(location);
		if (residence == null)
			return Collections.emptyList();
		residences.add(new ResidenceRegion(location.getWorld(), residence));
		return residences;
	}
	
	@Override
	@Nullable
	public Region getRegion_i(World world, String name) {
		ClaimedResidence residence = Residence.getInstance()
				.getResidenceManager()
				.getByName(name);
		if (residence == null)
			return null;
		return new ResidenceRegion(world, residence);
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
		Variables.yggdrasil.registerSingleClass(ResidenceRegion.class);
	}
	
	@YggdrasilID("ResidenceRegion")
	public final class ResidenceRegion extends Region {

		@UnknownNullability
		private transient ClaimedResidence residence;
		@UnknownNullability
		final World world;
		
		@SuppressWarnings("unused")
		private ResidenceRegion() {
			world = null;
		}
		
		public ResidenceRegion(World world, ClaimedResidence residence) {
			this.residence = residence;
			this.world = world;
		}
		
		@Override
		public Fields serialize() throws NotSerializableException {
			Fields fields = new Fields(this);
			fields.putObject("region", residence.getName());
			return fields;
		}

		@Override
		public void deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
			Object region = fields.getObject("region");
			if (!(region instanceof String))
				throw new StreamCorruptedException("Tried to deserialize Residence region with no valid name!");
			fields.setFields(this);
			ClaimedResidence residence = Residence.getInstance()
					.getResidenceManager()
					.getByName((String) region);
			if (residence == null)
				throw new StreamCorruptedException("Invalid region " + region + " in world " + world);
			this.residence = residence;
		}

		@Override
		public boolean contains(Location location) {
			return residence.containsLoc(location);
		}

		@Override
		public boolean isMember(OfflinePlayer player) {
			return residence.getPermissions().playerHas(player.getName(), Flags.build, false);
		}

		@Override
		public Collection<OfflinePlayer> getMembers() {
			return Collections.emptyList();
		}

		@Override
		public boolean isOwner(OfflinePlayer player) {
			return Objects.equal(residence.getPermissions().getOwnerUUID(), player.getUniqueId());
		}

		@Override
		public Collection<OfflinePlayer> getOwners() {
			return Collections.singleton(Residence.getInstance().getOfflinePlayer(residence.getPermissions().getOwner()));
		}

		@Override
		public Iterator<Block> getBlocks() {
			return Collections.emptyIterator();
		}

		@Override
		public String toString() {
			return residence.getName() + " in world " + world.getName();
		}

		@Override
		public RegionsPlugin<Residence> getPlugin() {
			return ResidenceHook.this;
		}

		@Override
		public boolean equals(@Nullable Object other) {
			if (other == this)
				return true;
			if (!(other instanceof ResidenceRegion))
				return false;
			return other.hashCode() == this.hashCode();
		}

		@Override
		public int hashCode() {
			return residence.getName().hashCode();
		}
	}

}
