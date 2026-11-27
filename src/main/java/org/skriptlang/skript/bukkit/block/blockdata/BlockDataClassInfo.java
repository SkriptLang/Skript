package org.skriptlang.skript.bukkit.block.blockdata;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.util.BlockUtils;
import ch.njol.yggdrasil.Fields;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Nullable;

import java.io.StreamCorruptedException;

public class BlockDataClassInfo extends ClassInfo<BlockData> {

	public BlockDataClassInfo() {
		super(BlockData.class, "blockdata");
		this.user("block ?datas?")
			.name("Block Data")
			.description("""
				Block data is the detailed information about a block, referred to in Minecraft as BlockStates, \
				allowing for the manipulation of different aspects of the block, including shape, waterlogging,  \
				direction the block is facing, and so much more.
				Information regarding each block's optional data can be found on Minecraft's Wiki. Find the block you're \
				looking for and scroll down to 'Block States'. Different states must be separated by a semicolon (see examples).
				The 'minecraft:' namespace is optional, as well as are underscores.
				""")
			.examples("set block at player to campfire[lit=false]",
					"set target block of player to oak stairs[facing=north;waterlogged=true]",
					"set block at player to grass_block[snowy=true]",
					"set loop-block to minecraft:chest[facing=north]",
					"set block above player to oak_log[axis=y]",
					"set target block of player to minecraft:oak_leaves[distance=2;persistent=false]")
			.after("itemtype")
			.since("2.5")
			.parser(new BlockDataParser())
			.serializer(new BlockDataSerializer())
			.cloner(BlockData::clone);
	}

	private static class BlockDataParser extends Parser<BlockData> {
		//<editor-fold desc="BlockDataParser", defaultstate="collapsed">
		@Override
		public @Nullable BlockData parse(String input, ParseContext context) {
			return BlockUtils.createBlockData(input);
		}

		@Override
		public String toString(BlockData blockData, int flags) {
			return blockData.getAsString().replace(",", ";");
		}

		@Override
		public String toVariableNameString(BlockData blockData) {
			return "blockdata:" + blockData.getAsString();
		}
		//</editor-fold>
	}

	private static class BlockDataSerializer extends Serializer<BlockData> {
		//<editor-fold desc="BlockDataSerializer", defaultstate="collapsed">
		@Override
		public Fields serialize(BlockData blockData) {
			return Fields.singletonObject("blockdata", blockData.getAsString());
		}

		@Override
		protected BlockData deserialize(Fields fields) throws StreamCorruptedException {
			String data = fields.getObject("blockdata", String.class);
			assert data != null;
			try {
				return Bukkit.createBlockData(data);
			} catch (IllegalArgumentException ex) {
				throw new StreamCorruptedException("Invalid block data: " + data);
			}
		}

		@Override
		public boolean mustSyncDeserialization() {
			return true;
		}

		@Override
		protected boolean canBeInstantiated() {
			return false;
		}
		//</editor-fold>
	}

}
