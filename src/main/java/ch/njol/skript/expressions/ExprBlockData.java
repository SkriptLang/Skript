package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Block Data")
@Description({
	"Get the <a href='#blockdata'>block data</a> associated with a block.",
	"This data can also be used to set blocks."
})
@Examples({
	"set {_data} to block data of target block",
	"set block at player to {_data}",
	"",
	"set block data of target block to oak_stairs[facing=south;waterlogged=true]"
})
@Since("2.5, 2.5.2 (set), 2.10 (block displays)")
public class ExprBlockData extends SimplePropertyExpression<Object, BlockData> {

	static {
		register(ExprBlockData.class, BlockData.class, "block[ ]data", "blocks/displays/entities");
	}

	@Override
	public @Nullable BlockData convert(Object object) {
		if (object instanceof Block block)
			return block.getBlockData();
		if (object instanceof BlockDisplay blockDisplay)
			return blockDisplay.getBlock();
		if (object instanceof FallingBlock fallingBlock)
			return fallingBlock.getBlockData();
		return null;

	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(BlockData.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null; // reset/delete not supported
		BlockData blockData = ((BlockData) delta[0]);
		for (Object object : getExpr().getArray(event)) {
			if (object instanceof Block block) {
				block.setBlockData(blockData);
			} else if (object instanceof BlockDisplay blockDisplay) {
				blockDisplay.setBlock(blockData);
			} else if (object instanceof FallingBlock fallingBlock) {
				fallingBlock.setBlockData(blockData);
			}
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
