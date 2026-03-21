package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BrushableBlock;
import org.bukkit.block.BlockState;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Buried Item")
@Description({
	"Represents the item that is uncovered when dusting.",
	"The only blocks that can currently be \"dusted\" are Suspicious Gravel and Suspicious Sand."
})
@Example("send target block's brushable item")
@Example("set {_gravel}'s brushable item to emerald")
@Since("2.12")
@RequiredPlugins("Minecraft 1.20+")
public class ExprBrushableItem extends SimplePropertyExpression<Block, ItemStack> {

	private static final boolean SUPPORTS_DUSTING = Skript.classExists("org.bukkit.block.BrushableBlock");

	static {
		if (SUPPORTS_DUSTING)
			register(ExprBrushableItem.class, ItemStack.class,
				"(brushable|buried) item",
				"blocks");
	}

	@Override
	public @Nullable ItemStack convert(Block block) {
		if (block.getState() instanceof BrushableBlock brushableBlock) {
			return brushableBlock.getItem();
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE) {
			return CollectionUtils.array(ItemStack.class);
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE) {
			ItemStack newItem = (mode == ChangeMode.SET) ? (ItemStack) delta[0] : null;
			for (Block block : getExpr().getArray(event)) {
				BlockState state = block.getState();
				if (state instanceof BrushableBlock brushableBlock) {
					brushableBlock.setItem(newItem);
					state.update();
				}
			}
		}
	}

	@Override
	public Class<? extends ItemStack> getReturnType() {
		return ItemStack.class;
	}

	@Override
	protected String getPropertyName() {
		return "brushable item";
	}

}
