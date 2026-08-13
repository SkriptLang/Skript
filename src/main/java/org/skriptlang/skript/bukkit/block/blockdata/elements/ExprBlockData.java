package org.skriptlang.skript.bukkit.block.blockdata.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.block.blockdata.BlockDataHolder;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Block Data")
@Description("""
	The <a href='#blockdata'>block data</a> associated with a block or block related objects. \
	(i.e. blocks, block displays, falling blocks, items)
	""")
@Example("set {_data} to block data of target block")
@Example("set block at player to {_data}")
@Example("set block data of target block to oak_stairs[facing=south;waterlogged=true]")
@Example("reset the blockdata of {_block}")
@Since({
	"2.5",
	"2.5.2 (set)",
	"2.10 (block displays)",
	"INSERT VERSION (items, reset)"
})
public class ExprBlockData extends SimplePropertyExpression<Object, BlockData> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(
			ExprBlockData.class,
			BlockData.class,
			"block[ ]data",
			BlockDataHolder.PLURAL_PATTERN_TYPES,
			false
		).supplier(ExprBlockData::new)
			.build());
	}

	@Override
	public @Nullable BlockData convert(Object object) {
		BlockDataHolder holder = BlockDataHolder.getHolder(object);
		if (holder == null)
			return null;
		//noinspection unchecked
		return holder.getBlockData(object);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			return CollectionUtils.array(BlockData.class);
		} else if (mode == ChangeMode.RESET) {
			return CollectionUtils.array();
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		BlockData newBlockData = delta == null ? null : ((BlockData) delta[0]);
		for (Object object : getExpr().getArray(event)) {
			BlockDataHolder holder = BlockDataHolder.getHolder(object);
			if (holder == null)
				continue;
			if (newBlockData != null) {
				//noinspection unchecked
				holder.setBlockData(object, newBlockData);
			}
			if (mode != ChangeMode.RESET)
				continue;
			//noinspection unchecked
			BlockData blockData = holder.getBlockData(object);
			String dataString = blockData.getMaterial().getKey() + "[]";
			try {
				BlockData newData = Bukkit.createBlockData(dataString);
				//noinspection unchecked
				holder.setBlockData(object, newData);
			} catch (Exception ignored) {}
		}
	}

	@Override
	public Class<? extends BlockData> getReturnType() {
		return BlockData.class;
	}

	@Override
	protected String getPropertyName() {
		return "block data";
	}

}
