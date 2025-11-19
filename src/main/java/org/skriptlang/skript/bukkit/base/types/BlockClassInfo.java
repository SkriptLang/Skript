package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.util.BlockUtils;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.yggdrasil.Fields;
import org.bukkit.Material;
import org.bukkit.Nameable;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

import java.io.StreamCorruptedException;
import java.util.Arrays;
import java.util.Objects;

@ApiStatus.Internal
public class BlockClassInfo extends ClassInfo<Block> {

	public BlockClassInfo() {
		super(Block.class, "block");
		this.user("blocks?")
			.name("Block")
			.description("A block in a <a href='#world'>world</a>. It has a <a href='#location'>location</a> and a <a href='#itemstack'>type</a>, " +
				"and can also have a <a href='#direction'>direction</a> (mostly a <a href='#ExprFacing'>facing</a>), an <a href='#inventory'>inventory</a>, or other special properties.")
			.usage("")
			.examples("")
			.since("1.0")
			.defaultExpression(new EventValueExpression<>(Block.class))
			.parser(new BlockParser())
			.changer(new BlockChanger())
			.serializer(new BlockSerializer())
			.property(Property.NAME,
				"The custom name of the block, if it has one. Only TileEntities like chests and furnaces can " +
					"have names. Can be set or reset.",
				Skript.instance(),
				new BlockNameHandler());
	}

	private static class BlockNameHandler implements ExpressionPropertyHandler<Block, String> {
		//<editor-fold desc="name property for blocks" defaultstate="collapsed">
		@Override
		public String convert(Block block) {
			BlockState state = block.getState();
			if (state instanceof Nameable nameable)
				//noinspection deprecation
				return nameable.getCustomName();
			return null;
		}

		@Override
		public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
			if (mode == ChangeMode.SET || mode == ChangeMode.RESET)
				return new Class[]{String.class};
			return null;
		}

		@Override
		public void change(Block named, Object @Nullable [] delta, ChangeMode mode) {
			assert mode == ChangeMode.SET || mode == ChangeMode.RESET;
			String name = delta == null ? null : (String) delta[0];
			BlockState state = named.getState();
			if (state instanceof Nameable nameable) {
				//noinspection deprecation
				nameable.setCustomName(name);
				state.update(true, false);
			}
		}

		@Override
		public @NotNull Class<String> returnType() {
			return String.class;
		}
		//</editor-fold>
	}

	private static class BlockSerializer extends Serializer<Block> {
		//<editor-fold desc="block serializer" defaultstate="collapsed">
		@Override
		public Fields serialize(final Block b) {
			final Fields f = new Fields();
			f.putObject("world", b.getWorld());
			f.putPrimitive("x", b.getX());
			f.putPrimitive("y", b.getY());
			f.putPrimitive("z", b.getZ());
			return f;
		}

		@Override
		public void deserialize(final Block o, final Fields f) {
			assert false;
		}

		@Override
		protected Block deserialize(final Fields fields) throws StreamCorruptedException {
			final World w = fields.getObject("world", World.class);
			final int x = fields.getPrimitive("x", int.class), y = fields.getPrimitive("y", int.class), z = fields.getPrimitive("z", int.class);
			if (w == null)
				throw new StreamCorruptedException();
			return w.getBlockAt(x, y, z);
		}

		@Override
		public boolean mustSyncDeserialization() {
			return true;
		}

		@Override
		public boolean canBeInstantiated() {
			return false;
		}
		//</editor-fold>
	}

	private static class BlockParser extends Parser<Block> {
		//<editor-fold desc="block parser" defaultstate="collapsed">
		@Override
		public @Nullable Block parse(final String s, final ParseContext context) {
			return null;
		}

		@Override
		public boolean canParse(final ParseContext context) {
			return false;
		}

		@Override
		public String toString(final Block b, final int flags) {
			return BlockUtils.blockToString(b, flags);
		}

		@Override
		public String toVariableNameString(final Block b) {
			return b.getWorld().getName() + ":" + b.getX() + "," + b.getY() + "," + b.getZ();
		}

		@Override
		public String getDebugMessage(final Block b) {
			return toString(b, 0) + " block (" + b.getWorld().getName() + ":" + b.getX() + "," + b.getY() + "," + b.getZ() + ")";
		}
		//</editor-fold>
	}

	public static class BlockChanger implements Changer<Block> {
		//<editor-fold desc="block changer" defaultstate="collapsed">
		@Override
		public Class<?> @Nullable [] acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.RESET)
				return null; // REMIND regenerate?
			if (mode == ChangeMode.SET)
				return CollectionUtils.array(ItemType.class, BlockData.class);
			return CollectionUtils.array(ItemType[].class, Inventory[].class);
		}

		@Override
		public void change(Block[] blocks, Object @Nullable [] delta, ChangeMode mode) {
			for (Block block : blocks) {
				assert block != null;
				switch (mode) {
					case SET:
						assert delta != null;
						Object object = delta[0];
						if (object instanceof ItemType itemType) {
							itemType.getBlock().setBlock(block, true);
						} else if (object instanceof BlockData blockData) {
							block.setBlockData(blockData);
						}
						break;
					case DELETE:
						block.setType(Material.AIR, true);
						break;
					case ADD:
					case REMOVE:
					case REMOVE_ALL:
						assert delta != null;
						BlockState state = block.getState();
						if (!(state instanceof InventoryHolder inventoryHolder))
							break;
						Inventory invi = inventoryHolder.getInventory();
						if (mode == ChangeMode.ADD) {
							for (Object obj : delta) {
								if (obj instanceof Inventory itemStacks) {
									for (ItemStack itemStack : itemStacks) {
										if (itemStack != null)
											invi.addItem(itemStack);
									}
								} else {
									((ItemType) obj).addTo(invi);
								}
							}
						} else {
							for (Object obj : delta) {
								if (obj instanceof Inventory inventory) {
									invi.removeItem(Arrays.stream(inventory.getContents())
											.filter(Objects::nonNull)
											.toArray(ItemStack[]::new));
								} else {
									if (mode == ChangeMode.REMOVE)
										((ItemType) obj).removeFrom(invi);
									else
										((ItemType) obj).removeAll(invi);
								}
							}
						}
						state.update();
						break;
					case RESET:
						assert false;
				}
			}
		}
		//</editor-fold>
	}

}
