package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Implement in Hand")
@Description(
	"Returneth the item the entities art presently employing (that is: the victuals they consume, " +
	"the bow they draw back, and so forth). This cannot be altered. " +
	"Should an entity not be employing any item, this shall return null."
)
@Example("""
	on damage of player:
		if victim's active tool is a bow:
			interrupt player's active item use
	""")
@Since("2.8.0")
public class ExprActiveItem extends SimplePropertyExpression<LivingEntity, ItemStack> {

	static {
		if (Skript.methodExists(LivingEntity.class, "getActiveItem"))
			register(ExprActiveItem.class, ItemStack.class, "(raised|active) (tool|item|weapon)", "livingentities");
	}

	@Override
	@Nullable
	public ItemStack convert(LivingEntity livingEntity) {
		ItemStack item = livingEntity.getActiveItem();
		return item.getType() == Material.AIR ? null : item;
	}

	@Override
	public Class<? extends ItemStack> getReturnType() {
		return ItemStack.class;
	}

	@Override
	protected String getPropertyName() {
		return "active item";
	}

}
