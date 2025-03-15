package ch.njol.skript.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Enderman Carrying BlockData")
@Description({
	"The block data an enderman is carrying.",
	"Custom attributes such as NBT or names do not transfer over.",
	"Blocks, blockdatas and items are acceptable objects to change the carrying block."
})
@Examples({
	"broadcast the carrying blockdata of last spawned enderman",
	"set the carried block of last spawned enderman to an oak log",
	"set the carrying block data of {_enderman} to oak stairs[facing=north]",
	"set the carried blockdata of {_enderman} to {_item}",
	"clear the carried blockdata of {_enderman}"
})
@Since("INSERT VERSION")
public class ExprCarryingBlockData extends SimplePropertyExpression<LivingEntity, BlockData> {

	static {
		register(ExprCarryingBlockData.class, BlockData.class, "carr(ied|ying) block[[ ]data]", "livingentities");
	}

	@Override
	public @Nullable BlockData convert(LivingEntity entity) {
		if (entity instanceof Enderman enderman)
			return enderman.getCarriedBlock();
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(Block.class, BlockData.class, ItemType.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		BlockData data = null;
		if (delta != null) {
			if (delta[0] instanceof BlockData blockData) {
				data = blockData;
			} else if (delta[0] instanceof Block block) {
				data = block.getBlockData();
			} else {
				ItemStack itemStack = ItemUtils.asItemStack(delta[0]);
				if (itemStack != null) {
					Material stackMaterial = itemStack.getType();
					if (stackMaterial.isBlock())
						data = stackMaterial.createBlockData();
				}
			}
		}

		for (LivingEntity entity : getExpr().getArray(event)) {
			if (entity instanceof Enderman enderman)
				enderman.setCarriedBlock(data);
		}
	}

	@Override
	public Class<? extends BlockData> getReturnType() {
		return BlockData.class;
	}

	@Override
	protected String getPropertyName() {
		return "carrying block data";
	}

}
