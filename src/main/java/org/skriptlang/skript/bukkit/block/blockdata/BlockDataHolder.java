package org.skriptlang.skript.bukkit.block.blockdata;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.util.StringUtils;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper class for listing types that contain {@link BlockData}.
 * @param <Type> The object type that contains {@link BlockData}.
 */
public interface BlockDataHolder<Type> {

	/**
	 * {@link BlockDataHolder} for {@link Block}s.
	 */
	BlockDataHolder<Block> BLOCK = new BlockDataHolder<>() {
		//<editor-fold desc="BLOCK", defaultstate="collapsed">
		@Override
		public String getPattern(boolean plural) {
			return plural ? "blocks" : "block";
		}

		@Override
		public Class<Block> getTypeClass() {
			return Block.class;
		}

		@Override
		public BlockData getBlockData(Block block) {
			return block.getBlockData();
		}

		@Override
		public void setBlockData(Block block, BlockData blockData) {
			block.setBlockData(blockData);
		}
		//</editor-fold>
	};

	/**
	 * {@link BlockDataHolder} for {@link BlockData}s.
	 */
	BlockDataHolder<BlockData> BLOCK_DATA = new BlockDataHolder<>() {
		//<editor-fold desc="BLOCK_DATA", defaultstate="collapsed">
		@Override
		public String getPattern(boolean plural) {
			return plural ? "blockdatas" : "blockdata";
		}

		@Override
		public Class<BlockData> getTypeClass() {
			return BlockData.class;
		}

		@Override
		public BlockData getBlockData(BlockData blockData) {
			return blockData;
		}

		@Override
		public void setBlockData(BlockData blockData, BlockData blockData2) {
			blockData2.copyTo(blockData);
		}
		//</editor-fold>
	};

	/**
	 * {@link BlockDataHolder} for {@link ItemType}s.
	 */
	BlockDataHolder<ItemType> ITEMTYPE = new BlockDataHolder<>() {
		//<editor-fold desc="ITEMTYPE", defaultstate="collapsed">
		@Override
		public String getPattern(boolean plural) {
			return plural ? "itemtypes" : "itemtype";
		}

		@Override
		public Class<ItemType> getTypeClass() {
			return ItemType.class;
		}

		@Override
		public boolean isType(Object object) {
			return object instanceof ItemType itemType && itemType.getItemMeta() instanceof BlockDataMeta && itemType.getMaterial().isBlock();
		}

		@Override
		public BlockData getBlockData(ItemType itemType) {
			return ((BlockDataMeta) itemType.getItemMeta()).getBlockData(itemType.getMaterial());
		}

		@Override
		public void setBlockData(ItemType itemType, BlockData blockData) {
			BlockDataMeta blockDataMeta = (BlockDataMeta) itemType.getItemMeta();
			blockDataMeta.setBlockData(blockData);
			itemType.setItemMeta(blockDataMeta);
		}
		//</editor-fold>
	};

	/**
	 * {@link BlockDataHolder} for {@link BlockDisplay}s.
	 */
	BlockDataHolder<BlockDisplay> BLOCK_DISPLAY = new BlockDataHolder<>() {
		//<editor-fold desc="BLOCK_DISPLAY", defaultstate="collapsed">
		@Override
		public String getPattern(boolean plural) {
			return plural ? "displays" : "display";
		}

		@Override
		public Class<BlockDisplay> getTypeClass() {
			return BlockDisplay.class;
		}

		@Override
		public BlockData getBlockData(BlockDisplay blockDisplay) {
			return blockDisplay.getBlock();
		}

		@Override
		public void setBlockData(BlockDisplay blockDisplay, BlockData blockData) {
			blockDisplay.setBlock(blockData);
		}
		//</editor-fold>
	};

	/**
	 * {@link BlockDataHolder} for {@link FallingBlock}s.
	 */
	BlockDataHolder<FallingBlock> FALLING_BLOCK = new BlockDataHolder<>() {
		//<editor-fold desc="FALLING_BLOCK", defaultstate="collapsed">
		@Override
		public String getPattern(boolean plural) {
			return plural ? "entities" : "entity";
		}

		@Override
		public Class<FallingBlock> getTypeClass() {
			return FallingBlock.class;
		}

		@Override
		public BlockData getBlockData(FallingBlock fallingBlock) {
			return fallingBlock.getBlockData();
		}

		@Override
		public void setBlockData(FallingBlock fallingBlock, BlockData blockData) {
			fallingBlock.setBlockData(blockData);
		}
		//</editor-fold>
	};

	/**
	 * List of all {@link BlockDataHolder}s currently supported.
	 */
	List<BlockDataHolder<?>> HOLDERS = List.of(BLOCK, BLOCK_DATA, ITEMTYPE, BLOCK_DISPLAY, FALLING_BLOCK);

	/**
	 * Combined pattern of all {@link BlockDataHolder}s plural type with "/".
	 * Used for patterns when registering a {@link SyntaxElement}.
	 */
	String PLURAL_PATTERN_TYPES = StringUtils.join(HOLDERS.stream()
		.map(holder -> holder.getPattern(true)).collect(Collectors.toSet()),
		"/");

	/**
	 * Retrieves the {@link BlockDataHolder} that handles {@code object}.
	 * @param object The {@link Object} to get a {@link BlockDataHolder} for.
	 * @return The resulting {@link BlockDataHolder} if found, otherwise {@code null}.
	 */
	static @Nullable BlockDataHolder<?> getHolder(Object object) {
		for (BlockDataHolder<?> holder : HOLDERS) {
			if (holder.isType(object))
				return holder;
		}
		return null;
	}

	/**
	 * @return The singular pattern used for registering {@link SyntaxElement}s.
	 */
	default String getPattern() {
		return getPattern(false);
	}

	/**
	 * @param plural Whether the returned pattern should be plural or singular.
	 * @return The resulting singular or plural pattern used for registering {@link SyntaxElement}s.
	 */
	String getPattern(boolean plural);

	/**
	 * @return The {@link Class} {@code this} handles.
	 */
	Class<Type> getTypeClass();

	/**
	 * Whether {@code object} is handled by {@code this}.
	 * @param object The {@link Object} to check.
	 * @return {@code true} if handled, otherwise {@code false}.
	 */
	default boolean isType(Object object) {
		return getTypeClass().isInstance(object);
	}

	/**
	 * @param type The object to get the {@link BlockData} from.
	 * @return The {@link BlockData} of {@code type}.
	 */
	BlockData getBlockData(Type type);

	/**
	 * Sets the {@link BlockData} on {@code type} to {@code blockData}.
	 * @param type The object to change the {@link BlockData} of.
	 * @param blockData The {@link BlockData} to change to.
	 */
	void setBlockData(Type type, BlockData blockData);

}
