package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.Nullable;

@Name("Exact Item")
@Description(
	"Get an exact item representation of a block, carrying over any data. "
	+ "For example, using this expression on a chest block with items stored inside will return a chest "
	+ "item with the exact same items in its inventory as the chest block."
)
@Example("set {_item} to exact item of block at location(0, 0, 0)")
@Since("2.12")
public class ExprExactItem extends SimplePropertyExpression<Block, ItemStack> {

	static {
		register(ExprExactItem.class, ItemStack.class, "exact item[s]", "blocks");
	}

	@Override
	public @Nullable ItemStack convert(Block block) {
		Material blockMaterial = block.getType();
		if (!blockMaterial.isItem())
			return null;
		ItemStack itemStack = new ItemStack(blockMaterial);
		if (itemStack.getItemMeta() instanceof BlockStateMeta blockStateMeta) {
			blockStateMeta.setBlockState(block.getState());
			itemStack.setItemMeta(blockStateMeta);
		}
		return itemStack;
	}

	@Override
	public Class<? extends ItemStack> getReturnType() {
		return ItemStack.class;
	}

	@Override
	protected String getPropertyName() {
		return "exact item";
	}

}
