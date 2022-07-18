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
package ch.njol.skript.util;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.block.BlockCompat;
import com.destroystokyo.paper.block.BlockSoundGroup;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundGroup;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.bukkit.util.VoxelShape;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * A block that gets all data from the world, but either delays
 * any changes by 1 tick of reflects them on a given BlockState
 * depending on which constructor is used.
 */
public class DelayedChangeBlock implements Block {

	private static final boolean ISPASSABLE_METHOD_EXISTS = Skript.methodExists(Block.class, "isPassable");

	final Block b;
	@Nullable
	private final BlockState newState;
	private final boolean isPassable;

	public DelayedChangeBlock(Block b) {
		this(b, null);
	}

	public DelayedChangeBlock(Block b, @Nullable BlockState newState) {
		assert b != null;
		this.b = b;
		this.newState = newState;
		if (ISPASSABLE_METHOD_EXISTS && newState != null)
			this.isPassable = newState.getBlock().isPassable();
		else
			this.isPassable = false;
	}

	@Override
	public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
		b.setMetadata(metadataKey, newMetadataValue);
	}

	@Override
	public List<MetadataValue> getMetadata(String metadataKey) {
		return b.getMetadata(metadataKey);
	}

	@Override
	public boolean hasMetadata(String metadataKey) {
		return b.hasMetadata(metadataKey);
	}

	@Override
	public void removeMetadata(String metadataKey, Plugin owningPlugin) {
		b.removeMetadata(metadataKey, owningPlugin);
	}

	@SuppressWarnings("deprecation")
	@Override
	public byte getData() {
		return b.getData();
	}

	@Override
	public Block getRelative(int modX, int modY, int modZ) {
		return b.getRelative(modX, modY, modZ);
	}

	@Override
	public Block getRelative(BlockFace face) {
		return b.getRelative(face);
	}

	@Override
	public Block getRelative(BlockFace face, int distance) {
		return b.getRelative(face, distance);
	}

	@Override
	public Material getType() {
		return b.getType();
	}

	@Override
	public byte getLightLevel() {
		return b.getLightLevel();
	}

	@Override
	public byte getLightFromSky() {
		return b.getLightFromSky();
	}

	@Override
	public byte getLightFromBlocks() {
		return b.getLightFromBlocks();
	}

	@Override
	public World getWorld() {
		return b.getWorld();
	}

	@Override
	public int getX() {
		return b.getX();
	}

	@Override
	public int getY() {
		return b.getY();
	}

	@Override
	public int getZ() {
		return b.getZ();
	}

	@Override
	public Location getLocation() {
		return b.getLocation();
	}

	@Override
	public Chunk getChunk() {
		return b.getChunk();
	}

	@Override
	public void setType(Material type) {
		if (newState != null) {
			newState.setType(type);
		} else {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					b.setType(type);
				}
			});
		}
	}

	@Nullable
	@Override
	public BlockFace getFace(Block block) {
		return b.getFace(block);
	}

	@Override
	public BlockState getState() {
		return b.getState();
	}

	@Override
	public BlockState getState(boolean useSnapshot) {
		return b.getState(useSnapshot);
	}

	@Override
	public Biome getBiome() {
		return b.getBiome();
	}

	@Override
	public @NotNull Biome getComputedBiome() {
		return b.getComputedBiome();
	}

	@Override
	public void setBiome(Biome bio) {
		b.setBiome(bio);
	}

	@Override
	public boolean isBlockPowered() {
		return b.isBlockPowered();
	}

	@Override
	public boolean isBlockIndirectlyPowered() {
		return b.isBlockIndirectlyPowered();
	}

	@Override
	public boolean isBlockFacePowered(BlockFace face) {
		return b.isBlockFacePowered(face);
	}

	@Override
	public boolean isBlockFaceIndirectlyPowered(BlockFace face) {
		return b.isBlockFaceIndirectlyPowered(face);
	}

	@Override
	public int getBlockPower(BlockFace face) {
		return b.getBlockPower(face);
	}

	@Override
	public int getBlockPower() {
		return b.getBlockPower();
	}

	@Override
	public boolean isEmpty() {
		Material type = getType();
		assert type != null;
		return BlockCompat.INSTANCE.isEmpty(type);
	}

	@Override
	public boolean isLiquid() {
		Material type = getType();
		assert type != null;
		return BlockCompat.INSTANCE.isLiquid(type);
	}

	@Override
	public boolean isBuildable() {
		return b.isBuildable();
	}

	@Override
	public boolean isBurnable() {
		return b.isBurnable();
	}

	@Override
	public boolean isReplaceable() {
		return b.isReplaceable();
	}

	@Override
	public boolean isSolid() {
		return b.isSolid();
	}

	@Override
	public boolean isCollidable() {
		return b.isCollidable();
	}

	@Override
	public double getTemperature() {
		return b.getTemperature();
	}

	@Override
	public double getHumidity() {
		return b.getHumidity();
	}

	@Override
	public PistonMoveReaction getPistonMoveReaction() {
		return b.getPistonMoveReaction();
	}

	@Override
	public boolean breakNaturally() {
		if (newState != null) {
			return false;
		} else {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					b.breakNaturally();
				}
			});
			return true;
		}
	}

	@Override
	public boolean breakNaturally(@Nullable ItemStack tool) {
		if (newState != null) {
			return false;
		} else {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					b.breakNaturally(tool);
				}
			});
			return true;
		}
	}

	@Override
	public boolean breakNaturally(boolean triggerEffect) {
		if (newState != null) {
			return false;
		} else {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					b.breakNaturally(triggerEffect);
				}
			});
			return true;
		}
	}

	@Override
	public boolean breakNaturally(ItemStack tool, boolean triggerEffect) {
		if (newState != null) {
			return false;
		} else {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					b.breakNaturally(tool, triggerEffect);
				}
			});
			return true;
		}
	}

	@Override
	public boolean applyBoneMeal(BlockFace blockFace) {
		return b.applyBoneMeal(blockFace);
	}

	@Override
	public Collection<ItemStack> getDrops() {
		return b.getDrops();
	}

	@Override
	public Collection<ItemStack> getDrops(@Nullable ItemStack tool) {
		return b.getDrops(tool);
	}

	@Override
	public Collection<ItemStack> getDrops(ItemStack tool, @Nullable Entity entity) {
		return b.getDrops(tool, entity);
	}

	@Nullable
	@Override
	public Location getLocation(@Nullable Location loc) {
		if (loc != null) {
			loc.setWorld(getWorld());
			loc.setX(getX());
			loc.setY(getY());
			loc.setZ(getZ());
			loc.setPitch(0);
			loc.setYaw(0);
		}
		return loc;
	}

	@Override
	public void setType(Material type, boolean applyPhysics) {
		if (newState != null) {
			newState.setType(type);
		} else {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					b.setType(type, applyPhysics);
				}
			});
		}
	}

	@Override
	public BlockData getBlockData() {
		return b.getBlockData();
	}

	@Override
	public void setBlockData(BlockData data) {
		setBlockData(data, true);
	}

	@Override
	public void setBlockData(BlockData data, boolean applyPhysics) {
		if (newState != null) {
			newState.setBlockData(data);
		} else {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), () -> b.setBlockData(data, applyPhysics));
		}
	}

	@Nullable
	@Override
	public RayTraceResult rayTrace(Location start, Vector direction, double maxDistance, FluidCollisionMode fluidCollisionMode) {
		return b.rayTrace(start, direction, maxDistance, fluidCollisionMode);
	}

	@Override
	public boolean isPassable() {
		return isPassable;
	}

	@Override
	public BoundingBox getBoundingBox() {
		return b.getBoundingBox();
	}

	@Override
	public BlockSoundGroup getSoundGroup() {
		return b.getSoundGroup();
	}

	@Override
	public @NotNull SoundGroup getBlockSoundGroup() {
		return b.getBlockSoundGroup();
	}

	@Override
	public String getTranslationKey() {
		return b.getTranslationKey();
	}

	@Override
	public float getDestroySpeed(ItemStack itemStack) {
		return b.getDestroySpeed(itemStack);
	}

	@Override
	public boolean isPreferredTool(@NotNull ItemStack tool) {
		return b.isPreferredTool(tool);
	}

	@Override
	public boolean isValidTool(@NotNull ItemStack itemStack) {
		return b.isValidTool(itemStack);
	}

	@Override
	public float getDestroySpeed(@NotNull ItemStack itemStack, boolean considerEnchants) {
		return b.getDestroySpeed(itemStack, considerEnchants);
	}

	@Override
	@NotNull
	public VoxelShape getCollisionShape() {
		return b.getCollisionShape();
	}

	@Override
	public boolean canPlace(@NotNull BlockData data) {
		return b.canPlace(data);
	}

	@Override
	public float getBreakSpeed(@NotNull Player player) {
		return b.getBreakSpeed(player);
	}

	@Override
	public @NotNull String translationKey() {
		return b.getTranslationKey();
	}
}
