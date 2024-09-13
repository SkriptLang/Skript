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

import static ch.njol.skript.variables.Variables.yggdrasil;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.util.AABB;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilID;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;

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
    public boolean canBuild_i(final Player p, final Location l) {
        return PreciousStones.API().canBreak(p, l) && PreciousStones.API().canPlace(p, l);
    }

    @Override
    public Collection<? extends Region> getRegionsAt_i(final Location l) {
        Set<PreciousStonesRegion> collect = PreciousStones.API().getFieldsProtectingArea(FieldFlag.ALL, l).stream()
                .map(PreciousStonesRegion::new)
                .collect(Collectors.toSet());
        assert collect != null;
		return collect;
    }

    @Override
    public @Nullable Region getRegion_i(final World world, final String name) {
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

        private transient Field field;

        public PreciousStonesRegion(final Field field) {
            this.field = field;
        }

        @Override
        public boolean contains(final Location l) {
            return field.envelops(l);
        }

        @Override
        public boolean isMember(final OfflinePlayer p) {
            return field.isInAllowedList(p.getName());
        }

        @Override
        public Collection<OfflinePlayer> getMembers() {
            @SuppressWarnings("deprecation")
			Set<OfflinePlayer> collect = field.getAllAllowed().stream()
                    .map(Bukkit::getOfflinePlayer)
                    .collect(Collectors.toSet());
            assert collect != null;
			return collect;
        }

        @Override
        public boolean isOwner(final OfflinePlayer p) {
            return field.isOwner(p.getName());
        }

        @Override
        public Collection<OfflinePlayer> getOwners() {
            @SuppressWarnings("deprecation")
			Set<OfflinePlayer> collect = Stream.of(Bukkit.getOfflinePlayer(field.getOwner()))
                    .collect(Collectors.toSet());
            assert collect != null;
			return collect;
        }

        @SuppressWarnings("null")
		@Override
        public Iterator<Block> getBlocks() {
            final List<Vector> vectors = field.getCorners();
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
        public boolean equals(@Nullable final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final PreciousStonesRegion that = (PreciousStonesRegion) o;
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
        public void deserialize(final Fields fields) throws StreamCorruptedException, NotSerializableException {
            new Fields(fields).setFields(this);
        }
    }
}
