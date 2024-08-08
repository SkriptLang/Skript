package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.BrushableBlock;
import org.bukkit.block.BlockState;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Dusted Item")
@Description({
	"Represents the item that is uncovered when dusting.",
	"The only blocks that can currently be \"dusted\" are Suspicious Gravel and Suspicious Sand."
})
@Examples({
	"send target block's brushable item",
	"set {_gravel}'s brushable item to emerald"
})
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.20+")
public class ExprBrushableItem extends SimpleExpression<ItemStack> {

	private static final boolean SUPPORTS_DUSTING = Skript.classExists("org.bukkit.block.BrushableBlock");

	static {
		if (SUPPORTS_DUSTING)
			Skript.registerExpression(ExprBrushableItem.class, ItemStack.class, ExpressionType.SIMPLE,
				"[the] %blocks% brush[able] item",
				"%blocks%'[s] brush[able] item");
	}

	private Expression<Block> blocks;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		blocks = (Expression<Block>) exprs[0];
		return true;
	}

	@Nullable
	@Override
	protected ItemStack[] get(Event event) {
		Block[] blockArray = blocks.getArray(event);
		ItemStack[] items = new ItemStack[blockArray.length];
		for (int i = 0; i < blockArray.length; i++) {
			Block block = blockArray[i];
			BlockState state = block.getState();
			if (state instanceof BrushableBlock) {
				BrushableBlock brushableBlock = (BrushableBlock) state;
				items[i] = brushableBlock.getItem();
			} else {
				items[i] = null;
			}
		}
		return items;
	}

	@Override
	public boolean isSingle() {
		return blocks.isSingle();
	}

	@Override
	public Class<? extends ItemStack> getReturnType() {
		return ItemStack.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return blocks.toString(event, debug) + "'s brushable item";
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.SET && delta.length > 0) {
			ItemStack newItem = (ItemStack) delta[0];
			for (Block block : blocks.getArray(event)) {
				BlockState state = block.getState();
				if (state instanceof BrushableBlock) {
					BrushableBlock brushableBlock = (BrushableBlock) state;
					brushableBlock.setItem(newItem);
					state.update(true);
				}
			}
		}
	}

	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.SET) {
			return new Class[]{ItemStack.class};
		}
		return null;
	}

}
