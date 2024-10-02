package ch.njol.skript.hooks.regions;

import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.util.AABB;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilID;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PreciousStonesHook extends RegionsPlugin<PreciousStones> {

	public PreciousStonesHook() throws IOException {
	}

	@Override
	protected boolean init() {
		return super.init();
	}

	@Override
	public String getName() {
		return "PreciousStones";
	}

	@Override
	public boolean canBuild_i(Player player, Location location) {
		return PreciousStones.API().canBreak(player, location) && PreciousStones.API().canPlace(player, location);
	}

	@Override
	public Collection<? extends Region> getRegionsAt_i(Location location) {
		return PreciousStones.API().getFieldsProtectingArea(FieldFlag.ALL, location)
			.stream()
			.map(PreciousStonesRegion::new)
			.collect(Collectors.toSet());
	}

	@Override
	public @Nullable Region getRegion_i(World world, String name) {
		return null;
	}

	@Override
	public boolean hasMultipleOwners_i() {
		return true;
	}

	@Override
	protected Class<? extends Region> getRegionClass() {
		return PreciousStonesRegion.class;
	}

	@YggdrasilID("PreciousStonesRegion")
	public final class PreciousStonesRegion extends Region {

		private final transient Field field;

		public PreciousStonesRegion(Field field) {
			this.field = field;
		}

		@Override
		public boolean contains(Location location) {
			return field.envelops(location);
		}

		@Override
		public boolean isMember(OfflinePlayer offlinePlayer) {
			return field.isInAllowedList(offlinePlayer.getName());
		}

		@Override
		public Collection<OfflinePlayer> getMembers() {
			return field.getAllAllowed().stream()
				.map(Bukkit::getOfflinePlayer)
				.collect(Collectors.toSet());
		}

		@Override
		public boolean isOwner(OfflinePlayer offlinePlayer) {
			return field.isOwner(offlinePlayer.getName());
		}

		@Override
		public Collection<OfflinePlayer> getOwners() {
			return Stream.of(Bukkit.getOfflinePlayer(field.getOwner()))
				.collect(Collectors.toSet());
		}

		@Override
		public Iterator<Block> getBlocks() {
			List<Vector> vectors = field.getCorners();

			return new AABB(Bukkit.getWorld(field.getWorld()), vectors.get(0), vectors.get(7)).iterator();
		}

		@Override
		public String toString() {
			return field.getName() + " in world " + field.getWorld();
		}

		@Override
		public RegionsPlugin<?> getPlugin() {
			return PreciousStonesHook.this;
		}

		@Override
		public boolean equals(@Nullable Object object) {
			if (this == object)
				return true;

			if (object == null || getClass() != object.getClass())
				return false;

			PreciousStonesRegion region = (PreciousStonesRegion) object;

			return Objects.equals(field, region.field);
		}

		@Override
		public int hashCode() {
			return Objects.hash(field);
		}

		@Override
		public Fields serialize() throws NotSerializableException {
			return new Fields(this);
		}

		@Override
		public void deserialize(@NotNull Fields fields) throws StreamCorruptedException, NotSerializableException {
			new Fields(fields).setFields(this);
		}
	}

}
