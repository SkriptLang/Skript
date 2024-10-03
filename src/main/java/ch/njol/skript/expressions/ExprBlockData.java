package ch.njol.skript.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.BlockDataUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

@Name("Block Data")
@Description({
	"Get the <a href='classes.html#blockdata'>block data</a> associated with a block or itemtype.",
	"This data can also be used to set blocks, or the set the blockdata of a valid item."
})
@Examples({
	"set {data} to block data of target block",
	"set block at player to {data}",
	"set block data of target block to oak_stairs[facing=south;waterlogged=true]",
	"",
	"set {data} to block data of oak log",
	"set block data of {data} to oak_log[axis=z]"
})
@RequiredPlugins("Minecraft 1.13+")
@Since("2.5, 2.5.2 (set), INSERT VERSION (itemtypes)")
public class ExprBlockData extends PropertyExpression<Object, BlockData> {
	
	static {
		register(ExprBlockData.class, BlockData.class, "block[ ]data", "blocks/itemtypes");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(exprs[0]);
		return true;
	}

	@Override
	protected BlockData[] get(Event event, Object[] source) {
		Set<BlockData> datas = new HashSet<>();
		for (Object object : source) {
			if (object instanceof Block block) {
				datas.add(block.getBlockData());
			} else if (object instanceof ItemType item && item.getItemMeta() instanceof BlockDataMeta meta) {
				if (item.isAll()) {
					for (ItemStack stack : item.getAll()) {
						addIfValid(datas, meta, stack);
					}
				} else {
					ItemStack random = item.getRandom();
					if (random != null)
						addIfValid(datas, meta, random);
				}
			}
		}
		return datas.isEmpty() ? new BlockData[0] : datas.toArray(BlockData[]::new);
	}

	private void addIfValid(Set<BlockData> datas, BlockDataMeta meta, ItemStack item) {
		Material asBlock = BlockDataUtils.toBlock(item);
		if (asBlock != null)
			datas.add(meta.getBlockData(asBlock));
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return mode == ChangeMode.SET ? CollectionUtils.array(BlockData.class) : null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (delta != null && delta[0] instanceof BlockData data) {
			for (Object object : getExpr().getArray(event)) {
				if (object instanceof Block block) {
					block.setBlockData(data);
				} else if (object instanceof ItemType item && item.getItemMeta() instanceof BlockDataMeta meta) {
					meta.setBlockData(data);
					item.setItemMeta(meta);
				}
			}
		}
	}

	@Override
	public boolean isSingle() {
		return getExpr().isSingle();
	}

	@Override
	public Class<? extends BlockData> getReturnType() {
		return BlockData.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "block data of " + getExpr().toString(event, debug);
	}

}
