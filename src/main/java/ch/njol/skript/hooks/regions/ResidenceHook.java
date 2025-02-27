package ch.njol.skript.hooks.regions;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import com.google.common.base.Objects;

import ch.njol.skript.hooks.regions.WorldGuardHook.WorldGuardRegion;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.variables.Variables;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilID;

/**
 * Hook for Residence protection plugin. Currently supports
 * only basic operations.
 * @author bensku
 */
public class ResidenceHook extends RegionsPlugin<Residence> {
	
	public ResidenceHook() throws IOException {}
	
	@Override
	protected boolean init() {
		return super.init();
	}
	
	@Override
	public String getName() {
		return "Residence";
	}
	
	@Override
	public boolean canBuild_i(final Player p, final Location l) {
		final ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(l);
		if (res == null)
			return true; // No claim here
		ResidencePermissions perms = res.getPermissions();
		return perms.playerHas(p, Flags.build, true);
	}
	
	@SuppressWarnings("null")
	@Override
	public Collection<? extends Region> getRegionsAt_i(final Location l) {
		final List<ResidenceRegion> ress = new ArrayList<>();
		final ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(l);
		if (res == null)
			return Collections.emptyList();
		ress.add(new ResidenceRegion(l.getWorld(), res));
		return ress;
	}
	
	@Override
	@Nullable
	public Region getRegion_i(final World world, final String name) {
		final ClaimedResidence res = Residence.getInstance().getResidenceManager().getByName(name);
		if (res == null)
			return null;
		return new ResidenceRegion(world, res);
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
	public class ResidenceRegion extends Region {
		
		private transient ClaimedResidence res;
		final World world;
		
		@SuppressWarnings({"null", "unused"})
		private ResidenceRegion() {
			world = null;
		}
		
		public ResidenceRegion(final World w, ClaimedResidence r) {
			res = r;
			world = w;
		}
		
		@Override
		public Fields serialize() throws NotSerializableException {
			final Fields f = new Fields(this);
			f.putObject("region", res.getName());
			return f;
		}

		@Override
		public void deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
			Object region = fields.getObject("region");
			if (!(region instanceof String))
				throw new StreamCorruptedException("Tried to deserialize Residence region with no valid name!");
			fields.setFields(this);
			ClaimedResidence res = Residence.getInstance().getResidenceManager().getByName((String) region);
			if (res == null)
				throw new StreamCorruptedException("Invalid region " + region + " in world " + world);
			this.res = res;
		}

		@Override
		public boolean contains(Location l) {
			return res.containsLoc(l);
		}

		@Override
		public boolean isMember(OfflinePlayer p) {
			return res.getPermissions().playerHas(p.getName(), Flags.build, false);
		}

		@SuppressWarnings("null")
		@Override
		public Collection<OfflinePlayer> getMembers() {
			return Collections.emptyList();
		}

		@Override
		public boolean isOwner(OfflinePlayer p) {
			return Objects.equal(res.getPermissions().getOwnerUUID(), p.getUniqueId());
		}

		@SuppressWarnings("null")
		@Override
		public Collection<OfflinePlayer> getOwners() {
			return Collections.singleton(Residence.getInstance().getOfflinePlayer(res.getPermissions().getOwner()));
		}

		@SuppressWarnings("null")
		@Override
		public Iterator<Block> getBlocks() {
			return Collections.emptyIterator();
		}

		@Override
		public String toString() {
			return res.getName() + " in world " + world.getName();
		}

		@Override
		public RegionsPlugin<?> getPlugin() {
			return ResidenceHook.this;
		}

		@Override
		public boolean equals(@Nullable Object o) {
			if (o == this)
				return true;
			if (!(o instanceof ResidenceRegion))
				return false;
			if (o.hashCode() == this.hashCode())
				return true;
			return false;
		}

		@Override
		public int hashCode() {
			return res.getName().hashCode();
		}
		
	}
}