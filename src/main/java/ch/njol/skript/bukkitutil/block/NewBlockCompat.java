package ch.njol.skript.bukkitutil.block;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.aliases.MatchQuality;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.variables.Variables;
import ch.njol.yggdrasil.Fields;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.BlockSupport;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Snow;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.StreamCorruptedException;
import java.util.Map;

/**
 * 1.13+ block compat.
 */
public class NewBlockCompat implements BlockCompat {

	private static class NewBlockValues extends BlockValues {

		static {
			Variables.yggdrasil.registerSingleClass(NewBlockValues.class, "NewBlockValues");
		}

		Material type;
		BlockData data;
		boolean isDefault;
		
		public NewBlockValues(Material type, BlockData data, boolean isDefault) {
			if (type != data.getMaterial())
				throw new IllegalArgumentException("'type' does not match material of 'data'");
			this.type = type;
			this.data = data;
			this.isDefault = isDefault;
		}

		/**
		 * For Serialization - INTERNAL USAGE ONLY!!
		 */
		private NewBlockValues() { }
		
		@Override
		public boolean isDefault() {
			return isDefault;
		}

		@Override
		public boolean equals(@Nullable Object other) {
			if (!(other instanceof NewBlockValues))
				return false;
			NewBlockValues n = (NewBlockValues) other;
			return (data.matches(n.data) || n.data.matches(data)) && type.equals(n.type);
		}

		@SuppressWarnings("null")
		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + (data == null ? 0 : data.hashCode());
			result = prime * result + type.hashCode();
			return result;
		}
		
		@Override
		public String toString() {
			return data.toString() + (isDefault ? " (default)" : "");
		}

		@Override
		public MatchQuality match(BlockValues other) {
			if (!(other instanceof NewBlockValues)) {
				throw new IllegalArgumentException("wrong block compat");
			}
			NewBlockValues n = (NewBlockValues) other;
			if (type == n.type) {
				if (data.equals(n.data)) { // Check for exact item match
					return MatchQuality.EXACT;
				} else if (data.matches(n.data)) { // What about explicitly defined states only?
					return MatchQuality.SAME_ITEM;
				} else { // Just same material and different block states
					return MatchQuality.SAME_MATERIAL;
				}
			} else {
				return MatchQuality.DIFFERENT;
			}
		}

		@Override
		public Fields serialize() {
			Fields fields = new Fields();
			fields.putObject("data", data.getAsString());
			fields.putPrimitive("isDefault", isDefault);
			return fields;
		}

		@Override
		public void deserialize(@NotNull Fields fields) throws StreamCorruptedException {
			String data = fields.getObject("data", String.class);
			boolean isDefault = fields.getPrimitive("isDefault", Boolean.class);
			if (data == null)
				throw new StreamCorruptedException("'data' is missing.");

			this.data = Bukkit.createBlockData(data);
			this.type = this.data.getMaterial();
			this.isDefault = isDefault;
		}
	}
	
	private static class NewBlockSetter implements BlockSetter {
		private static final BlockFace[] CARDINAL_FACES =
			new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
		
		@SuppressWarnings("null") // Late initialization with loadTypes() to avoid circular dependencies
		public NewBlockSetter() {}

		@Override
		public void setBlock(Block block, Material type, @Nullable BlockValues values, int flags) {
			boolean rotate = (flags & ROTATE) != 0;
			boolean rotateForce = (flags & ROTATE_FORCE) != 0;
			boolean rotateFixType = (flags & ROTATE_FIX_TYPE) != 0;
			boolean multipart = (flags & MULTIPART) != 0;
			boolean applyPhysics = (flags & APPLY_PHYSICS) != 0;
			NewBlockValues ourValues = null;
			if (values != null)
				ourValues = (NewBlockValues) values;
			
			Class<?> dataType = type.data;
			
			/**
			 * Set to true when block is placed. If no special logic places
			 * the block, generic placement will be done.
			 */
			boolean placed = false;
			if (rotate) {
				if (type == Material.TORCH || (rotateFixType && type == Material.WALL_TORCH)) {
					// If floor torch cannot be placed, try a wall torch
					Block under = block.getRelative(0, -1, 0);
					boolean canPlace = canSupportFloorTorch(under);
					
					// Can't really place a floor torch, try wall one instead
					if (!canPlace) {
						BlockFace face = findWallTorchSide(block);
						if (face != null) { // Found better torch spot
							block.setType(Material.WALL_TORCH);
							Directional data = (Directional) block.getBlockData();
							data.setFacing(face);
							block.setBlockData(data, applyPhysics);
							placed = true;
						}
					}
				} else if (type == Material.WALL_TORCH) {
					Directional data;
					if (ourValues != null)
						data = (Directional) ourValues.data;
					else
						data = (Directional) Bukkit.createBlockData(type);
					
					Block relative = block.getRelative(data.getFacing());
					if ((!canSupportWallTorch(relative, data.getFacing().getOppositeFace())) || rotateForce) {
						// Attempt to figure out a better rotation
						BlockFace face = findWallTorchSide(block);
						if (face != null) { // Found better torch spot
							block.setType(type);
							data.setFacing(face);
							block.setBlockData(data, applyPhysics);
							placed = true;
						}
					}
				}
			}
			
			if (multipart) {
				// Beds
				if (Bed.class.isAssignableFrom(dataType)) {
					Bed data;
					if (ourValues != null)
						data = (Bed) ourValues.data.clone();
					else
						data = (Bed) Bukkit.createBlockData(type);
					
					// Place this bed
					block.setType(type, false);
					block.setBlockData(data, applyPhysics);
					
					// Calculate rotation and location of other part
					BlockFace facing = data.getFacing();
					BlockFace otherFacing = facing;
					Bed.Part otherPart = Bed.Part.HEAD;
					if (data.getPart().equals(Bed.Part.HEAD)) {
						facing = facing.getOppositeFace();
						otherPart = Bed.Part.FOOT;
					}
					
					// Place the other part
					Block other = block.getRelative(facing);
					other.setType(type, false);
					
					data.setPart(otherPart);
					data.setFacing(otherFacing);
					other.setBlockData(data, applyPhysics);
					
					placed = true;
				}
				
				// Top-down bisected blocks (doors etc.)
				if (Bisected.class.isAssignableFrom(dataType) && !Tag.STAIRS.isTagged(type) && !Tag.TRAPDOORS.isTagged(type)) {
					Bisected data;
					if (ourValues != null)
						data = (Bisected) ourValues.data.clone();
					else
						data = (Bisected) Bukkit.createBlockData(type);
					
					// Place this part
					block.setType(type, false);
					block.setBlockData(data, applyPhysics);
					
					// Figure out place of other part
					BlockFace facing = BlockFace.DOWN;
					Bisected.Half otherHalf = Bisected.Half.BOTTOM;
					if (data.getHalf().equals(Bisected.Half.BOTTOM)) {
						facing = BlockFace.UP;
						otherHalf = Bisected.Half.TOP;
					}
					
					// Place the other block
					Block other = block.getRelative(facing);
					other.setType(type, false);
					
					data.setHalf(otherHalf);
					other.setBlockData(data, applyPhysics);
					
					placed = true;
				}
			}
			
			// Generic block placement
			if (!placed) {
				block.setType(type);
				if (ourValues != null && !ourValues.isDefault())
					block.setBlockData(ourValues.data, applyPhysics);
			}
		}

		// 1.19+
		// TODO: remove in 2.10
		private static final boolean HAS_BLOCK_SUPPORT = Skript.classExists("org.bukkit.block.BlockSupport");

		/**
		 * Returns whether this block can support a floor torch.
		 * @param block The block the torch will be placed on
		 * @return whether the block can support the torch.
		 */
		private static boolean canSupportFloorTorch(Block block) {
			if (HAS_BLOCK_SUPPORT)
				return block.getBlockData().isFaceSturdy(BlockFace.UP, BlockSupport.CENTER);

			Material material = block.getType();
			return (material.isOccluding()
				|| canSupportWallTorch(block, null)
				|| ItemUtils.isFence(block)
				|| ItemUtils.isGlass(material)
				|| material == Material.HOPPER
				|| (material == Material.SNOW && (((Snow) block.getBlockData()).getLayers() == 8))
			);
		}

		/**
		 * Returns whether this block can support a wall torch. In 1.19+, a face can be specified.
		 * @param block The block the torch will be placed on
		 * @param face The face the torch will be placed on (only considered in 1.19+)
		 * @return whether the block face can support the torch.
		 */
		private static boolean canSupportWallTorch(Block block, @Nullable BlockFace face) {
			if (HAS_BLOCK_SUPPORT && face != null)
				return block.getBlockData().isFaceSturdy(face, BlockSupport.FULL);

			Material material = block.getType();
			return material.isOccluding() || material == Material.SOUL_SAND || material == Material.SPAWNER;
		}

		@Nullable
		private BlockFace findWallTorchSide(Block block) {
			for (BlockFace face : CARDINAL_FACES) {
				Block relative = block.getRelative(face);
				if (relative.getType().isOccluding() || canSupportWallTorch(relative, face.getOppositeFace()))
					return face.getOppositeFace(); // Torch can be rotated towards from this face
			}
			
			return null; // Can't place torch here legally
		}
		
		@Override
		public void sendBlockChange(Player player, Location location, Material type, @Nullable BlockValues values) {
			BlockData blockData = values != null ? ((NewBlockValues) values).data : type.createBlockData();
			player.sendBlockChange(location, blockData);
		}
		
	}
	
	private NewBlockSetter setter = new NewBlockSetter();

	/**
	 * @deprecated Use {@link #getBlockValues(BlockData)} instead
	 */
	@Deprecated
	@Nullable
	@Override
	public BlockValues getBlockValues(BlockState blockState) {
		return getBlockValues(blockState.getBlockData());
	}

	@Override
	public @Nullable BlockValues getBlockValues(Material material) {
		if (material.isBlock())
			return new NewBlockValues(material, Bukkit.createBlockData(material), true);
		return null;
	}

	@Nullable
	@Override
	public BlockValues getBlockValues(BlockData blockData) {
		return new NewBlockValues(blockData.getMaterial(), blockData, false);
	}

	@Override
	@Nullable
	public BlockValues getBlockValues(ItemStack stack) {
		Material type = stack.getType();
		if (type.isBlock()) { // Block has data
			// Create default block data for the type
			return new NewBlockValues(type, Bukkit.createBlockData(type), true);
		}
		return null;
	}

	@Override
	public BlockSetter getSetter() {
		return setter;
	}

	@Deprecated
	@Override
	public BlockState fallingBlockToState(FallingBlock entity) {
		BlockState state = entity.getLocation().getBlock().getState();
		state.setBlockData(entity.getBlockData());
		return state;
	}

	@Override
	@Nullable
	public BlockValues createBlockValues(Material type, Map<String, String> states, @Nullable ItemStack item, int itemFlags) {
		// Ignore item; on 1.13+ block data never applies to items
		if (states.isEmpty()) {
			if (type.isBlock()) { // Still need default block values
				BlockData data =  Bukkit.createBlockData(type, "[]");
				assert data != null;
				return new NewBlockValues(type, data, true);
			} else { // Items cannot have block data
				return null;
			}
		}
		
		StringBuilder combined = new StringBuilder("[");
		boolean first = true;
		for (Map.Entry<String, String> entry : states.entrySet()) {
			if (first)
				first = false;
			else
				combined.append(',');
			combined.append(entry.getKey()).append('=').append(entry.getValue());
		}
		combined.append(']');
		
		try {
			BlockData data =  Bukkit.createBlockData(type, combined.toString());
			assert data != null;
			return new NewBlockValues(type, data, false);
		} catch (IllegalArgumentException e) {
			Skript.error("Parsing block state " + combined + " failed!");
			if (Skript.debug())
				e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean isEmpty(Material type) {
		return type == Material.AIR || type == Material.CAVE_AIR || type == Material.VOID_AIR;
	}

	@Override
	public boolean isLiquid(Material type) {
		return type == Material.WATER || type == Material.LAVA;
	}
	
}
