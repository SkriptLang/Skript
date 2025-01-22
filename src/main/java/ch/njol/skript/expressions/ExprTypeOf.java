package ch.njol.skript.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.skript.util.EnchantmentType;
import org.bukkit.enchantments.Enchantment;
import org.skriptlang.skript.lang.converter.Converters;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

@Name("Type of")
@Description({
	"Type of a block, item, entity, inventory, potion effect or enchantment type.",
	"Types of items, blocks and block datas are item types similar to them but have amounts",
	"of one, no display names and, on Minecraft 1.13 and newer versions, are undamaged.",
	"Types of entities and inventories are entity types and inventory types known to Skript.",
	"Types of potion effects are potion effect types.",
	"Types of enchantment types are enchantments."
})
@Examples({"on rightclick on an entity:",
	"\tmessage \"This is a %type of clicked entity%!\""})
@Since("1.4, 2.5.2 (potion effect), 2.7 (block datas), 2.10 (enchantment type)")
public class ExprTypeOf extends SimplePropertyExpression<Object, Object> {

	static {
		register(ExprTypeOf.class, Object.class, "type",
			"entitydatas/itemtypes/inventories/potioneffects/blockdatas/enchantmenttypes");
	}

	@Override
	protected String getPropertyName() {
		return "type";
	}

	@Override
	@Nullable
	public Object convert(Object object) {
		if (object instanceof EntityData<?> entityData) {
			return entityData.getSuperType();
		} else if (object instanceof ItemType itemType) {
			return itemType.getBaseType();
		} else if (object instanceof Inventory inventory) {
			return inventory.getType();
		} else if (object instanceof PotionEffect potionEffect) {
			return potionEffect.getType();
		} else if (object instanceof BlockData blockData) {
			return new ItemType(blockData.getMaterial());
		} else if (object instanceof EnchantmentType enchantmentType) {
			return enchantmentType.getType();
		}
		assert false;
		return null;
	}

	@Override
	public Class<?> getReturnType() {
		Class<?> returnType = getExpr().getReturnType();
		return EntityData.class.isAssignableFrom(returnType) ? EntityData.class
			: ItemType.class.isAssignableFrom(returnType) ? ItemType.class
			: PotionEffectType.class.isAssignableFrom(returnType) ? PotionEffectType.class
			: EnchantmentType.class.isAssignableFrom(returnType) ? Enchantment.class
			: BlockData.class.isAssignableFrom(returnType) ? ItemType.class : Object.class;
	}

	@Override
	@Nullable
	protected <R> ConvertedExpression<Object, ? extends R> getConvertedExpr(final Class<R>... to) {
		if (!Converters.converterExists(EntityData.class, to) && !Converters.converterExists(ItemType.class, to))
			return null;
		return super.getConvertedExpr(to);
	}

}
