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
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class PreciousStonesHook extends RegionsPlugin<PreciousStones> {

	public PreciousStonesHook() {
		super("PreciousStones");
	}

	@Override
	public boolean canBuild_i(Player player, Location location) {
		return PreciousStones.API().canBreak(player, location) && PreciousStones.API().canPlace(player, location);
	}

	@Override
	public Collection<? extends Region> getRegionsAt_i(Location location) {
		return PreciousStones.API().getFieldsProtectingArea(FieldFlag.ALL, location).stream()
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

	@UnknownNullability
		private transient Field field;

	@SuppressWarnings("unused")
	public PreciousStonesRegion() {}

		public PreciousStonesRegion(Field field) {
			this.field = field;
		}

		@Override
		public boolean contains(Location location) {
			return field.envelops(location);
		}

		@Override
		public boolean isMember(OfflinePlayer player) {
			return field.isInAllowedList(player.getName());
		}

		@Override
		public Collection<OfflinePlayer> getMembers() {
			return field.getAllAllowed().stream()
				.map(Bukkit::getOfflinePlayer)
				.collect(Collectors.toSet());
		}

		@Override
		public boolean isOwner(OfflinePlayer player) {
			return field.isOwner(player.getName());
		}

		@Override
		public Collection<OfflinePlayer> getOwners() {
			return Collections.singleton(Bukkit.getOfflinePlayer(field.getOwner()));
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
		public RegionsPlugin<PreciousStones> getPlugin() {
			return PreciousStonesHook.this;
		}

		@Override
		public boolean equals(@Nullable Object other) {
			if (this == other)
				return true;
			if (other == null || getClass() != other.getClass())
				return false;
			PreciousStonesRegion that = (PreciousStonesRegion) other;
			return Objects.equals(field, that.field);
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
		public void deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
			new Fields(fields).setFields(this);
		}
	}
}
