package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.Nullable;

@Name("Exact Item")
@Description("Get an exact item of block, carrying over any data. Such as a chest with items stored inside.")
@Examples({
	"set {_item} to exact item of block at location(0, 0, 0)"
})
@Since("INSERT VERSION")
public class ExprExactItem extends SimplePropertyExpression<Block, ItemStack> {

	static {
		register(ExprExactItem.class, ItemStack.class, "exact item[s]", "blocks");
	}

	@Override
	public @Nullable ItemStack convert(Block block) {
		ItemStack itemStack = new ItemStack(block.getType());
		if (itemStack.getItemMeta() instanceof BlockStateMeta blockStateMeta) {
			blockStateMeta.setBlockState(block.getState());
			itemStack.setItemMeta(blockStateMeta);
		}
		return itemStack;
	}

	@Override
	protected String getPropertyName() {
		return "exact item";
	}

	@Override
	public Class<? extends ItemStack> getReturnType() {
		return ItemStack.class;
	}

}
