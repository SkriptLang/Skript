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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
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

import com.destroystokyo.paper.block.BlockSoundGroup;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.block.BlockCompat;
import ch.njol.skript.bukkitutil.block.MagicBlockCompat;
import org.jetbrains.annotations.NotNull;

/**
 * A block that gets all data from a BlockState, and either reflects changes on the BlockState or delays them to the real block by 1 tick depending on which constructor is used.
 * 
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
public class BlockStateBlock implements Block {
	
	private static final boolean IS_RUNNING_1_13 = Skript.isRunningMinecraft(1, 13);
	private static final boolean ISPASSABLE_METHOD_EXISTS = Skript.methodExists(Block.class, "isPassable");
	
	final BlockState state;
	private final boolean delayChanges;
	private final boolean isPassable;
	
	public BlockStateBlock(final BlockState state) {
		this(state, false);
	}
	
	public BlockStateBlock(final BlockState state, final boolean delayChanges) {
		assert state != null;
		this.state = state;
		if (ISPASSABLE_METHOD_EXISTS)
			this.isPassable = state.getBlock().isPassable();
		else
			this.isPassable = false;
		this.delayChanges = delayChanges;
	}
	
	@Override
	public void setMetadata(final String metadataKey, final MetadataValue newMetadataValue) {
		state.setMetadata(metadataKey, newMetadataValue);
	}
	
	@Override
	public List<MetadataValue> getMetadata(final String metadataKey) {
		return state.getMetadata(metadataKey);
	}
	
	@Override
	public boolean hasMetadata(final String metadataKey) {
		return state.hasMetadata(metadataKey);
	}
	
	@Override
	public void removeMetadata(final String metadataKey, final Plugin owningPlugin) {
		state.removeMetadata(metadataKey, owningPlugin);
	}
	
	@Override
	public byte getData() {
		return state.getRawData();
	}
	
	public void setData(byte data) throws Throwable {
		MagicBlockCompat.setRawDataMethod.invokeExact(state, data);
	}
	
	@Override
	public Block getRelative(final int modX, final int modY, final int modZ) {
		return state.getBlock().getRelative(modX, modY, modZ);
	}
	
	@Override
	public Block getRelative(final BlockFace face) {
		return state.getBlock().getRelative(face);
	}
	
	@Override
	public Block getRelative(final BlockFace face, final int distance) {
		return state.getBlock().getRelative(face, distance);
	}
	
	@Override
	public Material getType() {
		return state.getType();
	}
	
	@Override
	public byte getLightLevel() {
		return state.getLightLevel();
	}
	
	@Override
	public byte getLightFromSky() {
		return state.getBlock().getLightFromSky();
	}
	
	@Override
	public byte getLightFromBlocks() {
		return state.getBlock().getLightFromBlocks();
	}
	
	@Override
	public World getWorld() {
		return state.getWorld();
	}
	
	@Override
	public int getX() {
		return state.getX();
	}
	
	@Override
	public int getY() {
		return state.getY();
	}
	
	@Override
	public int getZ() {
		return state.getZ();
	}

	@Override
	public Location getLocation() {
		return state.getLocation();
	}
	
	@Override
	public Chunk getChunk() {
		return state.getChunk();
	}
	
	@Override
	public void setType(Material type) {
		if (delayChanges) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					state.getBlock().setType(type);
				}
			});
		} else {
			state.setType(type);
		}
	}
	
	@Nullable
	@Override
	public BlockFace getFace(Block block) {
		return state.getBlock().getFace(block);
	}
	
	@Override
	public BlockState getState() {
		return state;
	}

	@Override
	public BlockState getState(boolean useSnapshot) {
		return state;
	}

	@Override
	public Biome getBiome() {
		return state.getBlock().getBiome();
	}
	
	@Override
	public void setBiome(final Biome bio) {
		state.getBlock().setBiome(bio);
	}
	
	@Override
	public boolean isBlockPowered() {
		return state.getBlock().isBlockPowered();
	}
	
	@Override
	public boolean isBlockIndirectlyPowered() {
		return state.getBlock().isBlockIndirectlyPowered();
	}
	
	@Override
	public boolean isBlockFacePowered(final BlockFace face) {
		return state.getBlock().isBlockFacePowered(face);
	}
	
	@Override
	public boolean isBlockFaceIndirectlyPowered(final BlockFace face) {
		return state.getBlock().isBlockFaceIndirectlyPowered(face);
	}
	
	@Override
	public int getBlockPower(final BlockFace face) {
		return state.getBlock().getBlockPower(face);
	}
	
	@Override
	public int getBlockPower() {
		return state.getBlock().getBlockPower();
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
		return state.getBlock().isBuildable();
	}
	
	@Override
	public boolean isBurnable() {
		return state.getBlock().isBurnable();
	}
	
	@Override
	public boolean isReplaceable() {
		return state.getBlock().isReplaceable();
	}
	
	@Override
	public boolean isSolid() {
		return state.getBlock().isSolid();
	}
	
	@Override
	public double getTemperature() {
		return state.getBlock().getTemperature();
	}
	
	@Override
	public double getHumidity() {
		return state.getBlock().getHumidity();
	}
	
	@Override
	public PistonMoveReaction getPistonMoveReaction() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean breakNaturally() {
		if (delayChanges) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					state.getBlock().breakNaturally();
				}
			});
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean breakNaturally(@Nullable ItemStack tool) {
		if (delayChanges) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					state.getBlock().breakNaturally(tool);
				}
			});
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean breakNaturally(ItemStack tool, boolean triggerEffect) {
		if (delayChanges) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					state.getBlock().breakNaturally(tool, triggerEffect);
				}
			});
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean applyBoneMeal(BlockFace blockFace) {
		return state.getBlock().applyBoneMeal(blockFace);
	}
	
	@Override
	public Collection<ItemStack> getDrops() {
		assert false;
		return Collections.emptySet();
	}
	
	@Override
	public Collection<ItemStack> getDrops(@Nullable ItemStack tool) {
		assert false;
		return Collections.emptySet();
	}
	
	@Override
	public Collection<ItemStack> getDrops(ItemStack tool, @Nullable Entity entity) {
		assert false;
		return Collections.emptySet();
	}

	@Nullable
	@Override
	public Location getLocation(final @Nullable Location loc) {
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

	// 1.13 additions
	// Note that overridden methods may not exist on Minecraft<1.13
	
	@Override
	public void setType(Material type, boolean applyPhysics) {
		if (!IS_RUNNING_1_13) {
			throw new IllegalStateException("not on 1.13");
		}
		
		if (delayChanges) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					state.getBlock().setType(type, applyPhysics);
				}
			});
		} else { // Can't do physics for block state
			state.setType(type);
		}
	}

	@Override
	public BlockData getBlockData() {
		if (!IS_RUNNING_1_13) {
			throw new IllegalStateException("not on 1.13");
		}
		
		return state.getBlockData();
	}

	@Override
	public void setBlockData(BlockData data) {
		if (!IS_RUNNING_1_13) {
			throw new IllegalStateException("not on 1.13");
		}
		
		if (delayChanges) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					state.getBlock().setBlockData(data);
				}
			});
		} else {
			state.setBlockData(data);
		}
	}

	@Override
	public void setBlockData(BlockData data, boolean applyPhysics) {
		if (!IS_RUNNING_1_13) {
			throw new IllegalStateException("not on 1.13");
		}
		
		if (delayChanges) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					state.getBlock().setBlockData(data, applyPhysics);
				}
			});
		} else { // Cannot apply physics to a block state
			state.setBlockData(data);
		}
	}
	
	@Nullable
	@Override
	public RayTraceResult rayTrace(Location start, Vector direction, double maxDistance, FluidCollisionMode fluidCollisionMode) {
		return state.getBlock().rayTrace(start, direction, maxDistance, fluidCollisionMode);
	}
	
	@Override
	public boolean isPassable() {
		return isPassable;
	}

	@Override
	public BoundingBox getBoundingBox() {
		return state.getBlock().getBoundingBox();
	}

	@Override
	public BlockSoundGroup getSoundGroup() {
		return state.getBlock().getSoundGroup();
	}
	
	@Override
	public String getTranslationKey() {
		return state.getBlock().getTranslationKey();
	}
	
	@Override
	public float getDestroySpeed(ItemStack itemStack) {
		return state.getBlock().getDestroySpeed(itemStack);
	}

	@Override
	public boolean isPreferredTool(@NotNull ItemStack tool) {
		return state.getBlock().isPreferredTool(tool);
	}

	@Override
	public boolean isValidTool(@NotNull ItemStack itemStack) {
		return state.getBlock().isValidTool(itemStack);
	}

	@Override
	public @NotNull float getDestroySpeed(@NotNull ItemStack itemStack, boolean considerEnchants) {
		return state.getBlock().getDestroySpeed(itemStack, considerEnchants);
	}

	@Override
	public @NotNull VoxelShape getCollisionShape() {
		return state.getBlock().getCollisionShape();
	}

	@Override
	public float getBreakSpeed(@NotNull Player player) {
		return state.getBlock().getBreakSpeed(player);
	}

}
